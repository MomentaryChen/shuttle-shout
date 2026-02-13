# API 行為契約：資源頁面與管理員完整存取

**Feature**: 002-admin-full-page-access  
**Scope**: 既有 API 在「呼叫者具 SYSTEM_ADMIN 角色」時之行為變更，簽章不變。

## 1. GET /resource-pages/my-accessible

**說明**: 取得目前登入使用者可存取之資源頁面列表，供前端導航／選單使用。

**請求**: 無 body；身分由現有認證（如 JWT）帶出，後端以 `@CurrentUserId` 取得 userId。

**回應**: `200 OK`，body 為 `List<ResourcePageDTO>`。

**行為**:

- **一般使用者**：回傳該使用者經由「角色–資源頁面」關聯（`role_resource_pages`）所得到的頁面聯集；若無任何關聯則回傳空陣列。
- **管理員（使用者任一角為 SYSTEM_ADMIN）**：回傳系統中**所有已啟用**（`is_active = true`）的資源頁面；若未使用 is_active 則回傳全部資源頁面。不依 `role_resource_pages` 過濾。

**契約不變**: 路徑、HTTP 方法、回應型別不變；僅回傳內容在「管理員」情境下由「依關聯過濾」改為「全部已啟用頁面」。

---

## 2. GET /resource-pages/check-permission/{resourcePageCode}/{permission}

**說明**: 檢查目前登入使用者對指定資源頁面是否具備指定權限（read / write / delete）。

**路徑參數**:  
- `resourcePageCode`: 資源頁面代碼  
- `permission`: 權限類型（read、write、delete）

**回應**: `200 OK`，body 為 `boolean`（true = 有權限，false = 無權限）。

**行為**:

- **一般使用者**：依該使用者之角色與 `role_resource_pages` 中對應之 `can_read` / `can_write` / `can_delete` 判斷；若無關聯或對應欄位為 false 則回傳 false。
- **管理員（使用者任一角為 SYSTEM_ADMIN）**：對**任意** `resourcePageCode` 與 **任意** `permission` 一律回傳 **true**，不查詢 `role_resource_pages`。

**契約不變**: 路徑、方法、參數與回應型別不變；僅在「管理員」情境下回傳值固定為 true。

---

## 3. 其他端點

以下端點**不受本需求影響**，行為維持不變：

- `GET /resource-pages`（取得所有資源頁面，依既有權限或註解）
- `GET /resource-pages/{id}`、`GET /resource-pages/code/{code}`（SYSTEM_ADMIN 可呼叫，未修改邏輯）
- `GET /resource-pages/role/{roleId}`、`POST` / `PUT` / `DELETE` 等管理端點

前端僅需繼續呼叫 `GET /resource-pages/my-accessible` 與 `GET .../check-permission/...`，無需改路徑或參數；管理員登入後將自動取得完整列表與權限通過結果。
