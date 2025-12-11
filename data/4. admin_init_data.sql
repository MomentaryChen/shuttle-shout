use shuttleshout;

-- ============================================
-- ShuttleShout 用户相关初始化数据
-- 导出时间: 2024
-- 数据库名: shuttleshout
-- ============================================

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 初始化数据: users - 用户表
-- ============================================
-- 注意：密码使用 BCrypt 加密，"Admin" 的 BCrypt 哈希值
-- 如果需要重新生成，可以使用 PasswordUtil.encode("admin123")
DELETE FROM `users` WHERE `username` = 'Admin';
INSERT INTO `users` (`id`, `username`, `password`, `email`, `phone_number`, `real_name`, `avatar`, `is_active`, `last_login_at`, `created_at`, `updated_at`) VALUES
(1, 'Admin', '$2a$10$aqnzMNAZe8e/5EdENPR.AuJav2.1FwxtjZ51gzM3KFdA6N73q6xlW', 'admin@shuttleshout.com', NULL, '系统管理员', NULL, 1, NULL, NOW(), NOW());

-- ============================================
-- 初始化数据: user_roles - 用户角色关联表
-- ============================================
-- 为 Admin 用户分配系统管理员角色 (SYSTEM_ADMIN, role_id = 1)
DELETE FROM `user_roles` WHERE `user_id` = 1;
INSERT INTO `user_roles` (`id`, `user_id`, `role_id`, `created_at`) VALUES
(1, 1, 1, NOW());

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

