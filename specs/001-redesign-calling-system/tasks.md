# Tasks: 叫號系統重新設計與結束流程修正

**Input**: Design documents from `/specs/001-redesign-calling-system/`  
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Spec 未明確要求 TDD；任務以實作為主，必要時可補驗收或手動依 quickstart 驗證。

**Organization**: 依 User Story 分階段，US1（結束即離開）為 MVP，可獨立驗收。

## Format: `[ID] [P?] [Story] Description`

- **[P]**: 可與同階段其他任務並行（不同檔案、無依賴）
- **[Story]**: 所屬 User Story（US1, US2, US3）
- 描述中含具體檔案路徑

## Path Conventions

- **Backend**: `src/main/java/com/shuttleshout/`
- **Frontend**: `shuttle-shout-frontend/`（含 `app/`, `components/`）

---

## Phase 1: Setup（確認環境與文件）

**Purpose**: 確認分支與既有程式庫，無新專案初始化

- [x] T001 Confirm feature branch `001-redesign-calling-system` and design docs (spec.md, plan.md, quickstart.md) in specs/001-redesign-calling-system/
- [x] T002 [P] Verify backend runs: `./gradlew bootRun` and frontend runs: `cd shuttle-shout-frontend && npm run dev` from repo root

**Checkpoint**: 可開啟叫號頁並連線，作為後續修改基準

---

## Phase 2: Foundational（無額外基礎建設）

**Purpose**: 本功能沿用既有 WebSocket 與 Queue/Court 服務，無需新增基礎設施

**Checkpoint**: 直接進入 User Story 階段

---

## Phase 3: User Story 1 - 按下「結束」後直接離開叫號系統頁面 (Priority: P1) — MVP

**Goal**: 使用者在叫號頁點擊「結束」後，系統結束連線並導向離開該頁面（如上一頁），不再僅斷線而停留。

**Independent Test**: 進入叫號系統頁並連線後，點擊「結束」→ 應離開該頁面（例如回到上一頁）；未連線時點「返回」仍可離開。

### Implementation for User Story 1

- [x] T003 [US1] Add optional prop `onEndSession?: () => void` to `TeamCallingSystem` in shuttle-shout-frontend/components/team-calling-system.tsx
- [x] T004 [US1] In 結束 button onClick: if connected, call existing clearQueue logic (send CLEAR_QUEUE), then disconnectWebSocket(), then onEndSession?.(); if not connected, call onEndSession?.() only in shuttle-shout-frontend/components/team-calling-system.tsx
- [x] T005 [US1] In team-calling page, pass onEndSession callback that performs router.back() with short delay (e.g. setTimeout 300ms) to match 返回 behavior in shuttle-shout-frontend/app/team-calling/page.tsx

**Checkpoint**: 連線後點「結束」會離開叫號頁；符合 spec 驗收情境 1–2、FR-001、SC-001

---

## Phase 4: User Story 2 - 叫號邏輯正確且可預期 (Priority: P1)

**Goal**: 等待隊列排序與分配符合先到先服務；場地／隊列狀態更新後同步至所有連線客戶端，無錯號或狀態不同步。

**Independent Test**: 依序加入等待隊列、分配、結束比賽，重複數次比對隊列順序與場地分配；多開分頁確認狀態一致。

### Implementation for User Story 2

- [x] T006 [P] [US2] Verify QueueService (or repository) returns WAITING queues ordered by queue_number asc, then createdAt asc in src/main/java/com/shuttleshout/service/ and related repository
- [x] T007 [US2] Ensure AutoAssignStrategy and RestoreStateStrategy use the same WAITING queue ordering when selecting next players in src/main/java/com/shuttleshout/handler/strategy/AutoAssignStrategy.java and RestoreStateStrategy.java
- [x] T008 [US2] Verify FinishMatchStrategy and ClearQueueStrategy trigger broadcast (e.g. sendWaitingQueueUpdate or equivalent) so all connected clients receive queue/court updates in src/main/java/com/shuttleshout/handler/strategy/FinishMatchStrategy.java and ClearQueueStrategy.java

**Checkpoint**: 叫號順序可重現、多端狀態一致；符合 FR-002、FR-003、FR-004、SC-002、SC-003

---

## Phase 5: User Story 3 - 叫號會話結束時狀態清理完整 (Priority: P2)

**Goal**: 透過「結束」離開時，後端正確清理該團隊等待隊列；再次進入時所見與後端一致。

**Independent Test**: 叫號進行中點「結束」離開，再進入同一團隊叫號頁，確認隊列與場地顯示與後端一致、無殘留。

### Implementation for User Story 3

- [x] T009 [US3] Confirm 結束 flow in TeamCallingSystem sends CLEAR_QUEUE (via existing handleClearQueue) before disconnect and onEndSession in shuttle-shout-frontend/components/team-calling-system.tsx
- [x] T010 [US3] Verify ClearQueueStrategy deletes all WAITING queues for the team and broadcasts QUEUE_UPDATE (empty list) to all sessions for that team in src/main/java/com/shuttleshout/handler/strategy/ClearQueueStrategy.java

**Checkpoint**: 離開後再進入狀態一致；符合 FR-005、SC-004

---

## Phase 6: Polish & Cross-Cutting

**Purpose**: 邊界情況與收尾

- [x] T011 [P] Prevent double submit on 結束: disable button or set flag after click until navigation occurs in shuttle-shout-frontend/components/team-calling-system.tsx
- [ ] T012 Run quickstart.md validation manually: 結束即離開、叫號順序、再次進入狀態一致

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: 無依賴，可先執行
- **Phase 2**: 無任務，僅說明
- **Phase 3 (US1)**: 依賴 Phase 1 完成；US1 可獨立完成並驗收（MVP）
- **Phase 4 (US2)**: 可與 US1 並行或之後做；依賴既有後端程式庫
- **Phase 5 (US3)**: 與 US1 重疊（結束流程含 CLEAR_QUEUE）；T009/T010 可於 US1 實作後驗證
- **Phase 6 (Polish)**: 建議在 US1–US3 完成後執行

### User Story Dependencies

- **US1**: 無依賴其他 story；完成即達「結束即離開」MVP
- **US2**: 無依賴 US1；可與 US1 並行（不同檔案）
- **US3**: 依賴 US1 的「結束」流程會送 CLEAR_QUEUE；T009/T010 為驗證與後端確認

### Parallel Opportunities

- T002 可與 T001 並行
- T006、T007、T008 中 T006 與 T007/T008 可不同人並行（查詢層 vs 策略層）
- T011 可與 T012 並行

---

## Parallel Example: User Story 1

```text
# US1 為單一流程，建議順序：
T003 (加 prop) → T004 (結束按鈕邏輯) → T005 (page 傳入 onEndSession)
```

---

## Implementation Strategy

### MVP First（僅 User Story 1）

1. 完成 Phase 1（T001–T002）
2. 完成 Phase 3（T003–T005）
3. **停下來驗收**：連線後點「結束」是否離開頁面
4. 可先交付或再做 US2/US3

### Incremental Delivery

1. Phase 1 → Phase 3 → 驗收 US1（MVP）
2. Phase 4 → 驗收 US2（叫號與多端一致）
3. Phase 5 → 驗收 US3（離開後再進入一致）
4. Phase 6 → 防重複與 quickstart 驗證

### Suggested Order for Single Developer

1. T001, T002  
2. T003, T004, T005（US1）  
3. T009, T010（US3，確認結束流程與後端清理）  
4. T006, T007, T008（US2）  
5. T011, T012（Polish）

---

## Notes

- 所有任務均含 checkbox、Task ID、必要時 [P]/[US?]、以及檔案路徑
- [P] 表示可與同階段其他任務並行
- 每個 User Story 可獨立驗收；US1 為最小可交付範圍
- 完成單一任務或一組任務後建議 commit
- 避免：模糊描述、同一檔案多人同時改、跨 story 未協調的依賴
