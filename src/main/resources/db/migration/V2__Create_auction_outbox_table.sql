CREATE TABLE `auction_outbox`
(
    `id`             BIGINT       NOT NULL AUTO_INCREMENT,
    `aggregate_type` VARCHAR(100) NOT NULL,
    `auction_id`     BIGINT       NOT NULL,
    `event_type`     VARCHAR(50)  NOT NULL,
    `event_status`   VARCHAR(50)  NOT NULL,
    `retry_count`    BIGINT       NOT NULL DEFAULT 0,
    `created_at`     DATETIME(6)  NOT NULL,
    `updated_at`     DATETIME(6),
    `processed_at`   DATETIME(6),
    PRIMARY KEY (`id`)
);