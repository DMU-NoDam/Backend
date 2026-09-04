DROP DATABASE IF EXISTS `NoDam`;
CREATE DATABASE `NoDam` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `NoDam`;

CREATE TABLE `region` (
  `lat` double DEFAULT NULL,
  `lon` double DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `super_id` bigint DEFAULT NULL,
  `code` varchar(15) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9gfnyhv4xb902fe9jiuad22fb` (`super_id`),
  CONSTRAINT `FK9gfnyhv4xb902fe9jiuad22fb` FOREIGN KEY (`super_id`) REFERENCES `region` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `place` (
  `is_deleted` bit(1) NOT NULL,
  `lat` double NOT NULL,
  `lon` double NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `crawl_id` bigint DEFAULT NULL,
  `region_id` bigint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `address` varchar(255) NOT NULL,
  `google_id` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `place_type` enum('AIRPORT','CAFE','HOTEL','RESTAURANT','SHOP','SIGHT') DEFAULT NULL,
  `price_type` enum('CHEEP','LUXURY','NORMAL') DEFAULT NULL,
  `recommend_season_type` enum('FALL','SPRING','SUMMER','WINTER') DEFAULT NULL,
  `recommend_trip_theme_type` enum('FOOD','HEALING','LANDMARK') DEFAULT NULL,
  `recommend_weather_type` enum('RAINY','SNOWY','SUNNY') DEFAULT NULL,
  `name_en` varchar(255) DEFAULT NULL,
  `name_jp` varchar(255) DEFAULT NULL,
  `score` double DEFAULT NULL,
  `score_top3` double DEFAULT NULL,
  `status` enum('CRAWLED','CREATED') NOT NULL,
  `summary` varchar(500) DEFAULT NULL,
  `time` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK7he8vghh32a8sn6ed07dw3627` (`google_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `place-open` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `close` int DEFAULT NULL,
  `day` int NOT NULL,
  `open` int DEFAULT NULL,
  `place_id` bigint NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `airport` (
  `place_id` bigint NOT NULL,
  `iata_code` enum('CJJ','CJU','CTS','FUK','GMP','HIJ','HND','ICN','KIX','NGO','NRT','OKA','PUS','SDJ','TAE') NOT NULL,
  PRIMARY KEY (`iata_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `user` (
  `is_deleted` bit(1) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `o_auth_id` varchar(255) DEFAULT NULL,
  `o_auth_provider` varchar(255) DEFAULT NULL,
  `role` enum('ADMIN','USER') DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `trip` (
  `end_date` date NOT NULL,
  `is_deleted` bit(1) NOT NULL,
  `is_fixed` bit(1) NOT NULL,
  `is_planning` bit(1) NOT NULL,
  `person_count` int NOT NULL,
  `start_date` date NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `name` varchar(30) NOT NULL,
  `uuid` varchar(36) NOT NULL,
  `price_type` enum('CHEEP','LUXURY','NORMAL') DEFAULT NULL,
  `schedule_type` enum('LOOSE','NORMAL','TIGHT') DEFAULT NULL,
  `trip_theme_type` enum('FOOD','HEALING','LANDMARK') DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKirf28d16ouebolwyfc7xu8jyg` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `user_fixed_trip` (
  `date` date NOT NULL,
  `trip_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`date`,`user_id`),
  KEY `FKrkpaleu86uw1f3d51e1yw86ys` (`trip_id`),
  CONSTRAINT `FKrkpaleu86uw1f3d51e1yw86ys` FOREIGN KEY (`trip_id`) REFERENCES `trip` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `trip_member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `joined_at` datetime(6) DEFAULT NULL,
  `trip_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `role` enum('MEMBER','OWNER') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_trip_member_trip_user` (`trip_id`,`user_id`),
  KEY `idx_trip_member_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `trip_invitation` (
  `created_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `inviter_user_id` bigint NOT NULL,
  `trip_id` bigint NOT NULL,
  `token` varchar(36) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_trip_invitation_token` (`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `trip_request` (
  `arrive_airport_place_id` bigint DEFAULT NULL,
  `arrive_time` datetime(6) DEFAULT NULL,
  `depart_airport_place_id` bigint DEFAULT NULL,
  `depart_time` datetime(6) DEFAULT NULL,
  `hotel_place_id` bigint DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `trip_id` bigint NOT NULL,
  `hotel_google_id` varchar(255) DEFAULT NULL,
  `region_codes` varchar(255) DEFAULT NULL,
  `selected_place_google_ids` varchar(255) DEFAULT NULL,
  `selected_place_ids` varchar(255) DEFAULT NULL,
  `arrive_airport_code` enum('CJJ','CJU','CTS','FUK','GMP','HIJ','HND','ICN','KIX','NGO','NRT','OKA','PUS','SDJ','TAE') DEFAULT NULL,
  `depart_airport_code` enum('CJJ','CJU','CTS','FUK','GMP','HIJ','HND','ICN','KIX','NGO','NRT','OKA','PUS','SDJ','TAE') DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKtmbj7r4bhm9pil8slcydxraga` (`trip_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `date_plan` (
  `airport_time` time(6) DEFAULT NULL,
  `date` date NOT NULL,
  `is_deleted` bit(1) NOT NULL,
  `airport_place_id` bigint DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `hotel_place_id` bigint DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `region_id` bigint NOT NULL,
  `trip_id` bigint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `necessary_places` varchar(255) DEFAULT NULL,
  `plan_status` enum('AI_PLANNED','CREATED','EDIT','FIXED_PLANNED','TRANSPORT_PLANNED') NOT NULL,
  `trip_theme_type` enum('FOOD','HEALING','LANDMARK') NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `place_plan` (
  `end_time` time(6) DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  `start_time` time(6) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `date_plan_id` bigint DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_index` bigint NOT NULL,
  `place_id` bigint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_place_plan_date_plan_order` (`date_plan_id`,`order_index`),
  CONSTRAINT `FKlp7rd2ikuca4i9ldqpjf9id99` FOREIGN KEY (`date_plan_id`) REFERENCES `date_plan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `transport_plan` (
  `end_time` time(6) DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  `start_time` time(6) DEFAULT NULL,
  `take_time` int NOT NULL,
  `total_distance_meters` int NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `date_plan_id` bigint NOT NULL,
  `from_place_plan_id` bigint DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `to_place_plan_id` bigint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `route_info` json NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_transport_plan_date_plan` (`date_plan_id`),
  KEY `FKl5yerq6blencx628qnn028veg` (`from_place_plan_id`),
  KEY `FKdrdgpayrtrrdg179730vredxc` (`to_place_plan_id`),
  CONSTRAINT `FKdrdgpayrtrrdg179730vredxc` FOREIGN KEY (`to_place_plan_id`) REFERENCES `place_plan` (`id`),
  CONSTRAINT `FKl5yerq6blencx628qnn028veg` FOREIGN KEY (`from_place_plan_id`) REFERENCES `place_plan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `behaviour_history` (
  `is_deleted` bit(1) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `date_plan_id` bigint NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `version` bigint NOT NULL,
  `behaviour` json NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_behaviour_history_date_plan_version` (`date_plan_id`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
