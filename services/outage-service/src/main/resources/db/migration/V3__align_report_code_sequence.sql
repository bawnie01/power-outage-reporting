DO $$
DECLARE
    max_sequence BIGINT;
BEGIN
    SELECT COALESCE(MAX(SUBSTRING(report_code FROM '([0-9]+)$')::BIGINT), 0)
    INTO max_sequence
    FROM outage_report;

    IF max_sequence = 0 THEN
        PERFORM setval('outage_report_code_seq', 1, false);
    ELSE
        PERFORM setval('outage_report_code_seq', max_sequence, true);
    END IF;
END $$;
