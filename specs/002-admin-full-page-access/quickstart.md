# Quickstart: 002-admin-full-page-access

**Branch**: `002-admin-full-page-access`

## 目標

管理員（SYSTEM_ADMIN）登入後可瀏覽系統內所有已啟用頁面，且「我的可存取頁面」列表與權限檢查對管理員一律回傳全部／通過。

## 前置

- Java 8、MySQL；可選前端：Node 18+、pnpm（`shuttle-shout-frontend`）。
- 後端：`./gradlew bootRun`（或依專案說明啟動）。
- 至少一個使用者具 **SYSTEM_ADMIN** 角色（經 `user_roles` 關聯至 `code = 'SYSTEM_ADMIN'` 之角色）。

## 實作要點

### 後端（本需求唯一變更處）

- **`ResourcePageServiceImpl.getResourcePagesByUserId(Long userId)`**
  - 以 `userRepository.selectOneWithRelationsById(userId)` 取得使用者與其角色。
  - 若使用者任一角色之 `code` 為 `"SYSTEM_ADMIN"`，則短路：回傳所有 `is_active = true` 的資源頁面（可呼叫既有 `getMapper().selectAll()` 或等同查詢後依 `isActive` 過濾，或新增依 `is_active = true` 的查詢），並 return，不查 `role_resource_pages`。
  - 否則維持現有邏輯（依角色 ID 查 `role_resource_pages` 聯集）。
- **`ResourcePageServiceImpl.hasPermission(Long userId, String resourcePageCode, String permission)`**
  - 同上取得使用者與角色；若任一角為 `SYSTEM_ADMIN`，則直接 return `true`。
  - 否則維持現有邏輯（依 role_resource_pages 與 can_read/can_write/can_delete 判斷）。

**注意**: 角色碼比對建議使用常數（如 `"SYSTEM_ADMIN"`）或既有 `SecurityUtil.hasRole` 之對應邏輯；若在服務層無法使用 SecurityContext，則以載入之 `UserPO.getRoles()` 的 `RolePO.getCode()` 比對即可。

### 前端

- **無需變更**。繼續使用 `GET /resource-pages/my-accessible` 與 `GET /resource-pages/check-permission/{code}/{permission}`；管理員登入後將自動取得完整列表與權限通過。

## 驗收對照

- Spec User Story 1：以管理員帳號登入，依序開啟所有已註冊頁面（含選單與直接輸入路徑），皆可正常載入。
- Spec User Story 2：管理員之導航／可存取頁面列表與系統已啟用頁面總數一致。
- Spec SC-001～SC-004：管理員 100% 頁面可開、列表完整、新頁面無需授權即可存取；非管理員行為不變。

## 手動檢查建議

1. 以**非**管理員帳號登入，呼叫 `GET /resource-pages/my-accessible`，記錄回傳筆數 A。
2. 以**管理員**帳號登入，呼叫 `GET /resource-pages/my-accessible`，確認回傳筆數 ≥ 所有已啟用資源頁面數，且包含步驟 1 中可能未出現的頁面。
3. 以管理員帳號呼叫 `GET /resource-pages/check-permission/{某頁 code}/read`，確認回傳 `true`（可對多個 code 抽測）。
4. 以非管理員帳號再次呼叫 my-accessible 與 check-permission，確認與變更前一致。

## 文件

- [spec.md](../spec.md) — 功能規格  
- [plan.md](../plan.md) — 實作計畫  
- [research.md](../research.md) — 決策與替代方案  
- [data-model.md](../data-model.md) — 實體與行為不變式  
- [contracts/resource-pages-admin-behavior.md](./contracts/resource-pages-admin-behavior.md) — API 行為契約  
