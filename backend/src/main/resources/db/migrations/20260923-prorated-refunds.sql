-- Apply once to an existing MySQL database before starting the updated application.
-- Historical requests retain their previously agreed amount and have no day snapshot.
ALTER TABLE refunds
    ADD COLUMN total_days BIGINT NULL,
    ADD COLUMN remaining_days BIGINT NULL;
