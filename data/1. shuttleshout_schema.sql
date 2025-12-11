CREATE DATABASE IF NOT EXISTS shuttleshout;
-- ============================================
-- ShuttleShout 数据库表结构导出
-- 导出时间: 2024
-- 数据库名: shuttleshout
-- ============================================

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 表结构: users - 用户表
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
-- 表结构: roles - 角色表
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
-- 表结构: user_roles - 用户角色关联表
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
-- 表结构: teams - 球队表
-- ============================================
DROP TABLE IF EXISTS `teams`;
CREATE TABLE `teams` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '球隊ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '球隊名稱',
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `color` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '球隊顏色標識，如 "bg-blue-500"',
  `level` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '球队程度，如 "初级"、"中级"、"高级"',
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
-- 表结构: user_teams - 球队用户关联表
-- ============================================
DROP TABLE IF EXISTS `user_teams`;
CREATE TABLE `user_teams` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `team_id` bigint(20) NOT NULL COMMENT '球队ID',
  `is_owner` tinyint(1) DEFAULT '0' COMMENT '是否为球队创建者/所有者',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_team_user` (`team_id`,`user_id`) COMMENT '唯一索引：确保同一用户不能重复加入同一球队',
  KEY `idx_team_id` (`team_id`) COMMENT '球队ID索引',
  KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引',
  CONSTRAINT `fk_team_users_team` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_team_users_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COMMENT='球队用户关联表';

-- ============================================
-- 表结构: resource_pages - 页面资源表
-- ============================================
DROP TABLE IF EXISTS `resource_pages`;
CREATE TABLE `resource_pages` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '页面显示名称',
  `code` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '页面代码标识符，如 PERSONNEL_MANAGEMENT',
  `path` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '页面路由路径，如 /personnel-management',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '页面描述',
  `icon` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '页面图标',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序顺序',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '父级页面ID，用于构建页面层级',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否激活',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_code` (`code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='页面资源表';

-- ============================================
-- 表结构: role_resource_pages - 角色页面资源权限关联表
-- ============================================
DROP TABLE IF EXISTS `role_resource_pages`;
CREATE TABLE `role_resource_pages` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `resource_page_id` bigint(20) NOT NULL COMMENT '页面资源ID',
  `can_read` tinyint(1) DEFAULT '1' COMMENT '是否可查看',
  `can_write` tinyint(1) DEFAULT '0' COMMENT '是否可编辑',
  `can_delete` tinyint(1) DEFAULT '0' COMMENT '是否可删除',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_resource_page` (`role_id`,`resource_page_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_resource_page_id` (`resource_page_id`),
  KEY `idx_can_read` (`can_read`),
  KEY `idx_can_write` (`can_write`),
  KEY `idx_can_delete` (`can_delete`),
  CONSTRAINT `fk_role_resource_pages_resource_page_id` FOREIGN KEY (`resource_page_id`) REFERENCES `resource_pages` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_role_resource_pages_role_id` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色页面资源权限关联表';

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

