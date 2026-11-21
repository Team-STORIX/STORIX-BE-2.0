-- AI 작품 검색 대화 · 챗봇 설정 (#451). 대화방은 사용자당 하나
CREATE TABLE `ai_chats` (
  `ai_chat_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `last_message_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`ai_chat_id`),
  UNIQUE KEY `uk_ai_chat_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `ai_chat_messages` (
  `ai_chat_message_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `ai_chat_id` bigint NOT NULL,
  `role` enum('USER','CHATBOT') NOT NULL,
  `type` enum('TEXT','ONBOARDING') NOT NULL,
  `content` text NOT NULL,
  `status` enum('OK','NO_RESULT','WORK_NOT_FOUND','OFF_TOPIC','ERROR') DEFAULT NULL,
  `works_json` text DEFAULT NULL,
  `pipeline_version` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`ai_chat_message_id`),
  KEY `idx_ai_chat_message_chat` (`ai_chat_id`,`ai_chat_message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `ai_chat_settings` (
  `ai_chat_setting_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `adult_enabled` bit(1) NOT NULL,
  `works_types` varchar(50) NOT NULL,
  `onboarding_version` int DEFAULT NULL,
  PRIMARY KEY (`ai_chat_setting_id`),
  UNIQUE KEY `uk_ai_chat_setting_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
