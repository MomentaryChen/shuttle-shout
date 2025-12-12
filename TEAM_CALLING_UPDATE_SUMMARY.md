# Team Calling 自動更新 team_courts 功能完成 ✅

## 完成的更新

### 🎯 核心功能
Team Calling 叫號系統現在會**自動更新數據庫**中的 `team_courts` 表，實現實時球員分配的持久化。

### ✅ 具體變更

#### 1. **TeamCallingWebSocketHandler.java** 更新

##### 新增的處理方法：

- **`handleAssignPlayer()`**
  - 處理單個球員分配到場地
  - 自動調用 `courtService.assignPlayerToCourt()` 更新數據庫
  - 支持的消息類型：`ASSIGN_PLAYER`

- **`handleRemovePlayer()`**
  - 處理從場地移除球員
  - 自動調用 `courtService.removePlayerFromCourt()` 更新數據庫
  - 支持的消息類型：`REMOVE_PLAYER`

- **更新 `handleAutoAssign()`**
  - 現在會實際更新數據庫
  - 調用 `courtService.updateCourtPlayers()` 批量更新
  - 支持的消息類型：`AUTO_ASSIGN`

##### 新增的工具方法：

- **`extractPlayerId()`** - 從多種格式中提取球員 ID
- **`convertToLong()`** - 安全的類型轉換（Long, Integer, String → Long）
- **`convertToInteger()`** - 安全的類型轉換（Integer, Long, String → Integer）

## 🔄 工作流程

### 流程 1：分配球員
```
前端發送消息 → WebSocket 接收 → handleAssignPlayer()
                                        ↓
                                  驗證參數
                                        ↓
                            courtService.assignPlayerToCourt()
                                        ↓
                            更新數據庫 team_courts 表
                                        ↓
                            廣播 PLAYER_ASSIGNED 給所有客戶端
```

### 流程 2：移除球員
```
前端發送消息 → WebSocket 接收 → handleRemovePlayer()
                                        ↓
                                  驗證參數
                                        ↓
                            courtService.removePlayerFromCourt()
                                        ↓
                            更新數據庫（設為 NULL）
                                        ↓
                            廣播 PLAYER_REMOVED 給所有客戶端
```

### 流程 3：自動批量分配
```
前端發送消息 → WebSocket 接收 → handleAutoAssign()
                                        ↓
                                  驗證並解析參數
                                        ↓
                            courtService.updateCourtPlayers()
                                        ↓
                            批量更新數據庫（所有 4 個位置）
                                        ↓
                            廣播 AUTO_ASSIGN_SUCCESS 給所有客戶端
```

## 📡 支持的 WebSocket 消息

### 1. 分配單個球員
```json
{
  "type": "ASSIGN_PLAYER",
  "courtId": 5,
  "userId": 123,
  "position": 1
}
```

### 2. 移除單個球員
```json
{
  "type": "REMOVE_PLAYER",
  "courtId": 5,
  "position": 2
}
```

### 3. 自動批量分配
```json
{
  "type": "AUTO_ASSIGN",
  "courtId": 5,
  "teamId": 10,
  "assignments": {
    "1": { "id": 101, "name": "張三" },
    "2": { "id": 102, "name": "李四" },
    "3": { "id": 103, "name": "王五" },
    "4": { "id": 104, "name": "趙六" }
  }
}
```

## 💾 數據庫更新

| 操作 | SQL 更新 | 說明 |
|------|---------|------|
| 分配到位置 1 | `UPDATE team_courts SET player1_id = ? WHERE id = ?` | 更新第 1 位球員 |
| 分配到位置 2 | `UPDATE team_courts SET player2_id = ? WHERE id = ?` | 更新第 2 位球員 |
| 分配到位置 3 | `UPDATE team_courts SET player3_id = ? WHERE id = ?` | 更新第 3 位球員 |
| 分配到位置 4 | `UPDATE team_courts SET player4_id = ? WHERE id = ?` | 更新第 4 位球員 |
| 移除球員 | `UPDATE team_courts SET player{N}_id = NULL WHERE id = ?` | 清空指定位置 |
| 批量更新 | `UPDATE team_courts SET player1_id=?, player2_id=?, player3_id=?, player4_id=? WHERE id=?` | 一次更新所有位置 |

## ✨ 主要優勢

1. ✅ **自動持久化** - 所有 WebSocket 操作都自動保存到數據庫
2. ✅ **實時同步** - 所有連接的客戶端立即收到更新
3. ✅ **完整驗證** - 參數驗證 + 類型轉換 + 錯誤處理
4. ✅ **靈活格式** - 支持多種參數格式（Map、Integer、String）
5. ✅ **詳細日誌** - 每個操作都有完整的日誌記錄
6. ✅ **錯誤回饋** - 操作失敗時發送錯誤消息給客戶端

## 🔍 測試驗證

### 測試場景 1：分配球員後檢查數據庫
```sql
SELECT id, name, player1_id, player2_id, player3_id, player4_id 
FROM team_courts WHERE id = 5;
```

### 測試場景 2：檢查日誌
```
[INFO] 處理球員分配: courtId=5, userId=123, position=1
[INFO] 成功將球員 123 分配到場地 5 的位置 1
```

### 測試場景 3：多客戶端同步
- 打開多個瀏覽器標籤
- 在一個標籤中分配球員
- 其他標籤應立即顯示更新

## 📋 代碼質量

- ✅ 編譯通過（無錯誤）
- ✅ 只有輕微的 null safety 警告（已妥善處理）
- ✅ 遵循 Spring Boot 最佳實踐
- ✅ 完整的異常處理
- ✅ 詳細的代碼註釋

## 📚 相關文件

- **更新的文件**：
  - `src/main/java/com/shuttleshout/controller/TeamCallingWebSocketHandler.java`
  
- **依賴的服務**：
  - `src/main/java/com/shuttleshout/service/CourtService.java`
  - `src/main/java/com/shuttleshout/service/impl/CourtServiceImpl.java`

- **數據庫 Schema**：
  - `data/1. shuttleshout_schema.sql`

- **詳細文檔**：
  - `data/TEAM_CALLING_COURT_UPDATE.md`

## 🎉 結論

Team Calling 系統現在完全整合了 `team_courts` 表的更新功能！

所有通過 WebSocket 進行的球員分配操作都會：
1. ✅ 實時更新數據庫
2. ✅ 廣播給所有客戶端
3. ✅ 記錄詳細日誌
4. ✅ 處理錯誤並回饋

---

**完成時間**：2024年12月  
**狀態**：✅ 已完成並通過編譯  
**測試狀態**：⏳ 待測試

