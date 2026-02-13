# Data Model: 001-redesign-calling-system

**Branch**: `001-redesign-calling-system` | **Phase**: 1

本功能不新增資料表，僅依 spec 關鍵實體對應既有模型與狀態轉換，供「結束即離開」與叫號邏輯修正時參考。

## 對應 Spec 關鍵實體

| Spec 實體 | 對應儲存 / 運行時 | 說明 |
|-----------|-------------------|------|
| 叫號會話 | 無持久化；WebSocket session 與前端頁面生命週期 | 結束時透過 CLEAR_QUEUE 清理該團隊 WAITING 隊列，並關閉連線 |
| 等待隊列 | `queues` 表，`status = WAITING` | 排序依 `queue_number` 升序、其次 `created_at` 升序；結束會話時可刪除該團隊所有 WAITING 記錄 |
| 場地 | `team_courts` (Court) | 含 player1_id..player4_id、match_started_at、is_active；狀態由分配／結束比賽更新 |
| 分配 | `queues`（SERVED / WAITING）+ `team_courts` 四格 | 待確認：SERVED 未開賽；確認後寫入 court 並可標記比賽開始；取消則還原隊列與場地 |

## 既有主要實體（摘要）

### Queue (`queues`)

- **id**, **player_id**（對應 Player），**court_id**（可空），**status**：WAITING | CALLED | SERVED | CANCELLED  
- **queue_number**, **created_at**, **called_at**, **served_at**, **updated_at**  
- **驗證 / 規則**：叫號順序依 queue_number 升序、createdAt 升序；CLEAR_QUEUE 僅刪除該 teamId 下 status=WAITING 的記錄。

### Court (`team_courts`)

- **id**, **team_id**, **name**  
- **player1_id**..**player4_id**（UserPO），**match_started_at**, **match_ended_at**, **is_active**  
- 空場：四格皆空；待確認：有分配但未確認開始；進行中：is_active 且 match_started_at 已設。

### Player (`players`)

- **id**, **team_id**, **name**, **phone_number**, **notes**, **created_at**, **updated_at**  
- 用於關聯 User 與 Team 的球員資料；Queue 以 player_id 關聯。

### Match (`matches`)

- **id**, **team_id**, **court_id**, **player1_id**..**player4_id**, **status**（PENDING_CONFIRMATION | ONGOING | FINISHED | CANCELLED）, **started_at**, **ended_at**  
- 記錄每場比賽；結束比賽時更新 status / ended_at，並將球員從場地清空、隊列狀態更新。

## 狀態轉換（叫號與結束）

1. **加入等待隊列**：新增 Queue(status=WAITING)，並設定 queue_number / created_at。  
2. **分配至場地（待確認）**：可建立 Queue(court_id, status=WAITING 或 SERVED 依實作)；前端／後端標記該場地為「待確認」。  
3. **確認開始**：更新 Court 的 player1..4、match_started_at、is_active；Queue 對應記錄改為 SERVED；Match 建立或更新為 ONGOING。  
4. **取消待確認**：移除該場地分配、還原隊列（刪除或改回 WAITING）。  
5. **結束比賽**：Match 設為 FINISHED、ended_at；Court 清空四格、is_active=false；已服務的 Queue 可保留或依產品規則清理。  
6. **結束會話（點擊「結束」）**：發送 CLEAR_QUEUE，後端刪除該團隊所有 WAITING 的 Queue；前端關閉 WebSocket 並導向離開。  

以上確保「離開後再次進入」時，等待隊列與後端一致（SC-004），且叫號順序可預期（FR-002）。
