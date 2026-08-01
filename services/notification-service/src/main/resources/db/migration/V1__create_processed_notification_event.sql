CREATE TABLE IF NOT EXISTS processed_notification_event (
    event_id UUID PRIMARY KEY,
    report_code VARCHAR(30) NOT NULL,
    partner_message_id VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_processed_notification_event_processed_at
    ON processed_notification_event (processed_at DESC);
