CREATE TABLE outage_report (
    id UUID PRIMARY KEY,
    report_code VARCHAR(30) NOT NULL UNIQUE,
    idempotency_key UUID NOT NULL UNIQUE,
    request_fingerprint VARCHAR(64) NOT NULL,
    customer_code VARCHAR(30) NOT NULL,
    service_point_code VARCHAR(30) NOT NULL,
    reporter_name VARCHAR(200) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    address VARCHAR(500) NOT NULL,
    description VARCHAR(1000),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_outage_report_created_at ON outage_report (created_at DESC);
CREATE INDEX idx_outage_report_status ON outage_report (status);
