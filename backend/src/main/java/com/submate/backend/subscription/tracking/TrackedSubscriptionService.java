package com.submate.backend.subscription.tracking;

import com.submate.backend.subscription.support.DomainException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrackedSubscriptionService {
    private final TrackedSubscriptionRepository repository;
    private final Clock clock;

    TrackedSubscriptionService(TrackedSubscriptionRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public record Entry(
            Long id, String name, long amount, BillingCycle billingCycle, LocalDate billingDate,
            boolean active, Integer reminderDays, LocalDate nextBillingDate, boolean reminderDue,
            boolean imported
    ) {
        static Entry from(TrackedSubscription item, LocalDate today) {
            LocalDate next = item.isActive()
                    ? item.getBillingCycle().nextOnOrAfter(item.getBillingDate(), today)
                    : null;
            boolean due = next != null && item.getReminderDays() != null
                    && !next.isAfter(today.plusDays(item.getReminderDays()));
            return new Entry(item.getId(), item.getName(), item.getAmount(), item.getBillingCycle(),
                    item.getBillingDate(), item.isActive(), item.getReminderDays(), next, due,
                    item.getReceiptId() != null);
        }
    }

    public record Dashboard(LocalDate today, YearMonth month, long monthlyTotal, List<Entry> subscriptions) {}

    @Transactional(readOnly = true)
    public Dashboard dashboard(long memberId) {
        LocalDate today = LocalDate.now(clock);
        YearMonth month = YearMonth.from(today);
        List<TrackedSubscription> items = repository.findAllByMemberId(memberId);
        long monthlyTotal = items.stream()
                .filter(TrackedSubscription::isActive)
                .filter(item -> month.equals(YearMonth.from(item.getBillingCycle()
                        .nextOnOrAfter(item.getBillingDate(), month.atDay(1)))))
                .mapToLong(TrackedSubscription::getAmount)
                .sum();
        List<Entry> entries = items.stream()
                .map(item -> Entry.from(item, today))
                .sorted(Comparator.comparing(Entry::nextBillingDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Entry::id))
                .toList();
        return new Dashboard(today, month, monthlyTotal, entries);
    }

    @Transactional
    public Entry create(long memberId, TrackedSubscriptionInput input, String receiptId) {
        if (receiptId != null && repository.existsByMemberIdAndReceiptId(memberId, receiptId)) {
            throw DomainException.conflict("이미 등록한 영수증입니다.");
        }
        TrackedSubscription item = repository.save(new TrackedSubscription(memberId, input, receiptId));
        return Entry.from(item, LocalDate.now(clock));
    }

    @Transactional
    public Entry update(long memberId, long id, TrackedSubscriptionInput input) {
        TrackedSubscription item = find(memberId, id);
        item.update(input);
        return Entry.from(item, LocalDate.now(clock));
    }

    @Transactional
    public void delete(long memberId, long id) {
        repository.delete(find(memberId, id));
    }

    private TrackedSubscription find(long memberId, long id) {
        return repository.findByIdAndMemberId(id, memberId)
                .orElseThrow(() -> DomainException.notFound("외부 구독"));
    }
}
