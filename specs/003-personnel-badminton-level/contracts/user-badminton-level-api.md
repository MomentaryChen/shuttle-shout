# API 契約：人員羽球等級欄位

**Feature**: 003-personnel-badminton-level  
**Scope**: 既有用戶相關 API 之請求/回應 body 擴充，路徑與方法不變。

## 1. GET /users、GET /users/{id}、GET /users/admin/users、GET /users/me

**說明**: 取得用戶列表或單一用戶；回應 body 擴充等級欄位。

**回應 body（UserDTO）新增欄位**:

| 欄位 | 型別 | 說明 |
|------|------|------|
| **badmintonLevel** | Integer (nullable) | 羽球等級級數（1–18），null 表示未設定 |

- 既有欄位不變（id, username, email, phoneNumber, realName, avatar, isActive, lastLoginAt, roleNames, roleCodes, createdAt, updatedAt）。
- 未設定等級時 `badmintonLevel` 為 null 或省略（建議回傳 null 以利前端顯示「未設定」）。

## 2. POST /users（建立用戶）

**說明**: 建立新用戶；請求 body 可選等級。

**請求 body（UserCreateDTO）新增欄位**:

| 欄位 | 型別 | 必填 | 說明 |
|------|------|------|------|
| **badmintonLevel** | Integer (nullable) | 否 | 羽球等級級數（1–18）；不傳或 null 則儲存為 null |

**驗證**: 若提供 `badmintonLevel`，須 1 ≤ value ≤ 18，否則回傳 `400 Bad Request` 與明確錯誤訊息（例如「等級須為 1–18」）。

**回應**: 同 GET /users/{id}，含 `badmintonLevel`。

## 3. PUT /users/{id}、PUT /users/me（更新用戶）

**說明**: 更新用戶資料；請求 body 可選等級。

**請求 body（UserUpdateDTO）新增欄位**:

| 欄位 | 型別 | 必填 | 說明 |
|------|------|------|------|
| **badmintonLevel** | Integer (nullable) | 否 | 羽球等級級數（1–18）；傳 null 或省略則維持或清空為 null |

**驗證**: 若提供 `badmintonLevel`，須為 null 或 1 ≤ value ≤ 18，否則回傳 `400 Bad Request`。

**回應**: 同 GET /users/{id}，含 `badmintonLevel`。

## 4. 錯誤回應

- **400 Bad Request**：`badmintonLevel` 不為 null 且不在 1–18 範圍時，回傳錯誤碼與訊息（正體中文），例如：「羽球等級須為 1 至 18 或未設定」。

## 5. 前端對照

- 後端僅回傳/接收**級數**（1–18 或 null）。
- 前端負責將級數對應至**比賽階級**顯示（依 data-model 對照表）；選單可顯示「比賽階級（級數）」或「級數 - 比賽階級」，送出時仍只送級數。
