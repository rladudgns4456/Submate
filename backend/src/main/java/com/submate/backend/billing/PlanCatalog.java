package com.submate.backend.billing;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;

@Component
public class PlanCatalog {
    public record Plan(String id, String name, long price, int months, String description) {}
    public List<Plan> list() {
        return List.of(
            new Plan("basic", "베이직", 9900, 1, "가볍게 시작하는 월간 구독"),
            new Plan("premium", "프리미엄", 19900, 1, "더 풍부하게 즐기는 월간 구독"),
            new Plan("annual", "연간 멤버십", 99000, 12, "한 번의 결제로 일 년 동안"));
    }
    public Plan get(String id) {
        return list().stream().filter(p -> p.id().equals(id)).findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "요금제를 찾을 수 없습니다."));
    }
}
