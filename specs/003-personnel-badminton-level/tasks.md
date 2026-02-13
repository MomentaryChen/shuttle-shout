# Tasks: 人員羽球等級制度

**Input**: Design documents from `/specs/003-personnel-badminton-level/`  
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Spec 未明確要求自動化測試；任務以實作為主，驗收依 quickstart.md 手動檢查。

**Organization**: 依 User Story 分階段；US1（等級欄位與預設 null）為 MVP，US2 為選等級與顯示比賽階級，US3 本 MVP 不實作參考表維護，僅確保既有資料遷移後為 null 且查詢正常。

## Format: `[ID] [P?] [Story] Description`

- **[P]**: 可與同階段其他任務並行（不同檔案、無依賴）
- **[Story]**: 所屬 User Story（US1, US2, US3）
- 描述中含具體檔案路徑

## Path Conventions

- **Backend**: `src/main/java/com/shuttleshout/`
- **Frontend**: `shuttle-shout-frontend/`（含 `app/`, `components/`, `types/`, `lib/`）
- **Data**: `data/`（遷移腳本）

---

## Phase 1: Setup（確認環境與文件）

**Purpose**: 確認分支與設計文件就緒

- [x] T001 Confirm feature branch `003-personnel-badminton-level` and design docs (spec.md, plan.md, data-model.md, quickstart.md, contracts/) in specs/003-personnel-badminton-level/
- [x] T002 [P] Verify backend runs: `./gradlew bootRun` and frontend runs from repo root (e.g. `cd shuttle-shout-frontend && pnpm dev`)

**Checkpoint**: 後端與前端可啟動，作為修改基準

---

## Phase 2: Foundational（資料庫與等級對照）

**Purpose**: 新增等級欄位與級數→比賽階級對照，為後續 User Story 共用

**Checkpoint**: 完成後 US1/US2 實作可開始

- [x] T003 Add DB migration: `users` 表新增 `badminton_level` TINYINT NULL，註解「羽球等級級數 1–18，null 未設定」；可於 data/ 新增遷移腳本或手動 ALTER TABLE，既有人員不更新（維持 NULL）
- [x] T004 [P] Create BadmintonLevel enum in src/main/java/com/shuttleshout/common/model/enums/BadmintonLevel.java：級數 1–18 對應比賽階級（1–3 新手階、4–5 初階、6–7 初中階、8–9 中階、10–12 中進階、13–15 高階、16–18 職業級），提供 fromLevel(int) 與 getDisplayName() 回傳比賽階級名稱，非法值回傳 null 或拋錯

**Checkpoint**: 資料庫可存等級、後端有等級枚舉可引用

---

## Phase 3: User Story 1 - 人員具備羽球等級欄位且預設為未設定 (Priority: P1) — MVP

**Goal**: 人員具備等級欄位；查詢回傳等級（未設定為 null）；新增人員不選等級則為 null；可將等級清空為未設定。

**Independent Test**: 查詢任一人員 API 回傳含 badmintonLevel 欄位；新增人員不傳等級則儲存為 null；編輯將等級改為未設定後儲存為 null；前端列表見等級欄、未設定顯示「未設定」。

### Implementation for User Story 1

- [x] T005 [P] [US1] Add `badmintonLevel` (Integer, nullable) to UserPO with column `badminton_level` in src/main/java/com/shuttleshout/common/model/po/UserPO.java
- [x] T006 [P] [US1] Add `badmintonLevel` (Integer, nullable) to UserDTO in src/main/java/com/shuttleshout/common/model/dto/UserDTO.java
- [x] T007 [P] [US1] Add optional `badmintonLevel` to UserCreateDTO and UserUpdateDTO in src/main/java/com/shuttleshout/common/model/dto/UserCreateDTO.java and UserUpdateDTO.java
- [x] T008 [US1] In UserServiceImpl: createUser 不傳或 null 時 setBadmintonLevel(null)；updateUser 可接受 null 以清空等級；convertToDto 將 user.getBadmintonLevel() 設入 DTO，在 src/main/java/com/shuttleshout/service/impl/UserServiceImpl.java
- [x] T009 [US1] Add frontend type: UserDto 新增 `badmintonLevel?: number | null` in shuttle-shout-frontend/types/api.ts
- [x] T010 [US1] In personnel-management: 列表新增等級欄位，當 badmintonLevel 為 null 或未定義時顯示「未設定」 in shuttle-shout-frontend/components/personnel-management.tsx

**Checkpoint**: 人員 API 含等級欄位、預設 null；列表顯示等級欄與「未設定」；符合 US1 驗收情境 1–3、FR-001/FR-003/FR-004/FR-005/FR-007

---

## Phase 4: User Story 2 - 依羽球等級參考表設定與顯示人員等級 (Priority: P2)

**Goal**: 人員新增/編輯可選擇等級（1–18）；後端驗證 1–18 或 null、拒絕非法值；前端顯示比賽階級與級數。

**Independent Test**: 編輯人員選擇某一等級儲存後，詳情與列表顯示該級數及對應比賽階級；送非法等級後端 400；未設定顯示「未設定」。

### Implementation for User Story 2

- [x] T011 [US2] In UserServiceImpl: createUser/updateUser 若傳入 badmintonLevel 非 null 則驗證 1 ≤ value ≤ 18，否則拋 ApiException 400 與正體中文訊息（如「羽球等級須為 1 至 18 或未設定」）；寫入時 setBadmintonLevel 僅接受 null 或 1–18，在 src/main/java/com/shuttleshout/service/impl/UserServiceImpl.java
- [x] T012 [P] [US2] Add frontend constant: 級數 1–18 對應比賽階級名稱（與 data-model 一致），供下拉與顯示用 in shuttle-shout-frontend/types/api.ts（或獨立 constants 檔）
- [x] T013 [US2] In personnel-management: 新增/編輯用戶表單新增等級下拉選單，選項為 1–18、顯示「比賽階級（級數）」或「級數 - 比賽階級」，含「未設定」送 null；送出時 body 含 badmintonLevel in shuttle-shout-frontend/components/personnel-management.tsx
- [x] T014 [US2] In personnel-management: 列表等級欄有值時顯示比賽階級與級數（同上格式），未設定顯示「未設定」 in shuttle-shout-frontend/components/personnel-management.tsx
- [x] T015 [US2] Ensure adminApi updateUser (或等同) 請求 body 可傳 badmintonLevel in shuttle-shout-frontend/lib/api.ts

**Checkpoint**: 可選等級並儲存、列表與表單顯示比賽階級與級數；非法值 400；符合 US2 驗收情境 1–2、FR-002/FR-004/FR-006、SC-001–SC-004

---

## Phase 5: User Story 3 - 等級參考表可維護且不影響既有資料 (Priority: P3)

**Goal**: 本 MVP 不實作參考表維護介面；僅確保既有人員遷移後等級為 null、查詢與顯示無錯誤。

**Independent Test**: 遷移後 GET /users 與 GET /users/{id} 回傳含 badmintonLevel（null）；既有人員列表顯示「未設定」無報錯。

### Implementation for User Story 3

- [x] T016 [US3] Verify migration: 既有人員 badminton_level 為 NULL，GET /users 與 GET /users/{id} 回傳 badmintonLevel 為 null，前端人員列表不報錯、顯示「未設定」（可合併於 T017 quickstart 手動驗證）

**Checkpoint**: 既有資料行為符合 FR-007、US3 邊界情境

---

## Phase 6: Polish & Cross-Cutting

**Purpose**: 收尾與驗收

- [ ] T017 Run quickstart.md validation manually: 等級欄位與預設 null、選等級儲存與顯示、非法值 400、既有人員顯示未設定

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: 無依賴，可立即執行
- **Phase 2 (Foundational)**: 依賴 Phase 1；完成後阻擋 US1/US2 的後端與 DB 工作可開始
- **Phase 3 (US1)**: 依賴 Phase 2；US1 完成即 MVP 可驗收
- **Phase 4 (US2)**: 依賴 Phase 2 與 Phase 3（需先有等級欄位與 API）
- **Phase 5 (US3)**: 依賴 Phase 2 遷移與 Phase 3 查詢/顯示
- **Phase 6 (Polish)**: 依賴 Phase 3–5 完成

### User Story Dependencies

- **US1 (P1)**: 僅依賴 Foundational；可獨立驗收（等級欄位存在、預設 null、列表顯示未設定）
- **US2 (P2)**: 依賴 US1（需有等級欄位與 API）；可獨立驗收（選等級、顯示比賽階級與級數、驗證 1–18）
- **US3 (P3)**: 依賴遷移與 US1 顯示；本 MVP 僅驗證既有人員為 null 與查詢正常，不實作參考表維護

### Within Each User Story

- 後端：PO/DTO → Service 邏輯與驗證 → 無需新增 Controller 端點
- 前端：types/常數 → 列表顯示 → 表單與 API 傳遞

### Parallel Opportunities

- T005, T006, T007 可並行（不同檔案）
- T009 與後端 T008 可並行
- T012 可與 T011 並行（前端常數 vs 後端驗證）

---

## Implementation Strategy

### MVP First（僅 User Story 1）

1. 完成 Phase 1：Setup  
2. 完成 Phase 2：Foundational（DB + BadmintonLevel 枚舉）  
3. 完成 Phase 3：User Story 1  
4. **STOP and VALIDATE**：依 quickstart 驗證等級欄位與預設 null、列表顯示「未設定」

### Incremental Delivery

1. Phase 1 + 2 → 基礎就緒  
2. Phase 3 (US1) → 獨立驗收 → MVP  
3. Phase 4 (US2) → 選等級與顯示比賽階級、驗證 1–18 → 完整功能  
4. Phase 5 (US3) → 確認既有人員與遷移行為  
5. Phase 6 → 手動跑 quickstart 全項驗收  

---

## Notes

- 等級對照表以枚舉/常數實作，不新增資料表或管理後台（符合 plan 避免過度設計）
- 程度說明不在介面呈現（符合 spec clarify）
- 列表依等級篩選/排序為規格「後續擴充」，本 tasks 不包含
