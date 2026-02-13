# Data Model: 002-admin-full-page-access

**Branch**: `002-admin-full-page-access` | **Phase**: 1

本功能**不新增與不修改資料表**，僅規範既有實體之行為不變式（invariant）：具管理員角色之使用者，在「可存取頁面」與「頁面權限檢查」上視同擁有全部已啟用頁面。

## 對應 Spec 關鍵實體

| Spec 實體 | 對應儲存 / 運行時 | 說明 |
|-----------|-------------------|------|
| 角色（管理員） | `roles` 表，`code = 'SYSTEM_ADMIN'` | 具此角色之使用者視為可存取所有已啟用頁面 |
| 頁面（資源） | `resource_pages` 表 | 含 code、path、is_active 等；管理員短路時回傳 is_active = true 者（若無則全部） |
| 使用者 | `users` 表，經 `user_roles` 關聯 `roles` | 若任一身分為 SYSTEM_ADMIN，則可存取頁面集合 = 全部已啟用頁面 |

## 既有主要實體（摘要）

### User (`users`)

- 經 `user_roles` 與 **Role** 多對多關聯。  
- 用於 `getResourcePagesByUserId(userId)`、`hasPermission(userId, ...)`；需能取得該使用者的角色列表以判斷是否含 SYSTEM_ADMIN。

### Role (`roles`)

- **id**, **code**（如 `SYSTEM_ADMIN`, `PLAYER`）, **name**, **description**, **is_active**, **created_at**, **updated_at**  
- **行為不變式**：當 `code = 'SYSTEM_ADMIN'` 時，該角色之使用者在「可存取頁面」與「權限檢查」上不受 `role_resource_pages` 限制，系統視為擁有全部已啟用頁面之讀寫刪權限。

### ResourcePage (`resource_pages`)

- **id**, **code**, **path**, **name**, **description**, **icon**, **sort_order**, **parent_id**, **is_active**, **created_at**, **updated_at**  
- 管理員短路時回傳範圍：`is_active = true` 之記錄（若欄位不存在或未使用則回傳全部）。

### RoleResourcePage (`role_resource_pages`)

- **id**, **role_id**, **resource_page_id**, **can_read**, **can_write**, **can_delete**  
- **非管理員**：可存取頁面與權限完全由此表決定。  
- **管理員**：此表不參與判斷，服務層依角色碼短路。

## 狀態與查詢行為

- **一般使用者**：`getResourcePagesByUserId` = 依其角色經 `role_resource_pages` 聯集出可存取頁面；`hasPermission` = 依該表與 permission 欄位判斷。  
- **管理員（任一角為 SYSTEM_ADMIN）**：`getResourcePagesByUserId` = 回傳所有 is_active = true 的 resource_pages；`hasPermission` = 對任意 code 與 permission 回傳 true。  
- 新增頁面時僅需寫入 `resource_pages`，管理員即可存取，無需寫入 `role_resource_pages`（FR-004）。
