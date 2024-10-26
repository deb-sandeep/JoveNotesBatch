ALTER TABLE `jove_notes`.`chapter_preparedness`
    ADD COLUMN `practice_level` VARCHAR(45) NULL DEFAULT NULL COMMENT 'null -> current, R1, R2, Rn -> Revision stages' AFTER `chapter_id`,
    CHANGE COLUMN `retention_score` `retention_score` DECIMAL(10,0) NOT NULL DEFAULT '0' AFTER `practice_level`;

ALTER TABLE `jove_notes`.`card_learning_summary`
    ADD COLUMN `predicted_outcome_next_attempt` TINYINT(1) NOT NULL DEFAULT b'1' AFTER `exam_preparedness_value`;

ALTER TABLE `jove_notes`.`card_rating_ex`
    ADD COLUMN `predicted_outcome` TINYINT(1) NULL DEFAULT b'0' AFTER `attempt_timestamp`;
