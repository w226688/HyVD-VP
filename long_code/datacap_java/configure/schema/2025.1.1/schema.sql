USE
`datacap`;

ALTER TABLE `datacap_user`
    CHANGE notification_types notify_configure TEXT NULL;

DROP TABLE `datacap_pipeline`;
DROP TABLE `datacap_pipeline_user_relation`;

DELETE
FROM `datacap_menu`
WHERE id = 7;