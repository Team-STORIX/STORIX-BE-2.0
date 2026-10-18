-- Flyway 도입 시점(2026-10-18) 운영 DB 스키마 (#386). 이미 있는 DB 에는 실행하지 않고 baseline 으로만 기록된다
-- 운영과 엔티티가 다른 곳(유령 컬럼, enum 대신 varchar 등)도 운영 그대로 둔다
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE `admin_notification_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `admin_notification_id` bigint NOT NULL,
  `attempts` int NOT NULL,
  `next_retry_at` datetime(6) DEFAULT NULL,
  `notification_id` bigint DEFAULT NULL,
  `sent_at` datetime(6) DEFAULT NULL,
  `status` enum('FAILED','PENDING','SENDING','SENT','SKIPPED') NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_notification_log_user` (`admin_notification_id`,`user_id`),
  KEY `idx_admin_notification_log_status` (`admin_notification_id`,`status`),
  KEY `idx_admin_notification_log_retry` (`status`,`next_retry_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `admin_notifications` (
  `admin_notification_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `assignee_admin_id` bigint DEFAULT NULL,
  `content` varchar(500) NOT NULL,
  `event_target_id` bigint DEFAULT NULL,
  `failed_count` int NOT NULL,
  `is_all_chunk_published` bit(1) NOT NULL,
  `last_broadcast_user_id` bigint DEFAULT NULL,
  `notification_type` enum('FEATURE_UPDATE','MARKETING','PRIVACY_UPDATE','TOS_UPDATE') NOT NULL,
  `scheduled_at` datetime(6) DEFAULT NULL,
  `send_type` enum('IMMEDIATE','SCHEDULED') NOT NULL,
  `sent_count` int NOT NULL,
  `skipped_count` int NOT NULL,
  `status` enum('CANCELED','FAILED','SCHEDULED','SENDING','SENT') NOT NULL,
  `target_audience` enum('ALL','EVENT_WINNERS','NEW_USERS') NOT NULL,
  `target_count` int NOT NULL,
  `link` varchar(1000) DEFAULT NULL,
  `target_type` enum('APP_EVENT','EXTERNAL','NONE') NOT NULL,
  `title` varchar(100) NOT NULL,
  PRIMARY KEY (`admin_notification_id`),
  KEY `idx_admin_notification_status_scheduled` (`status`,`scheduled_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `adult_verifications` (
  `adult_verification_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `expires_at` date DEFAULT NULL,
  `identity_verification_id` varchar(64) NOT NULL,
  `provider_transaction_id` varchar(128) DEFAULT NULL,
  `status` enum('ABANDONED','EXPIRED','PENDING','REVOKED','VERIFIED') NOT NULL,
  `user_id` bigint NOT NULL,
  `verified_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`adult_verification_id`),
  UNIQUE KEY `uk_adult_verification_identity_id` (`identity_verification_id`),
  KEY `idx_adult_verification_user_status` (`user_id`,`status`),
  KEY `idx_adult_verification_status_expires` (`status`,`expires_at`),
  KEY `idx_adult_verification_status_updated` (`status`,`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `app_event_attendance_rewards` (
  `app_event_id` bigint NOT NULL,
  `cumulative_tickets` int NOT NULL,
  `attended_days` int NOT NULL,
  PRIMARY KEY (`app_event_id`,`attended_days`),
  CONSTRAINT `FKcet2i82g5700yrtoy6mmwigne` FOREIGN KEY (`app_event_id`) REFERENCES `app_events` (`app_event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `app_event_promotion_types` (
  `app_event_id` bigint NOT NULL,
  `promotion_type` enum('BANNER','POPUP','PUSH') NOT NULL,
  PRIMARY KEY (`app_event_id`,`promotion_type`),
  CONSTRAINT `FKf7y3qdiyp85lje78l2aqcc5c9` FOREIGN KEY (`app_event_id`) REFERENCES `app_events` (`app_event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `app_event_winners` (
  `app_event_winner_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `app_event_id` bigint NOT NULL,
  `draw_order` int NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`app_event_winner_id`),
  UNIQUE KEY `uk_app_event_winner_user` (`app_event_id`,`user_id`),
  KEY `idx_app_event_winner_order` (`app_event_id`,`draw_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `app_events` (
  `app_event_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `assignee_admin_id` bigint DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  `event_type` enum('ATTENDANCE','GENERAL','STORY_CARD') NOT NULL,
  `end_at` datetime(6) NOT NULL,
  `has_winner` bit(1) NOT NULL,
  `name` varchar(100) NOT NULL,
  `start_at` datetime(6) NOT NULL,
  `page_key` varchar(50) DEFAULT NULL,
  `force_ended` bit(1) NOT NULL,
  PRIMARY KEY (`app_event_id`),
  KEY `idx_app_event_type_period` (`event_type`,`start_at`,`end_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `banned_words` (
  `banned_word_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `word` varchar(255) NOT NULL,
  PRIMARY KEY (`banned_word_id`),
  UNIQUE KEY `uk_banned_word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted` bit(1) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `message` text NOT NULL,
  `message_type` enum('ENTER','LEAVE','TALK') NOT NULL,
  `room_id` bigint NOT NULL,
  `sender_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_room_created` (`room_id`,`created_at`),
  KEY `idx_room_id_sender` (`room_id`,`id`,`sender_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `event_attendance_checks` (
  `attendance_check_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `app_event_id` bigint NOT NULL,
  `attended_on` date NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`attendance_check_id`),
  UNIQUE KEY `uk_attendance_check_event_user_date` (`app_event_id`,`user_id`,`attended_on`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `event_banners` (
  `event_banner_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `assignee_admin_id` bigint DEFAULT NULL,
  `banner_title` varchar(100) NOT NULL,
  `content_target_type` enum('APP_EVENT') NOT NULL,
  `display_end_at` datetime(6) NOT NULL,
  `display_start_at` datetime(6) NOT NULL,
  `image_object_key` varchar(512) NOT NULL,
  `status` enum('ACTIVE','ENDED','SCHEDULED') NOT NULL,
  `app_event_id` bigint DEFAULT NULL,
  PRIMARY KEY (`event_banner_id`),
  KEY `idx_event_banner_status_period` (`status`,`display_start_at`,`display_end_at`),
  KEY `idx_event_banner_app_event` (`app_event_id`),
  CONSTRAINT `FKamsw51c4kwx18ikkmexvy72gc` FOREIGN KEY (`app_event_id`) REFERENCES `app_events` (`app_event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `event_page_modal_confirmations` (
  `page_modal_confirmation_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `app_event_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`page_modal_confirmation_id`),
  UNIQUE KEY `uk_page_modal_confirmation_user_event` (`user_id`,`app_event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `event_popup_dismisses` (
  `event_popup_dismiss_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `dismissed_on` date NOT NULL,
  `is_permanent` bit(1) NOT NULL,
  `user_id` bigint NOT NULL,
  `popup_id` bigint NOT NULL,
  PRIMARY KEY (`event_popup_dismiss_id`),
  UNIQUE KEY `uk_popup_dismiss_user_popup` (`user_id`,`popup_id`),
  KEY `FKd55gvyql4ve87fsg7rh34h8dn` (`popup_id`),
  CONSTRAINT `FKd55gvyql4ve87fsg7rh34h8dn` FOREIGN KEY (`popup_id`) REFERENCES `event_popups` (`event_popup_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `event_popups` (
  `event_popup_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `assignee_admin_id` bigint DEFAULT NULL,
  `content` varchar(500) DEFAULT NULL,
  `content_target_type` enum('APP_EVENT') NOT NULL,
  `cta_text` varchar(40) DEFAULT NULL,
  `display_end_at` datetime(6) NOT NULL,
  `display_start_at` datetime(6) NOT NULL,
  `exposure_policy` enum('ALWAYS_DURING_PERIOD','ONCE_PER_DAY') NOT NULL,
  `image_object_key` varchar(512) NOT NULL,
  `popup_title` varchar(100) NOT NULL,
  `status` enum('ACTIVE','ENDED','SCHEDULED') NOT NULL,
  `app_event_id` bigint DEFAULT NULL,
  PRIMARY KEY (`event_popup_id`),
  KEY `idx_event_popup_status_period` (`status`,`display_start_at`,`display_end_at`),
  KEY `idx_event_popup_app_event` (`app_event_id`),
  CONSTRAINT `FKbl9wg4aek3b0448rpjm4nqdot` FOREIGN KEY (`app_event_id`) REFERENCES `app_events` (`app_event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `event_story_card_draws` (
  `story_card_draw_id` bigint NOT NULL AUTO_INCREMENT,
  `app_event_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `drawn_on` date NOT NULL COMMENT '06:00 초기화 기준 서비스 날짜',
  `genre` varchar(20) NOT NULL COMMENT '배정된 대표 장르. 카드 이미지가 이 값으로 결정됨',
  `message` varchar(200) NOT NULL COMMENT '뽑힌 한마디 (줄바꿈 포함 원문)',
  `immersion` varchar(100) NOT NULL COMMENT '뽑힌 몰입력',
  `lucky_work_title` varchar(100) NOT NULL,
  `lucky_work_type` varchar(20) NOT NULL,
  `lucky_work_platform` varchar(20) NOT NULL,
  `lucky_work_landing_url` varchar(500) DEFAULT NULL,
  `drawn_at` datetime(6) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `lucky_work_id` bigint NOT NULL,
  PRIMARY KEY (`story_card_draw_id`),
  UNIQUE KEY `uk_story_card_draw_event_user_date` (`app_event_id`,`user_id`,`drawn_on`),
  KEY `idx_story_card_draw_drawn_on` (`drawn_on`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='스토리 카드 - 당일 뽑기 결과 (매일 06:00 purge)';
CREATE TABLE `event_story_card_immersions` (
  `story_card_immersion_id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(100) NOT NULL,
  `is_active` bit(1) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`story_card_immersion_id`),
  UNIQUE KEY `uk_story_card_immersion_content` (`content`),
  KEY `idx_story_card_immersion_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='스토리 카드 - 오늘의 몰입력 풀';
CREATE TABLE `event_story_card_messages` (
  `story_card_message_id` bigint NOT NULL AUTO_INCREMENT,
  `genre` varchar(20) NOT NULL COMMENT 'Genre dbValue (한글). 예: 판타지',
  `content` varchar(200) NOT NULL COMMENT '오늘의 한마디. 줄바꿈 포함 원문',
  `is_active` bit(1) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`story_card_message_id`),
  UNIQUE KEY `uk_story_card_message_genre_content` (`genre`,`content`),
  KEY `idx_story_card_message_genre_active` (`genre`,`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='스토리 카드 - 오늘의 한마디 풀';
CREATE TABLE `feed_reply_report` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `reply_id` bigint DEFAULT NULL,
  `report_case_id` bigint DEFAULT NULL,
  `reported_user_id` bigint DEFAULT NULL,
  `reporter_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_feed_report_reporter_reply` (`reporter_id`,`reply_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `feed_report` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `board_id` bigint DEFAULT NULL,
  `report_case_id` bigint DEFAULT NULL,
  `reported_user_id` bigint DEFAULT NULL,
  `reporter_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_feed_report_reporter_board` (`reporter_id`,`board_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `hashtag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKtnicok67w95ajkoau49jeg9fm` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `libraries` (
  `user_id` bigint NOT NULL,
  `board_count` int NOT NULL,
  `review_count` int NOT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `notification` (
  `notification_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `content` varchar(200) NOT NULL,
  `is_read` bit(1) NOT NULL,
  `notification_type` enum('COMMENT_ON_FEED','FEATURE_UPDATE','HOT_TOPIC_ROOM','LIKE_COMMENT','LIKE_FEED','LIKE_REVIEW','MARKETING','PRIVACY_UPDATE','REPLY_ON_COMMENT','REPORT_PROCESSED','REPORT_RECEIVED','RESTRICTION_30D','RESTRICTION_7D','TODAY_FEED','TOS_UPDATE') NOT NULL,
  `parent_target_id` bigint DEFAULT NULL,
  `target_id` bigint DEFAULT NULL,
  `link` varchar(1000) DEFAULT NULL,
  `target_type` enum('APP_EVENT','COMMENT','EXTERNAL','FEED','NONE','REVIEW','TOPIC_ROOM') NOT NULL,
  `title` varchar(200) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`notification_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `notification_settings` (
  `user_id` bigint NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `comment_on_feed_enabled` bit(1) NOT NULL,
  `hot_topic_room_enabled` bit(1) NOT NULL,
  `like_comment_enabled` bit(1) NOT NULL,
  `like_feed_enabled` bit(1) NOT NULL,
  `like_review_enabled` bit(1) NOT NULL,
  `marketing_enabled` bit(1) NOT NULL,
  `operation_policy_enabled` bit(1) NOT NULL,
  `reply_on_comment_enabled` bit(1) NOT NULL,
  `today_feed_enabled` bit(1) NOT NULL,
  `topic_room_chat_enabled` bit(1) NOT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `push_devices` (
  `push_device_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `app_version` varchar(32) NOT NULL,
  `device_model` varchar(64) NOT NULL,
  `fcm_token` varchar(512) NOT NULL,
  `installation_id` varchar(64) NOT NULL,
  `is_active` bit(1) NOT NULL,
  `last_success_at` datetime(6) DEFAULT NULL,
  `last_synced_at` datetime(6) DEFAULT NULL,
  `os_platform` enum('ANDROID','IOS') NOT NULL,
  `os_version` varchar(32) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`push_device_id`),
  UNIQUE KEY `uk_push_device_installation` (`user_id`,`installation_id`),
  KEY `idx_push_device_user_active` (`user_id`,`is_active`),
  KEY `idx_push_device_fcm_token` (`fcm_token`),
  KEY `idx_push_device_installation` (`installation_id`,`is_active`),
  KEY `idx_push_device_active_synced` (`is_active`,`last_synced_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `reader_board` (
  `reader_board_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `content` varchar(300) NOT NULL,
  `deleted` bit(1) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `deleted_by` enum('ADMIN','USER') DEFAULT NULL,
  `is_works_selected` bit(1) DEFAULT NULL,
  `like_count` int NOT NULL,
  `reply_count` int NOT NULL,
  `user_id` bigint DEFAULT NULL,
  `works_id` bigint DEFAULT NULL,
  `is_spoiler` bit(1) DEFAULT NULL,
  `popularity_score` int DEFAULT NULL,
  `spoiler_script` varchar(255) DEFAULT NULL,
  `theme` enum('BIRTHDAY') DEFAULT NULL,
  `bookmark_count` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`reader_board_id`),
  KEY `idx_reader_board_user_id` (`user_id`),
  KEY `idx_reader_board_popularity` (`popularity_score`),
  KEY `idx_reader_board_deleted_created` (`deleted`,`created_at`),
  KEY `idx_reader_board_works_deleted` (`works_id`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `reader_board_bookmark` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `reader_board_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_reader_board_bookmark` (`user_id`,`reader_board_id`),
  KEY `FKn185wjiqnsmlsku9c5iri7uax` (`reader_board_id`),
  CONSTRAINT `FKn185wjiqnsmlsku9c5iri7uax` FOREIGN KEY (`reader_board_id`) REFERENCES `reader_board` (`reader_board_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `reader_board_image` (
  `reader_board_image_id` bigint NOT NULL AUTO_INCREMENT,
  `image_object_key` varchar(255) NOT NULL,
  `sort_order` int NOT NULL,
  `reader_board_id` bigint DEFAULT NULL,
  PRIMARY KEY (`reader_board_image_id`),
  KEY `FK9251o8x7mts6sg50e0i33fj9x` (`reader_board_id`),
  CONSTRAINT `FK9251o8x7mts6sg50e0i33fj9x` FOREIGN KEY (`reader_board_id`) REFERENCES `reader_board` (`reader_board_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `reader_board_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `reader_board_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_reader_board_like` (`user_id`,`reader_board_id`),
  KEY `FKsg2krkv0219jpnw1cn7vbrjfq` (`reader_board_id`),
  CONSTRAINT `FKsg2krkv0219jpnw1cn7vbrjfq` FOREIGN KEY (`reader_board_id`) REFERENCES `reader_board` (`reader_board_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `reader_board_reply` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `child_reply_count` int NOT NULL,
  `comment` varchar(300) NOT NULL,
  `deleted` bit(1) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `deleted_by` enum('ADMIN','USER') DEFAULT NULL,
  `depth` int NOT NULL,
  `like_count` int NOT NULL,
  `user_id` bigint NOT NULL,
  `reader_board_id` bigint NOT NULL,
  `parent_reply_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_reader_board_reply_board_id` (`reader_board_id`),
  KEY `idx_reader_board_reply_user_id` (`user_id`),
  KEY `idx_reader_board_reply_parent_id` (`parent_reply_id`),
  CONSTRAINT `FKaudk4b1hwhjxids4tl4aoke0w` FOREIGN KEY (`reader_board_id`) REFERENCES `reader_board` (`reader_board_id`),
  CONSTRAINT `FKr197309qah27f4a984erbvay6` FOREIGN KEY (`parent_reply_id`) REFERENCES `reader_board_reply` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `reader_board_reply_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `reader_board_reply_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_reader_board_reply_like` (`user_id`,`reader_board_reply_id`),
  KEY `FKidwc3lwuw8oso3bmhhyq0vtvv` (`reader_board_reply_id`),
  CONSTRAINT `FKidwc3lwuw8oso3bmhhyq0vtvv` FOREIGN KEY (`reader_board_reply_id`) REFERENCES `reader_board_reply` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `report_case` (
  `report_case_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `process_memo` varchar(500) DEFAULT NULL,
  `processed_at` datetime(6) DEFAULT NULL,
  `processed_by_admin_id` bigint DEFAULT NULL,
  `reported_user_id` bigint NOT NULL,
  `status` enum('COMPLETED','RECEIVED','REJECTED') NOT NULL,
  `target_id` bigint NOT NULL,
  `target_type` enum('CHAT','FEED','FEED_REPLY','REVIEW','TOPIC_ROOM') NOT NULL,
  PRIMARY KEY (`report_case_id`),
  UNIQUE KEY `uk_report_case_target_reported_user` (`target_type`,`target_id`,`reported_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `report_case_action` (
  `report_case_id` bigint NOT NULL,
  `process_action` enum('ACCOUNT_DELETED','ACCOUNT_SUSPENDED','CONTENT_DELETED') DEFAULT NULL,
  KEY `FKcyupbm7cjnb2w0kb1n6ttifu4` (`report_case_id`),
  CONSTRAINT `FKcyupbm7cjnb2w0kb1n6ttifu4` FOREIGN KEY (`report_case_id`) REFERENCES `report_case` (`report_case_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `review` (
  `review_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `content` varchar(500) NOT NULL,
  `deleted` bit(1) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `deleted_by` enum('ADMIN','USER') DEFAULT NULL,
  `is_spoiler` bit(1) DEFAULT NULL,
  `library_user_id` bigint NOT NULL,
  `like_count` int NOT NULL,
  `rating` decimal(2,1) NOT NULL,
  `spoiler_script` varchar(255) DEFAULT NULL,
  `works_id` bigint NOT NULL,
  PRIMARY KEY (`review_id`),
  UNIQUE KEY `uk_review_library_works` (`library_user_id`,`works_id`),
  KEY `idx_review_works_deleted` (`works_id`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `review_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `review_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_review_like` (`user_id`,`review_id`),
  KEY `FK68am9vk1s1e8n1v873meqkk0k` (`review_id`),
  CONSTRAINT `FK68am9vk1s1e8n1v873meqkk0k` FOREIGN KEY (`review_id`) REFERENCES `review` (`review_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `review_report` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `other_reason` varchar(100) DEFAULT NULL,
  `reason` enum('ABUSE','DEFAULT','OTHER','SPAM') DEFAULT NULL,
  `report_case_id` bigint DEFAULT NULL,
  `reported_user_id` bigint DEFAULT NULL,
  `reporter_id` bigint DEFAULT NULL,
  `review_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_review_report_reporter_review` (`reporter_id`,`review_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `taste_exploration` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `is_liked` bit(1) NOT NULL,
  `user_id` bigint NOT NULL,
  `works_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_works` (`user_id`,`works_id`),
  KEY `idx_user_liked` (`user_id`,`is_liked`),
  KEY `idx_taste_exploration_works` (`works_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `terms` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `announced_at` date DEFAULT NULL,
  `content` mediumtext,
  `effective_from` date NOT NULL,
  `effective_to` date DEFAULT NULL,
  `is_required` bit(1) NOT NULL,
  `terms_type` enum('PRIVACY','SERVICE') NOT NULL,
  `title` varchar(255) NOT NULL,
  `version` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_terms_type_version` (`terms_type`,`version`),
  KEY `idx_terms_type_effective` (`terms_type`,`effective_from`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `topic_room` (
  `topic_room_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `active_user_number` int NOT NULL,
  `activity_score` double NOT NULL,
  `last_chat_time` datetime(6) DEFAULT NULL,
  `popularity_growth_rate` double NOT NULL,
  `popularity_score` double NOT NULL,
  `previous_active_user_number` int NOT NULL,
  `topic_room_name` varchar(255) NOT NULL,
  `works_id` bigint NOT NULL,
  `last_message_id` bigint DEFAULT NULL,
  PRIMARY KEY (`topic_room_id`),
  UNIQUE KEY `uk_topic_room_works_id` (`works_id`),
  KEY `idx_popularity_last_chat` (`popularity_score`,`last_chat_time`),
  KEY `idx_growth_rate` (`popularity_growth_rate`),
  KEY `idx_active_user_last_chat` (`active_user_number`,`last_chat_time`),
  KEY `idx_activity_score_last_chat` (`activity_score`,`last_chat_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `topic_room_report` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `chat_message_id` bigint NOT NULL,
  `other_reason` varchar(100) DEFAULT NULL,
  `reason` enum('ABUSE','DEFAULT','OTHER','SPAM') DEFAULT NULL,
  `report_case_id` bigint DEFAULT NULL,
  `reported_user_id` bigint DEFAULT NULL,
  `reporter_id` bigint DEFAULT NULL,
  `topic_room_id` bigint DEFAULT NULL,
  `chat_message_key` bigint GENERATED ALWAYS AS (coalesce(`chat_message_id`,0)) VIRTUAL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_topic_room_report_reporter_reported_room_message` (`reporter_id`,`reported_user_id`,`topic_room_id`,`chat_message_id`),
  UNIQUE KEY `uk_topic_room_report` (`reporter_id`,`topic_room_id`,`chat_message_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `topic_room_user` (
  `topic_room_user_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `role` enum('HOST','MEMBER') DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `topic_room_id` bigint DEFAULT NULL,
  `last_read_message_id` bigint DEFAULT NULL,
  `notification_enabled` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`topic_room_user_id`),
  UNIQUE KEY `uk_topic_room_user_id` (`user_id`,`topic_room_id`),
  KEY `FKilwgpq2wmd6jww6lxtq1kj7s` (`topic_room_id`),
  CONSTRAINT `FKilwgpq2wmd6jww6lxtq1kj7s` FOREIGN KEY (`topic_room_id`) REFERENCES `topic_room` (`topic_room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `user_app_events` (
  `user_app_event_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `acked_at` datetime(6) DEFAULT NULL,
  `event_type` enum('TITLE_ACQUIRED') NOT NULL,
  `occurred_at` datetime(6) NOT NULL,
  `payload_json` varchar(1000) DEFAULT NULL,
  `status` enum('ACK','PENDING') NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`user_app_event_id`),
  KEY `idx_user_app_event_user_status` (`user_id`,`status`,`user_app_event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `user_block` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `blocked_user_id` bigint NOT NULL,
  `blocker_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_block_blocker_blocked` (`blocker_id`,`blocked_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `user_favorite_genre` (
  `user_id` bigint NOT NULL,
  `genre` enum('ACTION','BL','DAILY','DRAMA','FANTASY','GAG','HISTORICAL','MODERN_FANTASY','ROFAN','ROMANCE','SENTIMENTAL','SPORTS','THRILLER') DEFAULT NULL,
  KEY `FKb014uwxh0n76qdexd2s6k0ats` (`user_id`),
  CONSTRAINT `FKb014uwxh0n76qdexd2s6k0ats` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `user_favorite_works` (
  `favorite_works_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `works_id` bigint NOT NULL,
  PRIMARY KEY (`favorite_works_id`),
  UNIQUE KEY `uk_favorite_user_work` (`user_id`,`works_id`),
  KEY `idx_favorite_user` (`user_id`),
  KEY `idx_favorite_work` (`works_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `user_genre_raw_score` (
  `genre` enum('ACTION','BL','DAILY','DRAMA','FANTASY','GAG','HISTORICAL','MODERN_FANTASY','ROFAN','ROMANCE','SENTIMENTAL','SPORTS','THRILLER') NOT NULL,
  `user_id` bigint NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `raw_score` bigint NOT NULL,
  PRIMARY KEY (`genre`,`user_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `user_genre_score_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `event_type` enum('BOARD_WRITE','FAVORITE_WORKS_ADD','FAVORITE_WORKS_REMOVE','ONBOARDING_SELECT','REVIEW_WRITE_POSITIVE','TOPIC_ROOM_JOIN') NOT NULL,
  `genre` enum('ACTION','BL','DAILY','DRAMA','FANTASY','GAG','HISTORICAL','MODERN_FANTASY','ROFAN','ROMANCE','SENTIMENTAL','SPORTS','THRILLER') NOT NULL,
  `processed_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `weight` int NOT NULL,
  `works_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_unprocessed` (`processed_at`,`created_at`),
  KEY `idx_user_event` (`user_id`,`event_type`,`works_id`),
  KEY `idx_user_genre_created` (`user_id`,`genre`,`created_at`),
  KEY `idx_user_genre_score_log_works` (`works_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `user_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `detail` varchar(255) DEFAULT NULL,
  `history_type` enum('MARKETING_AGREE','MARKETING_REJECT','WITHDRAW') NOT NULL,
  `processed_at` datetime(6) NOT NULL,
  `processor` varchar(50) NOT NULL,
  `reason` enum('HARD_TO_USE','LACK_OF_CONTENT','LOW_USAGE','NOT_SUITABLE_COMMUNITY','OTHER') DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_created` (`user_id`,`created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `user_sanction_history` (
  `user_sanction_history_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `admin_id` bigint DEFAULT NULL,
  `ended_at` datetime(6) DEFAULT NULL,
  `memo` varchar(500) DEFAULT NULL,
  `report_case_id` bigint DEFAULT NULL,
  `source` enum('MANUAL','REPORT') NOT NULL,
  `started_at` datetime(6) NOT NULL,
  `type` enum('CONTENT_DELETED','RESTORED','SUSPENDED','WITHDRAWN') NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`user_sanction_history_id`),
  KEY `idx_user_sanction_user_created` (`user_id`,`created_at` DESC),
  KEY `idx_user_sanction_report_case` (`report_case_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `user_term_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `agreed_at` datetime(6) DEFAULT NULL,
  `is_agreed` bit(1) NOT NULL,
  `user_id` bigint NOT NULL,
  `withdrawn_at` datetime(6) DEFAULT NULL,
  `terms_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_term_user` (`user_id`),
  KEY `idx_user_term_terms` (`terms_id`),
  CONSTRAINT `fk_user_term_history_terms` FOREIGN KEY (`terms_id`) REFERENCES `terms` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `user_title_history` (
  `user_title_history_id` bigint NOT NULL AUTO_INCREMENT,
  `acquired_at` datetime(6) NOT NULL,
  `title` enum('BL_ENTRY','BL_EXPLORE','BL_IMMERSE','BL_NONE','DAILY_ENTRY','DAILY_EXPLORE','DAILY_IMMERSE','DAILY_NONE','DRAMA_ENTRY','DRAMA_EXPLORE','DRAMA_IMMERSE','DRAMA_NONE','FANTASY_ENTRY','FANTASY_EXPLORE','FANTASY_IMMERSE','FANTASY_NONE','HISTORICAL_ENTRY','HISTORICAL_EXPLORE','HISTORICAL_IMMERSE','HISTORICAL_NONE','MODERN_FANTASY_ENTRY','MODERN_FANTASY_EXPLORE','MODERN_FANTASY_IMMERSE','MODERN_FANTASY_NONE','ROFAN_ENTRY','ROFAN_EXPLORE','ROFAN_IMMERSE','ROFAN_NONE','ROMANCE_ENTRY','ROMANCE_EXPLORE','ROMANCE_IMMERSE','ROMANCE_NONE','THRILLER_ENTRY','THRILLER_EXPLORE','THRILLER_IMMERSE','THRILLER_NONE') NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`user_title_history_id`),
  UNIQUE KEY `uk_user_title_history` (`user_id`,`title`),
  KEY `idx_user_title_history_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `users` (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `account_state` enum('DELETED','NORMAL','SUSPENDED') NOT NULL,
  `age_over_14` bit(1) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `last_login_at` datetime(6) DEFAULT NULL,
  `nick_name` varchar(64) NOT NULL,
  `oauth_email` varchar(255) DEFAULT NULL,
  `oauth_refresh_token` varchar(2048) DEFAULT NULL,
  `oauth_oid` varchar(255) DEFAULT NULL,
  `oauth_provider` enum('APPLE','KAKAO','NAVER','SLACK','X') DEFAULT NULL,
  `password` varchar(100) DEFAULT NULL,
  `profile_description` varchar(30) DEFAULT NULL,
  `profile_object_key` varchar(255) DEFAULT NULL,
  `role` enum('ADMIN','READER','TESTER') NOT NULL,
  `suspended_until` datetime(6) DEFAULT NULL,
  `title` enum('BL_ENTRY','BL_EXPLORE','BL_IMMERSE','BL_NONE','DAILY_ENTRY','DAILY_EXPLORE','DAILY_IMMERSE','DAILY_NONE','DRAMA_ENTRY','DRAMA_EXPLORE','DRAMA_IMMERSE','DRAMA_NONE','FANTASY_ENTRY','FANTASY_EXPLORE','FANTASY_IMMERSE','FANTASY_NONE','HISTORICAL_ENTRY','HISTORICAL_EXPLORE','HISTORICAL_IMMERSE','HISTORICAL_NONE','MODERN_FANTASY_ENTRY','MODERN_FANTASY_EXPLORE','MODERN_FANTASY_IMMERSE','MODERN_FANTASY_NONE','ROFAN_ENTRY','ROFAN_EXPLORE','ROFAN_IMMERSE','ROFAN_NONE','ROMANCE_ENTRY','ROMANCE_EXPLORE','ROMANCE_IMMERSE','ROMANCE_NONE','THRILLER_ENTRY','THRILLER_EXPLORE','THRILLER_IMMERSE','THRILLER_NONE') DEFAULT NULL,
  `is_library_public` bit(1) NOT NULL DEFAULT b'1',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_nick_name` (`nick_name`),
  UNIQUE KEY `uk_oauth_provider_oid` (`oauth_provider`,`oauth_oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `works` (
  `works_id` bigint NOT NULL AUTO_INCREMENT,
  `age_classification` varchar(255) NOT NULL,
  `artist_name` varchar(255) NOT NULL,
  `author` varchar(100) DEFAULT NULL,
  `avg_rating` double NOT NULL DEFAULT '0',
  `description` text NOT NULL,
  `genre` varchar(255) NOT NULL,
  `illustrator` varchar(100) DEFAULT NULL,
  `is_onboarding` bit(1) DEFAULT NULL,
  `original_author` varchar(100) DEFAULT NULL,
  `reviews_count` int DEFAULT NULL,
  `thumbnail_url` varchar(500) NOT NULL,
  `works_name` varchar(255) NOT NULL,
  `works_type` varchar(255) NOT NULL,
  `is_story_card_lucky_work` bit(1) DEFAULT NULL COMMENT '오늘의 스토리 카드 행운의 작품 대상 여부',
  `normalized_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`works_id`),
  KEY `idx_works_author` (`author`),
  KEY `idx_works_illustrator` (`illustrator`),
  KEY `idx_works_original_author` (`original_author`),
  KEY `idx_works_normalized_name_type` (`normalized_name`,`works_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `works_hashtag` (
  `works_id` bigint NOT NULL,
  `hashtag_id` bigint NOT NULL,
  PRIMARY KEY (`works_id`,`hashtag_id`),
  KEY `FK4mk4ussttyntse5lx0w385uj` (`hashtag_id`),
  CONSTRAINT `FK4mk4ussttyntse5lx0w385uj` FOREIGN KEY (`hashtag_id`) REFERENCES `hashtag` (`id`),
  CONSTRAINT `FKsp3ncu1lr6k1ifnhg28ba9gew` FOREIGN KEY (`works_id`) REFERENCES `works` (`works_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `works_nickname` (
  `works_nickname_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `nickname` varchar(100) NOT NULL,
  `normalized` varchar(100) NOT NULL,
  `works_id` bigint NOT NULL,
  PRIMARY KEY (`works_nickname_id`),
  UNIQUE KEY `uk_works_nickname` (`works_id`,`normalized`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `works_platform` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `platform` varchar(255) NOT NULL,
  `works_id` bigint NOT NULL,
  `landing_url` varchar(500) DEFAULT NULL COMMENT '해당 플랫폼의 작품 페이지 외부 링크. 미확보 시 NULL',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKqmp9dxs9x41eyxnedgmmnqubi` (`works_id`,`platform`),
  CONSTRAINT `FK3jf5rg9d7kslwrlilvk2rlnvs` FOREIGN KEY (`works_id`) REFERENCES `works` (`works_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


SET FOREIGN_KEY_CHECKS = 1;
