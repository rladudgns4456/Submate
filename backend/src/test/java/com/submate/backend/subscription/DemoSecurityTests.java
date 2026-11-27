package com.submate.backend.subscription;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "SUBMATE_DEMO_PASSWORD=1234",
        "spring.datasource.url=jdbc:h2:mem:demo_security;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "submate.scheduler.enabled=false"
})
@ActiveProfiles("demo")
@AutoConfigureMockMvc
class DemoSecurityTests {
    @Autowired MockMvc mvc;
    @Test void demoBasicAuthenticationAndRolesWork() throws Exception {
        mvc.perform(get("/api/subscriptions")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/subscriptions").with(httpBasic("user@submate.test", "wrong")))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/subscriptions").with(httpBasic("user@submate.test", "1234")))
                .andExpect(status().isOk());
        mvc.perform(get("/api/admin/refunds").with(httpBasic("user@submate.test", "1234")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/refunds").with(httpBasic("admin@submate.test", "1234")))
                .andExpect(status().isOk());
    }
    @Test void loopbackFrontendCanMakeAuthenticatedPayment() throws Exception {
        mvc.perform(post("/api/subscriptions").with(httpBasic("user@submate.test", "1234"))
                .header("Origin", "http://127.0.0.1:5173").contentType("application/json")
                .content("{\"productId\":1,\"paymentMethod\":\"CARD\",\"requestKey\":\"cors-test\",\"simulateFailure\":false}"))
                .andExpect(status().isCreated());
    }

    @Test void adminWithdrawalPersistsAcrossRequests() throws Exception {
        var admin = httpBasic("admin@submate.test", "1234");
        mvc.perform(patch("/api/admin/members/2/withdraw").with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WITHDRAWN"));
        mvc.perform(get("/api/admin/members/2").with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WITHDRAWN"));
    }
}
