INSERT INTO tracked_subscriptions (member_id, name, amount, billing_cycle, billing_date, active, reminder_days)
VALUES
    (1, '샘플 영상 스트리밍', 12900, 'MONTHLY',
     CAST(CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Seoul' AS DATE), TRUE, 0),
    (1, '샘플 클라우드 저장공간', 24000, 'YEARLY',
     DATEADD('DAY', 3, CAST(CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Seoul' AS DATE)), TRUE, 7),
    (1, '샘플 음악 스트리밍', 8900, 'MONTHLY',
     CAST(CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Seoul' AS DATE), FALSE, NULL);
