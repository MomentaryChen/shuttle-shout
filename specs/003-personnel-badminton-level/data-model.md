# Data Model: 003-personnel-badminton-level

**Branch**: `003-personnel-badminton-level` | **Phase**: 1

## 變更摘要

- **人員（User）**：新增屬性 `badmintonLevel`（級數 1–18 或 null）。
- **羽球等級參考**：以枚舉/常數實作，不新增資料表；級數 1–18 對應比賽階級（新手階、初階、初中階、中階、中進階、高階、職業級）。

## 實體與欄位

### User (`users` 表)

| 欄位 | 型別 | 說明 | 驗證 |
|------|------|------|------|
| (既有) | … | id, username, password, email, phone_number, real_name, avatar, is_active, last_login_at, created_at, updated_at | 不變 |
| **badminton_level** | TINYINT NULL | 羽球等級級數（1–18），null 表示未設定 | 若提供則須 1 ≤ value ≤ 18 |

- **預設**：新建立人員與既有人員遷移後未設定時為 null。
- **唯一性 / 關聯**：無新增唯一鍵或外鍵。

### BadmintonLevel（枚舉 / 對照，非資料表）

| 級數 | 比賽階級 |
|------|----------|
| 1–3 | 新手階 |
| 4–5 | 初階 |
| 6–7 | 初中階 |
| 8–9 | 中階 |
| 10–12 | 中進階 |
| 13–15 | 高階 |
| 16–18 | 職業級 |

- 後端：枚舉或工具類提供 `fromLevel(int)`, `getDisplayName()`（比賽階級）。
- 前端：常數對照級數→比賽階級，顯示時組合「比賽階級（級數）」或「級數 - 比賽階級」。

## 狀態與生命週期

- 等級無狀態機：僅為可選欄位，值為 null 或 1–18。
- 建立：未傳等級則存 null；傳 1–18 則儲存該級數。
- 更新：可改為 null（未設定）或任一 1–18；傳入 1–18 以外或非整數時回傳 400。

## 遷移與既有資料

- 新增 `users.badminton_level` 欄位，TINYINT NULL，預設 NULL。
- 既有人員不寫入預設值，即維持 NULL（未設定），符合 FR-007。
