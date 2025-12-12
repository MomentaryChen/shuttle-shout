use shuttleshout;

-- ============================================
-- ShuttleShout 用戶相關初始化資料
-- 匯出時間: 2024
-- 資料庫名: shuttleshout
-- ============================================

-- 設定字元集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 初始化資料: users - 用戶表
-- ============================================
-- 注意：密碼使用 BCrypt 加密，"Admin" 的 BCrypt 雜湊值
-- 如果需要重新生成，可以使用 PasswordUtil.encode("admin123")
DELETE FROM `users` WHERE `username` = 'Admin';
INSERT INTO `users` (`id`, `username`, `password`, `email`, `phone_number`, `real_name`, `avatar`, `is_active`, `last_login_at`, `created_at`, `updated_at`) VALUES
(1, 'Admin', '$2a$10$aqnzMNAZe8e/5EdENPR.AuJav2.1FwxtjZ51gzM3KFdA6N73q6xlW', 'admin@shuttleshout.com', NULL, '系統管理員', NULL, 1, NULL, NOW(), NOW());

-- ============================================
-- 初始化資料: user_roles - 用戶角色關聯表
-- ============================================
-- 為 Admin 用戶分配系統管理員角色 (SYSTEM_ADMIN, role_id = 1)
DELETE FROM `user_roles` WHERE `user_id` = 1;
INSERT INTO `user_roles` (`id`, `user_id`, `role_id`, `created_at`) VALUES
(1, 1, 1, NOW());

-- 恢復外鍵檢查
SET FOREIGN_KEY_CHECKS = 1;

