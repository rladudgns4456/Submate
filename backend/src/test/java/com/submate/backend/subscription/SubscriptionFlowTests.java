package com.submate.backend.subscription;

import com.jayway.jsonpath.JsonPath;
import com.submate.backend.payment.dto.CheckoutRequest;
import com.submate.backend.payment.entity.PaymentMethod;
import com.submate.backend.payment.repository.PaymentRepository;
import com.submate.backend.payment.service.PaymentService;
import com.submate.backend.refund.repository.RefundRepository;
import com.submate.backend.subscription.repository.SubscriptionRepository;
import com.submate.backend.subscription.service.SubscriptionService;
import com.submate.backend.subscription.support.DomainException;
import java.time.*;
import java.util.List;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.*;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SubscriptionFlowTests.TimeConfiguration.class)
class SubscriptionFlowTests {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired PaymentService payments;
    @Autowired SubscriptionService subscriptions;
    @Autowired SubscriptionRepository subscriptionRepository;
    @Autowired PaymentRepository paymentRepository;
    @Autowired RefundRepository refundRepository;
    @Autowired TestClock clock;

    @TestConfiguration
    static class TimeConfiguration {
        @Bean @Primary TestClock testClock() { return new TestClock(); }
    }
    static class TestClock extends Clock {
        private volatile Instant now = Instant.parse("2026-01-31T03:00:00Z");
        public void setDate(String date) { now = LocalDate.parse(date).atTime(12, 0).atZone(getZone()).toInstant(); }
        public void setInstant(String instant) { now = Instant.parse(instant); }
        @Override public ZoneId getZone() { return ZoneId.of("Asia/Seoul"); }
        @Override public Clock withZone(ZoneId zone) { return Clock.fixed(now, zone); }
        @Override public Instant instant() { return now; }
    }

    @BeforeEach
    void resetData() {
        jdbc.update("delete from tracked_subscriptions");
        jdbc.update("delete from refunds");
        jdbc.update("delete from payments");
        jdbc.update("delete from subscriptions");
        jdbc.update("delete from products");
        jdbc.update("delete from categories");
        jdbc.update("delete from members");
        new ResourceDatabasePopulator(new ClassPathResource("db/demo-data.sql")).execute(jdbc.getDataSource());
        clock.setDate("2026-01-31");
    }

    private String body(long product, String key, boolean failure) {
        return "{\"productId\":" + product + ",\"paymentMethod\":\"CARD\",\"requestKey\":\"" + key + "\",\"simulateFailure\":" + failure + "}";
    }
    private ResultActions checkout(String key) throws Exception {
        return mvc.perform(post("/api/subscriptions").with(user("user@submate.test").roles("USER"))
                .contentType("application/json").content(body(1, key, false)));
    }
    private long number(MvcResult result, String path) throws Exception {
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), path)).longValue();
    }
    private long subscribe() throws Exception { return number(checkout("first").andExpect(status().isCreated()).andReturn(), "$.subscription.subscriptionId"); }
    private long refundRequest(long paymentId) throws Exception {
        var result = mvc.perform(post("/api/refunds").with(user("user@submate.test"))
                .contentType("application/json").content("{\"paymentId\":" + paymentId + ",\"reason\":\"이용 계획 변경\"}"))
                .andExpect(status().isCreated()).andReturn();
        return number(result, "$.refundId");
    }
    private ResultActions decide(long refundId, String decision) throws Exception {
        return mvc.perform(patch("/api/admin/refunds/" + refundId).with(user("admin@submate.test").roles("ADMIN"))
                .contentType("application/json").content("{\"decision\":\"" + decision + "\"}"));
    }

    @Test void requiresAuthenticationAndNeverTrustsMemberId() throws Exception {
        mvc.perform(get("/api/subscriptions")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/subscriptions").contentType("application/json").content(body(1, "anonymous", false)))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/payments").with(user("user@submate.test").roles("USER"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/subscriptions").with(user("missing@submate.test"))).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/payments").with(user("user@submate.test").roles("ADMIN"))).andExpect(status().isForbidden());
    }

    @Test void tracksExternalSubscriptionsWithMonthEndRemindersAndMemberIsolation() throws Exception {
        String path = "/api/tracked-subscriptions";
        String input = """
                {"name":"Streaming","amount":12900,"billingCycle":"MONTHLY",
                 "billingDate":"2026-01-31","active":true,"reminderDays":3}
                """;
        var owner = user("user@submate.test");
        var other = user("other@submate.test");
        mvc.perform(get(path)).andExpect(status().isUnauthorized());
        mvc.perform(post(path).with(owner).contentType("application/json")
                .content(input.replace("12900", "12900.5"))).andExpect(status().isBadRequest());
        var created = mvc.perform(post(path).with(owner).contentType("application/json").content(input))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nextBillingDate").value("2026-01-31"))
                .andExpect(jsonPath("$.reminderDue").value(true)).andReturn();
        long id = number(created, "$.id");
        clock.setDate("2026-02-25");
        mvc.perform(get(path).with(owner)).andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyTotal").value(12900))
                .andExpect(jsonPath("$.subscriptions[0].nextBillingDate").value("2026-02-28"))
                .andExpect(jsonPath("$.subscriptions[0].reminderDue").value(true));
        clock.setDate("2026-03-01");
        mvc.perform(get(path).with(owner)).andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptions[0].nextBillingDate").value("2026-03-31"))
                .andExpect(jsonPath("$.subscriptions[0].reminderDue").value(false));
        mvc.perform(get(path).with(other)).andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptions").isEmpty());
        mvc.perform(put(path + "/" + id).with(other).contentType("application/json").content(input))
                .andExpect(status().isNotFound());
        mvc.perform(delete(path + "/" + id).with(other)).andExpect(status().isNotFound());
        mvc.perform(put(path + "/" + id).with(owner).contentType("application/json")
                .content(input.replace("true", "false"))).andExpect(status().isOk());
        mvc.perform(get(path).with(owner)).andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyTotal").value(0))
                .andExpect(jsonPath("$.subscriptions[0].reminderDue").value(false));
        mvc.perform(delete(path + "/" + id).with(owner)).andExpect(status().isNoContent());
        mvc.perform(get(path).with(owner)).andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptions").isEmpty());
        assertThat(subscriptionRepository.count()).isZero();
        assertThat(paymentRepository.count()).isZero();
    }

    @Test void deduplicatesReceiptImportsPerMemberAndCalculatesAnnualLeapDay() throws Exception {
        String path = "/api/tracked-subscriptions";
        String input = """
                {"receiptId":"gmail:%s","subscription":{"name":"Annual plan",
                 "amount":49000,"billingCycle":"YEARLY","billingDate":"2024-02-29",
                 "active":true,"reminderDays":0}}
                """.formatted("a".repeat(64));
        var owner = user("user@submate.test");
        clock.setDate("2026-02-28");
        mvc.perform(post(path + "/imports").with(owner).contentType("application/json").content(input))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imported").value(true))
                .andExpect(jsonPath("$.nextBillingDate").value("2026-02-28"));
        mvc.perform(post(path + "/imports").with(owner).contentType("application/json").content(input))
                .andExpect(status().isConflict());
        mvc.perform(post(path + "/imports").with(user("other@submate.test"))
                .contentType("application/json").content(input)).andExpect(status().isCreated());
        clock.setDate("2028-02-01");
        mvc.perform(get(path).with(owner)).andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyTotal").value(49000))
                .andExpect(jsonPath("$.subscriptions[0].nextBillingDate").value("2028-02-29"));
        clock.setDate("2028-03-01");
        mvc.perform(get(path).with(owner)).andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyTotal").value(0));
    }

    @Test void successfulPaymentCreatesActiveSubscriptionWithServerPrice() throws Exception {
        checkout("one").andExpect(status().isCreated())
                .andExpect(jsonPath("$.subscription.status").value("ACTIVE"))
                .andExpect(jsonPath("$.subscription.startDate").value("2026-01-31"))
                .andExpect(jsonPath("$.subscription.endDate").value("2026-02-28"))
                .andExpect(jsonPath("$.subscription.usable").value(true))
                .andExpect(jsonPath("$.payment.amount").value(12900.0))
                .andExpect(jsonPath("$.payment.status").value("COMPLETED"));
        assertThat(subscriptionRepository.count()).isEqualTo(1);
        assertThat(paymentRepository.count()).isEqualTo(1);
    }

    @ParameterizedTest
    @CsvSource({"2024-01-31,1,2024-02-29", "2026-01-31,1,2026-02-28", "2024-02-29,2,2025-02-28", "2026-12-31,1,2027-01-31"})
    void computesMonthEndAndLeapYear(String start, long productId, String end) {
        clock.setDate(start);
        var result = payments.checkout(1L, new CheckoutRequest(productId, PaymentMethod.CARD, "dates", false));
        assertThat(result.subscription().endDate()).isEqualTo(LocalDate.parse(end));
    }

    @Test void paymentFailureLeavesNoSubscriptionOrPayment() throws Exception {
        mvc.perform(post("/api/subscriptions").with(user("user@submate.test")).contentType("application/json")
                .content(body(1, "failed", true))).andExpect(status().isBadRequest());
        assertThat(subscriptionRepository.count()).isZero();
        assertThat(paymentRepository.count()).isZero();
        checkout("failed").andExpect(status().isCreated());
    }

    @Test void paymentInsertFailureRollsBackSubscription() {
        jdbc.execute("alter table payments add constraint ck_test_failure check(request_key <> 'force-db-failure')");
        try {
            assertThatThrownBy(() -> payments.checkout(1L, new CheckoutRequest(1L, PaymentMethod.CARD, "force-db-failure", false)))
                    .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
            assertThat(subscriptionRepository.count()).isZero();
            assertThat(paymentRepository.count()).isZero();
        } finally { jdbc.execute("alter table payments drop constraint ck_test_failure"); }
    }

    @Test void replayIsIdempotentAndDifferentPayloadConflicts() throws Exception {
        long id = subscribe();
        checkout("first").andExpect(status().isOk()).andExpect(jsonPath("$.replayed").value(true))
                .andExpect(jsonPath("$.subscription.subscriptionId").value(id));
        mvc.perform(post("/api/subscriptions").with(user("user@submate.test")).contentType("application/json")
                .content(body(2, "first", false))).andExpect(status().isConflict());
        checkout("different-key").andExpect(status().isConflict());
        assertThat(paymentRepository.count()).isEqualTo(1);
    }

    @Test void concurrentSameKeyProducesExactlyOnePayment() throws Exception {
        var results = race("same", "same");
        assertThat(results).containsExactlyInAnyOrder(201, 200);
        assertThat(paymentRepository.count()).isEqualTo(1);
        assertThat(subscriptionRepository.count()).isEqualTo(1);
    }
    @Test void concurrentDifferentKeysStillPreventsDuplicateLiveSubscription() throws Exception {
        assertThat(race("first-key", "second-key")).containsExactlyInAnyOrder(201, 409);
        assertThat(paymentRepository.count()).isEqualTo(1);
    }
    private List<Integer> race(String first, String second) throws Exception {
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var futures = List.of(first, second).stream().map(key -> pool.submit(() -> {
                ready.countDown();
                if (!start.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("start timeout");
                try { return payments.checkout(1L, new CheckoutRequest(1L, PaymentMethod.CARD, key, false)).replayed() ? 200 : 201; }
                catch (DomainException ex) { return ex.getStatus(); }
            })).toList();
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            return List.of(futures.get(0).get(20, TimeUnit.SECONDS), futures.get(1).get(20, TimeUnit.SECONDS));
        }
    }

    @Test void inactiveMissingOrInvalidProductIsRejected() throws Exception {
        mvc.perform(post("/api/subscriptions").with(user("user@submate.test")).contentType("application/json")
                .content(body(3, "inactive", false))).andExpect(status().isConflict());
        mvc.perform(post("/api/subscriptions").with(user("user@submate.test")).contentType("application/json")
                .content(body(999, "missing", false))).andExpect(status().isNotFound());
        jdbc.update("update products set price = 0 where product_id = 1");
        checkout("free").andExpect(status().isConflict());
        assertThat(paymentRepository.count()).isZero();
    }

    @Test void validatesRequestsAndPagination() throws Exception {
        mvc.perform(post("/api/subscriptions").with(user("user@submate.test")).contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/subscriptions").with(user("user@submate.test")).contentType("application/json")
                .content(body(-1, "bad", false))).andExpect(status().isBadRequest());
        mvc.perform(get("/api/subscriptions?page=-1").with(user("user@submate.test"))).andExpect(status().isBadRequest());
        mvc.perform(get("/api/subscriptions?size=101").with(user("user@submate.test"))).andExpect(status().isBadRequest());
        mvc.perform(get("/api/subscriptions/quote").with(user("user@submate.test"))).andExpect(status().isBadRequest());
    }

    @Test void otherMemberCannotReadCancelOrRefund() throws Exception {
        var result = checkout("private").andExpect(status().isCreated()).andReturn();
        long id = number(result, "$.subscription.subscriptionId");
        long paymentId = number(result, "$.payment.paymentId");
        mvc.perform(get("/api/subscriptions/" + id).with(user("other@submate.test"))).andExpect(status().isNotFound());
        mvc.perform(patch("/api/subscriptions/" + id + "/cancel").with(user("other@submate.test"))).andExpect(status().isNotFound());
        mvc.perform(post("/api/refunds").with(user("other@submate.test")).contentType("application/json")
                .content("{\"paymentId\":" + paymentId + ",\"reason\":\"타인\"}")).andExpect(status().isNotFound());
        for (String path : List.of("/api/subscriptions", "/api/payments", "/api/refunds")) {
            mvc.perform(get(path).with(user("other@submate.test"))).andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }

    @Test void cancellationKeepsAccessAndCannotBeRepeatedOrResubscribedEarly() throws Exception {
        long id = subscribe();
        mvc.perform(patch("/api/subscriptions/" + id + "/cancel").with(user("user@submate.test")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.usable").value(true)).andExpect(jsonPath("$.cancelledAt").isNotEmpty());
        mvc.perform(patch("/api/subscriptions/" + id + "/cancel").with(user("user@submate.test"))).andExpect(status().isConflict());
        checkout("too-early").andExpect(status().isConflict());
    }

    @ParameterizedTest @CsvSource({"true", "false"})
    void expiryAppliesToActiveAndCancelledAtKoreanMidnight(boolean cancel) throws Exception {
        long id = subscribe();
        if (cancel) subscriptions.cancel(1L, id);
        clock.setInstant("2026-02-27T14:59:59Z");
        assertThat(subscriptions.expireDueBatch()).isZero();
        assertThat(subscriptions.detail(1L, id).usable()).isTrue();
        clock.setInstant("2026-02-27T15:00:00Z");
        assertThat(subscriptions.detail(1L, id).usable()).isFalse();
        assertThat(subscriptions.expireDueBatch()).isEqualTo(1);
        assertThat(subscriptions.expireDueBatch()).isZero();
        assertThat(subscriptions.detail(1L, id).status().name()).isEqualTo("EXPIRED");
        mvc.perform(patch("/api/subscriptions/" + id + "/cancel").with(user("user@submate.test"))).andExpect(status().isConflict());
        long newId = number(checkout("new-after-expiry").andExpect(status().isCreated()).andReturn(), "$.subscription.subscriptionId");
        assertThat(newId).isNotEqualTo(id);
    }

    @Test void adminCanListDetailAndCancelButUserCannotManage() throws Exception {
        long id = subscribe();
        for (String path : List.of("/api/admin/subscriptions", "/api/admin/payments", "/api/admin/refunds")) {
            mvc.perform(get(path).with(user("admin@submate.test").roles("ADMIN"))).andExpect(status().isOk());
            mvc.perform(get(path).with(user("user@submate.test"))).andExpect(status().isForbidden());
        }
        mvc.perform(get("/api/admin/subscriptions/" + id).with(user("admin@submate.test").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.memberId").value(1));
        mvc.perform(patch("/api/admin/subscriptions/" + id + "/cancel").with(user("admin@submate.test").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test void approvingSameDayRefundUpdatesAllThreeRecordsAndStopsAccess() throws Exception {
        var result = checkout("refund-flow").andExpect(status().isCreated()).andReturn();
        long subscriptionId = number(result, "$.subscription.subscriptionId");
        long paymentId = number(result, "$.payment.paymentId");
        long refundId = refundRequest(paymentId);
        assertThat(subscriptions.detail(1L, subscriptionId).usable()).isTrue();
        mvc.perform(patch("/api/admin/refunds/" + refundId).with(user("user@submate.test"))
                .contentType("application/json").content("{\"decision\":\"APPROVE\"}")).andExpect(status().isForbidden());
        decide(refundId, "APPROVE").andExpect(status().isOk()).andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.refundedAt").isNotEmpty());
        assertThat(paymentRepository.findById(paymentId).orElseThrow().getStatus().name()).isEqualTo("REFUNDED");
        assertThat(subscriptions.detail(1L, subscriptionId).status().name()).isEqualTo("EXPIRED");
        assertThat(subscriptions.detail(1L, subscriptionId).usable()).isFalse();
        decide(refundId, "APPROVE").andExpect(status().isConflict());
        checkout("refund-flow").andExpect(status().isOk()).andExpect(jsonPath("$.payment.status").value("REFUNDED"));
        checkout("repurchase").andExpect(status().isCreated());
    }

    @Test void proratedRefundFloorsWonAndFreezesAmountOnRequestDate() throws Exception {
        var result = checkout("prorated").andExpect(status().isCreated()).andReturn();
        long paymentId = number(result, "$.payment.paymentId");
        clock.setDate("2026-02-10");
        long refundId = refundRequest(paymentId);
        mvc.perform(get("/api/refunds").with(user("user@submate.test")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].amount").value(8292))
                .andExpect(jsonPath("$.content[0].totalDays").value(28))
                .andExpect(jsonPath("$.content[0].remainingDays").value(18));
        clock.setDate("2026-02-12");
        decide(refundId, "APPROVE").andExpect(status().isOk()).andExpect(jsonPath("$.amount").value(8292));
    }

    @Test void expiredSubscriptionCannotRequestRefundEvenBeforeSchedulerRuns() throws Exception {
        var result = checkout("expired-refund").andExpect(status().isCreated()).andReturn();
        long paymentId = number(result, "$.payment.paymentId");
        clock.setDate("2026-02-28");
        mvc.perform(post("/api/refunds").with(user("user@submate.test"))
                .contentType("application/json").content("{\"paymentId\":" + paymentId + ",\"reason\":\"기간 종료\"}"))
                .andExpect(status().isConflict());
        assertThat(refundRepository.count()).isZero();
    }

    @Test void cancelledSubscriptionCanRefundRemainingDays() throws Exception {
        var result = checkout("cancel-refund").andExpect(status().isCreated()).andReturn();
        subscriptions.cancel(1L, number(result, "$.subscription.subscriptionId"));
        clock.setDate("2026-02-27");
        long id = refundRequest(number(result, "$.payment.paymentId"));
        decide(id, "REJECT").andExpect(status().isOk()).andExpect(jsonPath("$.amount").value(460))
                .andExpect(jsonPath("$.remainingDays").value(1));
    }

    @Test void rejectionKeepsPaymentAndSubscriptionAndCannotRequestAgain() throws Exception {
        var result = checkout("reject-flow").andReturn();
        long paymentId = number(result, "$.payment.paymentId");
        long refundId = refundRequest(paymentId);
        decide(refundId, "REJECT").andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REJECTED"));
        assertThat(paymentRepository.findById(paymentId).orElseThrow().getStatus().name()).isEqualTo("COMPLETED");
        mvc.perform(post("/api/refunds").with(user("user@submate.test")).contentType("application/json")
                .content("{\"paymentId\":" + paymentId + ",\"reason\":\"재요청\"}")).andExpect(status().isConflict());
        mvc.perform(get("/api/refunds").with(user("user@submate.test")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].status").value("REJECTED"));
    }

    @Test void foreignKeysPreventDeletingReferencedMembersAndProducts() throws Exception {
        subscribe();
        assertThatThrownBy(() -> jdbc.update("delete from members where member_id = 1")).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("delete from products where product_id = 1")).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThat(subscriptionRepository.count()).isEqualTo(1);
    }
}
