package com.submate.backend;

import com.submate.backend.billing.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.context.annotation.*;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import java.net.*;
import java.net.http.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.jpa.show-sql=false")
@ActiveProfiles("dev")
@Import(BillingIntegrationTests.TimeConfiguration.class)
class BillingIntegrationTests {
    @Value("${local.server.port}") int port;
    @Autowired BillingService service;
    @Autowired SubscriptionRepository subscriptions;
    @Autowired PaymentRepository payments;
    @Autowired RefundRepository refunds;
    static final AtomicReference<Instant> NOW = new AtomicReference<>();
    final HttpClient client = HttpClient.newHttpClient();
    final JsonMapper mapper = JsonMapper.builder().build();

    @TestConfiguration
    static class TimeConfiguration {
        @Bean @Primary Clock testClock() {
            return new Clock() {
                public ZoneId getZone() { return ZoneId.of("Asia/Seoul"); }
                public Clock withZone(ZoneId zone) { return Clock.fixed(instant(), zone); }
                public Instant instant() { return NOW.get() == null ? Instant.parse("2026-01-31T03:00:00Z") : NOW.get(); }
            };
        }
    }
    @BeforeEach void reset() {
        refunds.deleteAll(); payments.deleteAll(); subscriptions.deleteAll();
        NOW.set(Instant.parse("2026-01-31T03:00:00Z"));
    }
    HttpResponse<String> call(String method, String path, String body, String user) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api" + path))
            .header("Content-Type", "application/json");
        if (user != null) builder.header("Authorization", "Basic " + Base64.getEncoder().encodeToString(
            (user + ":" + (user.equals("admin") ? "admin1234" : "demo1234")).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        return client.send(builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }
    JsonNode json(HttpResponse<String> response, int expected) {
        assertEquals(expected, response.statusCode(), response.body());
        return mapper.readTree(response.body());
    }
    JsonNode buy(String key, boolean success) throws Exception {
        return json(call("POST", "/subscriptions", "{\"planId\":\"basic\",\"requestKey\":\"" + key + "\",\"success\":" + success + "}", "demo"), 200);
    }

    @Test void paymentIsAtomicIdempotentAndOwned() throws Exception {
        var payment = buy("first", true);
        assertEquals("SUCCESS", payment.get("status").asText());
        long id = payment.get("subscriptionId").asLong();
        var s = json(call("GET", "/subscriptions/" + id, null, "demo"), 200);
        assertEquals("2026-02-28", s.get("endDate").asText());
        assertEquals(payment.get("id"), buy("first", true).get("id"));
        assertEquals(1, payments.count()); assertEquals(1, subscriptions.count());
        assertEquals(404, call("GET", "/subscriptions/" + id, null, "other").statusCode());
        assertEquals(404, call("POST", "/subscriptions/" + id + "/cancel", null, "other").statusCode());
        assertEquals(403, call("GET", "/admin/subscriptions", null, "demo").statusCode());
        assertEquals(401, call("GET", "/subscriptions", null, null).statusCode());
        assertEquals(200, call("GET", "/admin/subscriptions/" + id, null, "admin").statusCode());
        assertEquals(409, call("POST", "/subscriptions", "{\"planId\":\"basic\",\"requestKey\":\"duplicate\",\"success\":true}", "demo").statusCode());
        assertEquals(1, payments.count());
    }
    @Test void localBrowserOriginIsAllowed() throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/subscriptions"))
            .header("Origin", "http://127.0.0.1:5173")
            .header("Access-Control-Request-Method", "POST")
            .header("Access-Control-Request-Headers", "authorization,content-type")
            .method("OPTIONS", HttpRequest.BodyPublishers.noBody()).build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("http://127.0.0.1:5173", response.headers().firstValue("Access-Control-Allow-Origin").orElseThrow());
    }
    @Test void failedPaymentDoesNotCreateSubscriptionAndInputIsValidated() throws Exception {
        assertEquals("FAILED", buy("failed", false).get("status").asText());
        assertEquals(0, subscriptions.count());
        assertEquals(400, call("POST", "/subscriptions", "{}", "demo").statusCode());
        assertEquals(404, call("POST", "/subscriptions", "{\"planId\":\"unknown\",\"requestKey\":\"x\",\"success\":true}", "demo").statusCode());
    }
    @Test void cancellationPreservesAccessThenSchedulerExpiresAndAllowsNewPurchase() throws Exception {
        long id = buy("cancel", true).get("subscriptionId").asLong();
        var s = json(call("POST", "/subscriptions/" + id + "/cancel", null, "demo"), 200);
        assertEquals("CANCELLED", s.get("status").asText());
        assertEquals("2026-02-28", s.get("endDate").asText());
        assertEquals(409, call("PATCH", "/admin/subscriptions/" + id + "/status", "{\"status\":\"ACTIVE\"}", "admin").statusCode());
        NOW.set(Instant.parse("2026-02-27T15:00:00Z"));
        service.expireDue();
        assertEquals(Subscription.Status.EXPIRED, subscriptions.findById(id).orElseThrow().status);
        assertEquals("SUCCESS", buy("renew", true).get("status").asText());
    }
    @Test void proratedRefundUsesRequestDateAndApprovalEndsAccess() throws Exception {
        var p = buy("refund", true);
        NOW.set(Instant.parse("2026-02-10T03:00:00Z"));
        String body = "{\"paymentId\":" + p.get("id").asLong() + ",\"reason\":\"이용 계획 변경\"}";
        assertEquals(404, call("POST", "/refunds", body, "other").statusCode());
        var r = json(call("POST", "/refunds", body, "demo"), 200);
        assertEquals(6364, r.get("amount").asLong());
        assertEquals(18, r.get("remainingDays").asLong());
        assertEquals(28, r.get("totalDays").asLong());
        assertEquals(409, call("POST", "/refunds", body, "demo").statusCode());
        String endpoint = "/admin/refunds/" + r.get("id").asLong() + "/decision";
        assertEquals(403, call("POST", endpoint, "{\"approve\":true}", "demo").statusCode());
        NOW.set(Instant.parse("2026-02-11T03:00:00Z"));
        var approved = json(call("POST", endpoint, "{\"approve\":true}", "admin"), 200);
        assertEquals(6364, approved.get("amount").asLong());
        assertEquals("APPROVED", approved.get("status").asText());
        assertEquals(Payment.Status.REFUNDED, payments.findById(p.get("id").asLong()).orElseThrow().status);
        assertEquals(Subscription.Status.EXPIRED, subscriptions.findById(p.get("subscriptionId").asLong()).orElseThrow().status);
        assertEquals(409, call("POST", endpoint, "{\"approve\":true}", "admin").statusCode());
        assertEquals("SUCCESS", buy("after-refund", true).get("status").asText());
    }
    @Test void rejectedRefundKeepsPaymentAndSubscriptionAndExpiredRefundIsRejected() throws Exception {
        var p = buy("reject", true);
        var r = json(call("POST", "/refunds", "{\"paymentId\":" + p.get("id").asLong() + ",\"reason\":\"변경\"}", "demo"), 200);
        json(call("POST", "/admin/refunds/" + r.get("id").asLong() + "/decision", "{\"approve\":false,\"note\":\"반려 사유\"}", "admin"), 200);
        assertEquals(Payment.Status.SUCCESS, payments.findById(p.get("id").asLong()).orElseThrow().status);
        assertEquals(Subscription.Status.ACTIVE, subscriptions.findById(p.get("subscriptionId").asLong()).orElseThrow().status);
        NOW.set(Instant.parse("2026-02-28T03:00:00Z"));
        var newPayment = buy("next", true);
        NOW.set(Instant.parse("2026-03-28T03:00:00Z"));
        assertEquals(409, call("POST", "/refunds", "{\"paymentId\":" + newPayment.get("id").asLong() + ",\"reason\":\"만료 후\"}", "demo").statusCode());
    }
}
