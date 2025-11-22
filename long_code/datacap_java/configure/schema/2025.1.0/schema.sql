USE
`datacap`;

DROP TABLE IF EXISTS `datacap_metadata_table_database_relation`;
DROP TABLE IF EXISTS `datacap_metadata_table`;
DROP TABLE IF EXISTS `datacap_metadata_database_source_relation`;
DROP TABLE IF EXISTS `datacap_metadata_database`;
DROP TABLE IF EXISTS `datacap_metadata_column_table_relation`;
DROP TABLE IF EXISTS `datacap_metadata_column`;
DROP TABLE IF EXISTS `datacap_template`;

DELETE
FROM `datacap_menu`
WHERE id = 11;

CREATE TABLE datacap_notification
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    name          VARCHAR(255) NULL,
    code          VARCHAR(255) NULL,
    active        BIT(1) NULL,
    create_time   datetime NULL,
    update_time   datetime NULL,
    content       VARCHAR(255) NOT NULL,
    type          VARCHAR(255) NULL,
    is_read       BIT(1)                DEFAULT 0 NOT NULL,
    user_id       BIGINT NULL,
    entity_type   VARCHAR(255) NULL,
    entity_code   VARCHAR(255) NULL,
    entity_name   VARCHAR(255) NULL,
    entity_exists BIT(1)       NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_datacap_notification PRIMARY KEY (id)
);

ALTER TABLE `datacap_notification`
    ADD CONSTRAINT FK_DATACAP_NOTIFICATION_ON_USER
        FOREIGN KEY (user_id)
            REFERENCES datacap_user (id)
            ON DELETE CASCADE;

ALTER TABLE `datacap_user`
    ADD NOTIFICATION_TYPES VARCHAR(200);
