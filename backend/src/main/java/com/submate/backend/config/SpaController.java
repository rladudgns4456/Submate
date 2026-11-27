package com.submate.backend.config;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Profile("production")
class SpaController {
    @GetMapping({
            "/", "/login", "/signup", "/tracker", "/products", "/subscriptions", "/subscriptions/{id}",
            "/subscribe/{id}", "/payment/result", "/payments", "/refunds", "/admin/subscriptions",
            "/admin/subscriptions/{id}", "/admin/payments", "/admin/refunds"
    })
    String index() {
        return "forward:/index.html";
    }
}
