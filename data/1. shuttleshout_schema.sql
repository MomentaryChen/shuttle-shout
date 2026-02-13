CREATE DATABASE IF NOT EXISTS shuttleshout;
-- ============================================
-- ShuttleShout 資料庫表結構匯出
-- 匯出時間: 2024
-- 資料庫名: shuttleshout
-- ============================================

-- 設定字元集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 表結構: users - 用戶表
-- ============================================
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用戶ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用戶名',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密碼（加密後）',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '郵箱',
  `phone_number` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '電話號碼',
  `real_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '真實姓名',
  `avatar` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '頭像URL',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否啟用',
  `last_login_at` datetime DEFAULT NULL COMMENT '最後登錄時間',
  `created_at` datetime NOT NULL COMMENT '建立時間',
  `updated_at` datetime NOT NULL COMMENT '更新時間',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `idx_user_username` (`username`),
  KEY `idx_user_email` (`email`),
  KEY `idx_user_active` (`is_active`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用戶表';

-- ============================================
-- 表結構: roles - 角色表
-- ============================================
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名稱',
  `code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色代碼，如 ADMIN, USER, MANAGER',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '角色描述',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否啟用',
  `created_at` datetime NOT NULL COMMENT '建立時間',
  `updated_at` datetime NOT NULL COMMENT '更新時間',
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_role_name` (`name`),
  KEY `idx_role_code` (`code`),
  KEY `idx_role_active` (`is_active`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ============================================
-- 表結構: user_roles - 用戶角色關聯表
-- ============================================
DROP TABLE IF EXISTS `user_roles`;
CREATE TABLE `user_roles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '關聯ID',
  `user_id` bigint(20) NOT NULL COMMENT '用戶ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `created_at` datetime NOT NULL COMMENT '建立時間',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`),
  KEY `idx_user_role_user` (`user_id`),
  KEY `idx_user_role_role` (`role_id`),
  CONSTRAINT `user_roles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `user_roles_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用戶角色關聯表';

-- ============================================
-- 表結構: teams - 球隊表
-- ============================================
DROP TABLE IF EXISTS `teams`;
CREATE TABLE `teams` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '球隊ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '球隊名稱',
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `color` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '球隊顏色標識，如 "bg-blue-500"',
  `level` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '球隊程度，如 "初級"、"中級"、"高級"',
  `max_players` int(11) NOT NULL DEFAULT '20' COMMENT '球隊最大人數',
  `court_count` int(11) NOT NULL DEFAULT '2' COMMENT '球隊分配的場地數量',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否啟用',
  `user_id` bigint(20) NOT NULL COMMENT '球隊所屬用戶ID',
  `created_at` datetime NOT NULL COMMENT '建立時間',
  `updated_at` datetime NOT NULL COMMENT '更新時間',
  PRIMARY KEY (`id`),
  KEY `idx_team_name` (`name`),
  KEY `idx_team_active` (`is_active`),
  KEY `idx_team_user` (`user_id`),
  CONSTRAINT `teams_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='球隊表';

-- ============================================
-- 表結構: user_teams - 球隊用戶關聯表
-- ============================================
DROP TABLE IF EXISTS `user_teams`;
CREATE TABLE `user_teams` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主鍵ID',
  `user_id` bigint(20) NOT NULL COMMENT '用戶ID',
  `team_id` bigint(20) NOT NULL COMMENT '球隊ID',
  `is_owner` tinyint(1) DEFAULT '0' COMMENT '是否為球隊創建者/所有者',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_team_user` (`team_id`,`user_id`) COMMENT '唯一索引：確保同一用戶不能重複加入同一球隊',
  KEY `idx_team_id` (`team_id`) COMMENT '球隊ID索引',
  KEY `idx_user_id` (`user_id`) COMMENT '用戶ID索引',
  CONSTRAINT `fk_team_users_team` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_team_users_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COMMENT='球隊用戶關聯表';

-- ============================================
-- 表結構: team_courts - 球隊場地表
-- ============================================
DROP TABLE IF EXISTS `team_courts`;
CREATE TABLE `team_courts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '場地ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '場地名稱',
  `team_id` bigint(20) NOT NULL COMMENT '所屬球隊ID',
  `player1_id` bigint(20) DEFAULT NULL COMMENT '球員1用戶ID',
  `player2_id` bigint(20) DEFAULT NULL COMMENT '球員2用戶ID',
  `player3_id` bigint(20) DEFAULT NULL COMMENT '球員3用戶ID',
  `player4_id` bigint(20) DEFAULT NULL COMMENT '球員4用戶ID',
  `match_started_at` datetime DEFAULT NULL COMMENT '比賽開始時間',
  `match_ended_at` datetime DEFAULT NULL COMMENT '比賽結束時間',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否啟用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
  PRIMARY KEY (`id`),
  KEY `idx_team_court_team` (`team_id`),
  KEY `idx_team_court_active` (`is_active`),
  KEY `idx_team_court_player1` (`player1_id`),
  KEY `idx_team_court_player2` (`player2_id`),
  KEY `idx_team_court_player3` (`player3_id`),
  KEY `idx_team_court_player4` (`player4_id`),
  CONSTRAINT `fk_team_courts_team` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_team_courts_player1` FOREIGN KEY (`player1_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_team_courts_player2` FOREIGN KEY (`player2_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_team_courts_player3` FOREIGN KEY (`player3_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_team_courts_player4` FOREIGN KEY (`player4_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='球隊場地表';

-- ============================================
-- 表結構: resource_pages - 頁面資源表
-- ============================================
DROP TABLE IF EXISTS `resource_pages`;
CREATE TABLE `resource_pages` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '頁面顯示名稱',
  `code` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '頁面代碼識別碼，如 PERSONNEL_MANAGEMENT',
  `path` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '頁面路由路徑，如 /personnel-management',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '頁面描述',
  `icon` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '頁面圖標',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序順序',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '父級頁面ID，用於構建頁面層級',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否啟用',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_code` (`code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='頁面資源表';

-- ============================================
-- 表結構: role_resource_pages - 角色頁面資源權限關聯表
-- ============================================
DROP TABLE IF EXISTS `role_resource_pages`;
CREATE TABLE `role_resource_pages` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主鍵ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `resource_page_id` bigint(20) NOT NULL COMMENT '頁面資源ID',
  `can_read` tinyint(1) DEFAULT '1' COMMENT '是否可查看',
  `can_write` tinyint(1) DEFAULT '0' COMMENT '是否可編輯',
  `can_delete` tinyint(1) DEFAULT '0' COMMENT '是否可刪除',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_resource_page` (`role_id`,`resource_page_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_resource_page_id` (`resource_page_id`),
  KEY `idx_can_read` (`can_read`),
  KEY `idx_can_write` (`can_write`),
  KEY `idx_can_delete` (`can_delete`),
  CONSTRAINT `fk_role_resource_pages_resource_page_id` FOREIGN KEY (`resource_page_id`) REFERENCES `resource_pages` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_role_resource_pages_role_id` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色頁面資源權限關聯表';

-- ============================================
-- 表結構: matches - 比賽表
-- ============================================
DROP TABLE IF EXISTS `matches`;
CREATE TABLE `matches` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '比賽ID',
  `team_id` bigint(20) NOT NULL COMMENT '所屬球隊ID',
  `court_id` bigint(20) NOT NULL COMMENT '場地ID',
  `player1_id` bigint(20) DEFAULT NULL COMMENT '球員1用戶ID',
  `player2_id` bigint(20) DEFAULT NULL COMMENT '球員2用戶ID',
  `player3_id` bigint(20) DEFAULT NULL COMMENT '球員3用戶ID',
  `player4_id` bigint(20) DEFAULT NULL COMMENT '球員4用戶ID',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ONGOING' COMMENT '比賽狀態：PENDING_CONFIRMATION(等待確認), ONGOING(進行中), FINISHED(已完成), CANCELLED(已取消)',
  `started_at` datetime NOT NULL COMMENT '比賽開始時間',
  `ended_at` datetime DEFAULT NULL COMMENT '比賽結束時間',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
  PRIMARY KEY (`id`),
  KEY `idx_match_team` (`team_id`),
  KEY `idx_match_court` (`court_id`),
  KEY `idx_match_status` (`status`),
  KEY `idx_match_started_at` (`started_at`),
  KEY `idx_match_player1` (`player1_id`),
  KEY `idx_match_player2` (`player2_id`),
  KEY `idx_match_player3` (`player3_id`),
  KEY `idx_match_player4` (`player4_id`),
  CONSTRAINT `fk_matches_team` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_matches_court` FOREIGN KEY (`court_id`) REFERENCES `team_courts` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_matches_player1` FOREIGN KEY (`player1_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_matches_player2` FOREIGN KEY (`player2_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_matches_player3` FOREIGN KEY (`player3_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_matches_player4` FOREIGN KEY (`player4_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='比賽表';

-- ============================================
-- 表結構: players - 球員表
-- ============================================
DROP TABLE IF EXISTS `players`;
CREATE TABLE `players` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '球員ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '球員姓名',
  `phone_number` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '電話號碼',
  `notes` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '備註',
  `team_id` bigint(20) NOT NULL COMMENT '所屬球隊ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
  PRIMARY KEY (`id`),
  KEY `idx_player_team` (`team_id`),
  KEY `idx_player_name` (`name`),
  CONSTRAINT `fk_players_team` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='球員表';

-- ============================================
-- 表結構: queues - 叫號隊列表
-- ============================================
DROP TABLE IF EXISTS `queues`;
CREATE TABLE `queues` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '隊列ID',
  `player_id` bigint(20) NOT NULL COMMENT '球員ID',
  `court_id` bigint(20) DEFAULT NULL COMMENT '場地ID',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'WAITING' COMMENT '隊列狀態：WAITING(等待中), CALLED(已叫號), SERVED(已服務), CANCELLED(已取消)',
  `queue_number` int(11) DEFAULT NULL COMMENT '排隊號碼',
  `called_at` datetime DEFAULT NULL COMMENT '叫號時間',
  `served_at` datetime DEFAULT NULL COMMENT '服務時間（上場時間）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
  PRIMARY KEY (`id`),
  KEY `idx_queue_player` (`player_id`),
  KEY `idx_queue_court` (`court_id`),
  KEY `idx_queue_status` (`status`),
  KEY `idx_queue_number` (`queue_number`),
  CONSTRAINT `fk_queues_player` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_queues_court` FOREIGN KEY (`court_id`) REFERENCES `team_courts` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='叫號隊列表';

-- 恢復外鍵檢查
SET FOREIGN_KEY_CHECKS = 1;

