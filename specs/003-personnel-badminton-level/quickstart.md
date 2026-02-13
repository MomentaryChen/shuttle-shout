# Quickstart: 003-personnel-badminton-level

**Branch**: `003-personnel-badminton-level`

## 目標

人員（User）具備羽球等級欄位：後端儲存級數（1–18），預設 null；前端在人員列表、詳情與編輯表單顯示比賽階級與級數，可選等級並儲存。

## 前置

- Java 8、MySQL；Node 18+、pnpm（`shuttle-shout-frontend`）。
- 後端：`./gradlew bootRun`；前端：`pnpm dev`（或依專案說明）。
- 具人員寫入權限之角色（如 SYSTEM_ADMIN）用於測試人員管理頁面。

## 實作要點

### 1. 資料庫

- 在 `users` 表新增欄位：`badminton_level` TINYINT NULL，註解「羽球等級級數 1–18，null 未設定」。
- 既有人員不更新，維持 NULL。
- 可於 `data/` 下新增遷移腳本或手動執行 ALTER TABLE。

### 2. 後端

- **BadmintonLevel 枚舉**（`common/model/enums/BadmintonLevel.java`）
  - 級數 1–18 對應比賽階級：1–3 新手階、4–5 初階、6–7 初中階、8–9 中階、10–12 中進階、13–15 高階、16–18 職業級。
  - 提供 `fromLevel(int)`, `getDisplayName()`（比賽階級名稱）；若 value 不在 1–18 回傳 null 或拋錯依實作擇一。
- **UserPO**：新增 `private Integer badmintonLevel;`，對應欄位 `badminton_level`。
- **UserDTO / UserCreateDTO / UserUpdateDTO**：新增 `private Integer badmintonLevel;`（可選）。
- **UserServiceImpl**：
  - `createUser`：從 DTO 讀取 badmintonLevel，若在 1–18 則 set，否則 set null；寫入前驗證 1–18 或 null，非法值拋 ApiException 400。
  - `updateUser`：同上處理 badmintonLevel（可清空為 null）。
  - `convertToDto`：將 user.getBadmintonLevel() 設入 DTO。
- **驗證**：接受 badmintonLevel 為 null 或 1–18；其餘回傳 400 與正體中文錯誤訊息。

### 3. 前端

- **types/api.ts**：`UserDto` 新增 `badmintonLevel?: number | null`。可選新增常數：級數 1–18 對應比賽階級名稱，供顯示與下拉選單使用。
- **personnel-management.tsx**（或其他人員列表/編輯元件）：
  - 列表：每列顯示等級欄位；未設定顯示「未設定」，有值顯示「比賽階級（級數）」或「級數 - 比賽階級」。
  - 新增/編輯用戶表單：下拉選單列出 1–18，選項顯示比賽階級與級數；可選「未設定」送 null。送出時 body 含 `badmintonLevel`（number | null）。
- **lib/api.ts**：若更新用戶 API 需傳 body，確保 `adminApi.updateUser` 或等同方法可傳 `badmintonLevel`。

### 4. 權限

- 等級的讀寫依既有人員管理頁面權限（如 PERSONNEL_MANAGEMENT 之 can_read / can_write）；無需新增權限。

## 驗收對照

- Spec User Story 1：查詢人員含等級欄位；新增人員不選等級則為 null；可將既有等級改為未設定並儲存為 null。
- Spec User Story 2：編輯時選擇等級並儲存，詳情與列表顯示該級數及比賽階級；未設定顯示「未設定」。
- Spec SC-001～SC-004：所有介面顯示等級欄位且未設定時明確顯示；非法等級值後端 400 並回饋訊息。

## 手動檢查建議

1. 取得單一用戶 API（GET /users/{id}），確認回應含 `badmintonLevel`（未設定時為 null）。
2. 建立用戶不傳 `badmintonLevel`，確認儲存後為 null；傳 `badmintonLevel: 8`，確認儲存為 8。
3. 更新用戶送 `badmintonLevel: null`，確認該用戶等級變為 null；送 `badmintonLevel: 20`，確認 400。
4. 人員管理頁面：列表見等級欄、編輯表單可選等級並儲存，顯示與選項一致。

## 文件

- [spec.md](../spec.md) — 功能規格  
- [plan.md](../plan.md) — 實作計畫  
- [research.md](../research.md) — 決策與替代方案  
- [data-model.md](../data-model.md) — 實體與欄位  
- [contracts/user-badminton-level-api.md](./contracts/user-badminton-level-api.md) — API 契約  
