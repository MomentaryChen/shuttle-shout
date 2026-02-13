# Quickstart: 001-redesign-calling-system

**Branch**: `001-redesign-calling-system`

## 目標

1. **結束即離開**：叫號頁「結束」按鈕改為「清理等待隊列（可選）→ 斷線 → 導向離開」。
2. **叫號邏輯**：確認後端排序與廣播一致，無錯號／狀態不同步。

## 前置

- Java 8、Node 18+、MySQL。
- 後端：`./gradlew bootRun`（或依專案說明啟動）。
- 前端：`cd shuttle-shout-frontend && npm run dev`。
- 登入為團隊創建者，進入團隊叫號頁（帶 `teamId`）。

## 實作要點

### 前端

- **`shuttle-shout-frontend/components/team-calling-system.tsx`**
  - 新增 prop：`onEndSession?: () => void`。
  - 「結束」按鈕 onClick：若已連線則呼叫現有 clearQueue 邏輯（送 `CLEAR_QUEUE`）、`disconnectWebSocket()`，然後 `onEndSession?.()`；若未連線則直接 `onEndSession?.()`。
  - 可加防重複：點擊後禁用按鈕或設 flag，直到導向發生。
- **`shuttle-shout-frontend/app/team-calling/page.tsx`**
  - 傳入 `onEndSession`：例如 `() => { setTimeout(() => router.back(), 300) }`，與現有「返回」延遲一致。

### 後端

- **叫號順序**：確認 `QueueService.getQueuesByTeamIdAndStatus(teamId, WAITING)` 的排序為 queue_number 升序、createdAt 升序；AutoAssignStrategy / RestoreStateStrategy 等使用同一順序。
- **狀態廣播**：確認 `FinishMatchStrategy` 與 `ClearQueueStrategy` 在更新後有呼叫 `sendWaitingQueueUpdate` 或等同廣播，使所有連線客戶端收到一致狀態。
- 無需新增 WebSocket 訊息類型；必要時補單元／整合測試覆蓋 CLEAR_QUEUE 與結束比賽後狀態。

## 驗收對照

- Spec 驗收情境 1–2（User Story 1）：連線後點「結束」→ 離開叫號頁。
- Spec 驗收情境 2（User Story 2）：分配與結束比賽後，多端狀態一致。
- Spec SC-001：2 秒內完成導向；SC-003：3 秒內多端一致。

## 文件

- [spec.md](./spec.md) — 功能規格  
- [plan.md](./plan.md) — 實作計畫  
- [research.md](./research.md) — 決策與替代方案  
- [data-model.md](./data-model.md) — 實體與狀態轉換  
- [contracts/websocket-team-calling.md](./contracts/websocket-team-calling.md) — 本功能相關 WebSocket 契約  
