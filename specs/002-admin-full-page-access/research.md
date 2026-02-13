# Research: 管理員完整頁面瀏覽權限

**Feature**: 002-admin-full-page-access  
**Purpose**: 決策「管理員視為可存取全部頁面」之實作方式，並釐清與既有權限模型之關係。

## 1. 管理員角色識別

**Decision**: 以既有角色代碼 `SYSTEM_ADMIN` 作為「管理員」之識別。  
**Rationale**: 專案中已使用 `@PreAuthorize("hasRole('SYSTEM_ADMIN')")` 與 `SecurityUtil.hasRole("SYSTEM_ADMIN")`，角色代碼一致、無需新增枚舉或設定檔。  
**Alternatives considered**: 新增設定項（如 `app.admin-role-codes`）可支援多角色視為管理員，但規格僅要求「管理員」一種，YAGNI 不引入。

## 2. 實作策略：短路 vs 資料面授權

**Decision**: 在服務層「依使用者回傳可存取頁面」與「檢查單頁權限」時，若使用者具 `SYSTEM_ADMIN`，則短路回傳「全部頁面」／「有權限」，不查詢 `role_resource_pages`。  
**Rationale**:  
- 符合 FR-004：新增頁面時管理員自動可存取，無需維護角色–頁面對應。  
- 單一責任：邏輯集中於 `ResourcePageServiceImpl`，易測、易維護。  
- 無 schema 變更、無遷移、無營運上「記得把新頁面掛給 admin」之負擔。  
**Alternatives considered**:  
- 在資料庫將所有 `resource_pages` 逐筆授權給 SYSTEM_ADMIN 角色：需於每次新增頁面時寫入 `role_resource_pages`，易遺漏且與 FR-004 衝突。  
- 新增「超級管理員」旗標於使用者或角色：多餘維度，既有角色碼已足。

## 3. 判斷時機與依賴

**Decision**: 在 `ResourcePageServiceImpl.getResourcePagesByUserId(Long userId)` 與 `hasPermission(Long userId, String resourcePageCode, String permission)` 內，依 `userId` 載入使用者與其角色，若任一角色的 `code` 為 `SYSTEM_ADMIN`，則執行短路。  
**Rationale**: 服務層已有 `userRepository` 可取得使用者與角色；不在 Controller 層判斷，避免重複與分散。  
**Alternatives considered**: 在 Controller 呼叫前用 `SecurityUtil.hasRole("SYSTEM_ADMIN")` 再呼叫不同服務方法：會造成「取得當前使用者」與「傳入 userId」雙重來源，且無法覆蓋「以 userId 查詢」的測試情境，故在服務層依 `userId` 與關聯角色判斷為宜。

## 4. 「全部頁面」之範圍

**Decision**: 短路時回傳「系統中所有已啟用（is_active = true）的資源頁面」；若無啟用旗標則回傳全部。  
**Rationale**: 與 spec「已定義、已啟用頁面」一致；與既有 `getAllResourcePages()` 或等價查詢行為對齊，避免回傳已停用頁面造成前端混淆。  
**Alternatives considered**: 回傳全部頁面含停用：可能讓管理員看到尚未開放功能，依 spec 假設以「已啟用」為範圍。

## 5. 權限檢查 hasPermission 之語意

**Decision**: 當使用者為 SYSTEM_ADMIN 時，對任意 `resourcePageCode` 與 `permission`（read/write/delete）回傳 `true`。  
**Rationale**: spec 假設「本需求僅涵蓋瀏覽/讀取」；實務上管理員通常具完整操作權，對 write/delete 一併放行可避免前端或路由仍依 `check-permission` 阻擋管理員，且與「管理員可瀏覽所有頁面」之預期一致。  
**Alternatives considered**: 僅對 read 回傳 true、write/delete 仍查表：可能導致管理員進入頁面後操作被擋，需額外為管理員授權，違反「無需額外設定」；故採一律 true。
