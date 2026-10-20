package com.submate.backend.subscription.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/** 로컬 통합 검증 전용. 일반 실행에서는 이 인증 방식과 샘플 데이터가 활성화되지 않는다. */
@Configuration
@Profile("demo")
public class DemoConfiguration {
    @Bean
    public UserDetailsService demoUsers(@Value("${SUBMATE_DEMO_PASSWORD}") String password) {
        if (password.length() < 12) throw new IllegalArgumentException("SUBMATE_DEMO_PASSWORD는 12자 이상이어야 합니다.");
        String encoded = new BCryptPasswordEncoder().encode(password);
        return new InMemoryUserDetailsManager(
                User.withUsername("user@submate.test").password(encoded).roles("USER").build(),
                User.withUsername("other@submate.test").password(encoded).roles("USER").build(),
                User.withUsername("admin@submate.test").password(encoded).roles("ADMIN").build());
    }

    @Bean @Order(1)
    public SecurityFilterChain demoSecurity(HttpSecurity http, @Qualifier("corsConfigurationSource") CorsConfigurationSource cors) throws Exception {
        return http.securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .cors(config -> config.configurationSource(cors))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(errors -> errors.authenticationEntryPoint((request, response, ex) -> {
                    response.setStatus(401);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"status\":401,\"message\":\"로그인이 필요합니다.\"}");
                }))
                .build();
    }
}
