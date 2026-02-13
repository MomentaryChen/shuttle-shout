-- ============================================
-- 003-personnel-badminton-level: 人員羽球等級欄位
-- 羽球等級級數 1–18，null 表示未設定（台灣羽球推廣協會分級）
-- ============================================
SET NAMES utf8mb4;

ALTER TABLE `users`
  ADD COLUMN `badminton_level` TINYINT NULL DEFAULT NULL
  COMMENT '羽球等級級數 1–18，null 未設定'
  AFTER `updated_at`;

-- 既有人員不更新，維持 NULL
