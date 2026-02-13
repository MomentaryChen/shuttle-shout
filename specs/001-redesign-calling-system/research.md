# Research: 001-redesign-calling-system

**Branch**: `001-redesign-calling-system` | **Phase**: 0

## 1. 結束按鈕行為：斷線後是否先清理再導向

**Decision**: 點擊「結束」時，先發送清除等待隊列（CLEAR_QUEUE）、關閉 WebSocket，再執行導向離開；若未連線則直接導向。

**Rationale**: Spec FR-001、FR-005 要求結束時離開頁面且會話狀態需清理；現有「返回」已具備 clearQueue + 導向，將「結束」對齊此流程可避免僅斷線仍留頁的認知落差，並滿足「離開後再次進入狀態與後端一致」（SC-004）。

**Alternatives considered**:
- 僅斷線不發 CLEAR_QUEUE：會導致下次進入時仍看到舊等待隊列，違反 FR-005 / SC-004。
- 僅導向、不主動斷線：關閉頁或離開時瀏覽器會斷線，但未先送 CLEAR_QUEUE 仍可能留下後端等待隊列，故不採用。

---

## 2. 叫號順序與狀態一致性

**Decision**: 沿用現有「先到先服務」：後端以 `queueNumber` 升序、其次 `createdAt` 升序取得 WAITING 隊列；分配與結束比賽後透過既有 WebSocket 廣播更新隊列與場地狀態，確保所有連線客戶端收到相同訊息。

**Rationale**: Spec FR-002、FR-003 要求叫號可預期且多端一致；程式已有 `QueueService.getQueuesByTeamIdAndStatus` 與排序邏輯，以及 `sendWaitingQueueUpdate` / MATCH_FINISHED 等廣播，只需確認單一真實來源（後端）與廣播時機、順序無遺漏或競態。

**Alternatives considered**:
- 前端主導排序：易與後端不同步，不符「後端為單一真實來源」。
- 新增獨立「結束會話」WebSocket 訊息：目前 CLEAR_QUEUE 已能清理等待隊列，無需新增訊息類型；必要時僅需在文件註明「結束」即 CLEAR_QUEUE + 關閉連線。

---

## 3. 前端「結束」與導向的責任切分

**Decision**: 由 `TeamCallingSystem` 在「結束」點擊時執行：若有連線則呼叫現有 clearQueue 邏輯、關閉 WebSocket，再呼叫頁面傳入的 `onEndSession` 回調；`team-calling/page.tsx` 提供 `onEndSession={() => router.back()}`（可加短延遲以確保 CLEAR_QUEUE 送出）。

**Rationale**: 導向屬路由層（page），斷線與送 CLEAR_QUEUE 屬叫號元件；透過回調可保持單向資料流且不把 router 注入元件，符合 React 慣例。

**Alternatives considered**:
- 在元件內使用 `useRouter` 並直接 `router.back()`：可行但將路由與元件耦合，不利測試與重用。
- 僅靠「返回」按鈕、不改「結束」：與 spec「按下結束應直接離開」不符。
