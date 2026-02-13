# WebSocket 團隊叫號契約（本功能相關）

**Branch**: `001-redesign-calling-system`  
**Scope**: 結束會話與隊列清理；其餘訊息類型為既有契約，僅列本功能會用到的。

## 連線

- **Endpoint**: 依現有 `WebSocketConfig`（如 `/ws/team-calling?teamId={teamId}`）。
- **Server → Client**: `CONNECTED` — 連線建立後發送，payload 含 `teamId`。

## 結束會話流程（本功能實作）

「結束」按鈕行為由前端實作，無新增訊息類型：

1. **Client**（可選）：發送 `CLEAR_QUEUE`，見下。
2. **Client**：關閉 WebSocket 連線。
3. **Client**：執行導向離開（例如 `router.back()`），由 page 透過 `onEndSession` 回調提供。

若未連線，Client 僅執行導向。

## CLEAR_QUEUE（會話結束時清理等待隊列）

**Client → Server**

```json
{
  "type": "CLEAR_QUEUE",
  "teamId": "<number|string>"
}
```

**Server → Client**（單一 session 回應）

```json
{
  "type": "CLEAR_QUEUE_SUCCESS",
  "teamId": "<number>",
  "deletedCount": "<number>",
  "message": "<string>"
}
```

**Server**：另會對該團隊廣播 `QUEUE_UPDATE`，payload 為空陣列，使其他連線客戶端同步清空等待隊列顯示。

## 既有訊息類型（本功能會碰到）

| type | 方向 | 用途 |
|------|------|------|
| `QUEUE_UPDATE` | Server → Client | 廣播等待隊列；CLEAR_QUEUE 後會收到空陣列 |
| `MATCH_FINISHED` | Server → Client | 比賽結束，客戶端同步場地與隊列狀態 |
| `ERROR` | Server → Client | 錯誤，payload 含 `message` |

其餘類型（AUTO_ASSIGN、CONFIRM_START_MATCH、FINISH_MATCH 等）見既有 handler 策略，本規格不變更其契約。
