# Tasks: 管理員完整頁面瀏覽權限

**Input**: Design documents from `/specs/002-admin-full-page-access/`  
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Spec 未明確要求 TDD；任務以實作為主，必要時可補驗收或手動依 quickstart 驗證。

**Organization**: 依 User Story 分階段，US1（管理員可瀏覽所有頁面）為 MVP，可獨立驗收。

## Format: `[ID] [P?] [Story] Description`

- **[P]**: 可與同階段其他任務並行（不同檔案、無依賴）
- **[Story]**: 所屬 User Story（US1, US2）
- 描述中含具體檔案路徑

## Path Conventions

- **Backend**: `src/main/java/com/shuttleshout/`
- **Frontend**: `shuttle-shout-frontend/`（本需求無前端變更）

---

## Phase 1: Setup（確認環境與文件）

**Purpose**: 確認分支與既有程式庫，無新專案初始化

- [x] T001 Confirm feature branch `002-admin-full-page-access` and design docs (spec.md, plan.md, research.md, data-model.md, quickstart.md) in specs/002-admin-full-page-access/
- [x] T002 [P] Verify backend runs: `./gradlew bootRun` from repo root
- [x] T003 [P] Verify at least one user exists with SYSTEM_ADMIN role (check via database or existing admin account)

**Checkpoint**: 可啟動後端並確認有管理員帳號可用於測試

---

## Phase 2: Foundational（無額外基礎建設）

**Purpose**: 本功能僅修改既有服務層邏輯，無需新增基礎設施

**Checkpoint**: 直接進入 User Story 階段

---

## Phase 3: User Story 1 - 管理員可瀏覽系統內所有頁面 (Priority: P1) — MVP

**Goal**: 管理員（SYSTEM_ADMIN）登入後可正常進入並瀏覽系統中定義的每一個頁面，包含選單與直接路徑存取。

**Independent Test**: 以管理員帳號登入，依序開啟系統中所有已註冊的頁面（含子頁面），確認皆可正常載入且無權限阻擋。

### Implementation for User Story 1

- [x] T004 [US1] Add helper method `isAdminUser(UserPO user)` in `ResourcePageServiceImpl` to check if user has SYSTEM_ADMIN role code in `src/main/java/com/shuttleshout/service/impl/ResourcePageServiceImpl.java`
- [x] T005 [US1] Add helper method `getAllActiveResourcePages()` in `ResourcePageServiceImpl` to return all resource pages where `is_active = true` in `src/main/java/com/shuttleshout/service/impl/ResourcePageServiceImpl.java`
- [x] T006 [US1] Modify `getResourcePagesByUserId(Long userId)` to check if user is admin (via T004), and if so return all active resource pages (via T005) instead of querying role_resource_pages in `src/main/java/com/shuttleshout/service/impl/ResourcePageServiceImpl.java`
- [x] T007 [US1] Modify `hasPermission(Long userId, String resourcePageCode, String permission)` to check if user is admin (via T004), and if so return `true` immediately without querying role_resource_pages in `src/main/java/com/shuttleshout/service/impl/ResourcePageServiceImpl.java`

**Checkpoint**: 管理員登入後可開啟所有已啟用頁面；符合 spec 驗收情境 1–3、FR-001、FR-002、SC-001、SC-003

---

## Phase 4: User Story 2 - 管理員可見完整導航與可存取頁面列表 (Priority: P2)

**Goal**: 管理員登入後，導航選單或「我的可存取頁面」列表應顯示系統內所有已註冊且啟用的頁面，無遺漏。

**Independent Test**: 以管理員帳號登入，檢查導航/選單與「我的可存取頁面」清單，與系統定義之全部頁面比對，確認無遺漏。

### Implementation for User Story 2

**Note**: 本 User Story 的實作已由 US1 的 `getResourcePagesByUserId` 修改完成（管理員回傳全部已啟用頁面），無需額外實作。以下為驗證任務：

- [ ] T008 [US2] Verify `GET /resource-pages/my-accessible` endpoint returns all active resource pages when called by admin user (manual test or integration test)
- [ ] T009 [US2] Verify frontend navigation/menu displays all pages returned from `/my-accessible` endpoint (manual test)
  - **Note**: 後端已實作完成，請以管理員登入後呼叫 API 或檢查前端選單以驗收。

**Checkpoint**: 管理員之導航列表與系統已啟用頁面總數一致；符合 FR-003、SC-002

---

## Phase 5: Edge Cases & Non-Admin Behavior

**Purpose**: 確保邊界情況與非管理員行為不受影響

- [ ] T010 [P] Verify non-admin users still get pages only from role_resource_pages (no behavior change) - test `getResourcePagesByUserId` with non-admin user
- [ ] T011 [P] Verify non-admin users' `hasPermission` still works as before - test with non-admin user
- [ ] T012 [P] Verify user with multiple roles (including SYSTEM_ADMIN) still gets all pages - test `getResourcePagesByUserId` with user having SYSTEM_ADMIN + other roles
- [ ] T013 [P] Verify inactive (is_active = false) pages are NOT returned to admin - test `getResourcePagesByUserId` returns only active pages

**Checkpoint**: 非管理員行為不變；管理員僅取得已啟用頁面；符合 FR-005、SC-004

---

## Phase 6: Polish & Cross-Cutting

**Purpose**: 邊界情況與收尾

- [x] T014 [P] Add comments in Chinese (正體中文) explaining admin bypass logic in `ResourcePageServiceImpl.java`
- [ ] T015 Run quickstart.md validation manually: admin can access all pages, admin sees complete list, non-admin behavior unchanged
  - **Note**: 請依 `quickstart.md` 的「手動檢查建議」執行驗收。

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: No tasks - checkpoint only
- **User Story 1 (Phase 3)**: Depends on Setup completion
- **User Story 2 (Phase 4)**: Depends on User Story 1 completion (uses same modified methods)
- **Edge Cases (Phase 5)**: Can run in parallel with Phase 4 or after
- **Polish (Phase 6)**: Depends on all previous phases

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Setup (Phase 1) - No dependencies on other stories
- **User Story 2 (P2)**: Depends on User Story 1 (uses same `getResourcePagesByUserId` method)

### Within User Story 1

- T004 (helper method) → T005 (helper method) → T006 (uses T004, T005) → T007 (uses T004)
- T004 and T005 can be done in parallel
- T006 and T007 can be done in parallel after T004 completes

### Parallel Opportunities

- T002 and T003 can run in parallel (Phase 1)
- T004 and T005 can run in parallel (Phase 3)
- T006 and T007 can run in parallel after T004 completes (Phase 3)
- T010, T011, T012, T013 can all run in parallel (Phase 5)
- T014 and T015 can run in parallel (Phase 6)

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 3: User Story 1 (T004 → T005 → T006, T007)
3. **STOP and VALIDATE**: Test admin can access all pages
4. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo
4. Add Edge Cases → Test independently → Deploy/Demo
5. Each story adds value without breaking previous stories

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- No frontend changes needed - existing API endpoints work correctly with backend changes
- Admin role code is `SYSTEM_ADMIN` (constant string)
- Only active pages (`is_active = true`) are returned to admin
