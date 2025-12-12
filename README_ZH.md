# 羽毛球叫號系統 (Shuttle Shout)

## 專案簡介

這是一個完整的羽毛球叫號系統，包含前端和後端兩部分。系統用於管理球員、球場和叫號佇列，幫助羽毛球場館高效管理排隊叫號流程。

## 當前開發狀態

**注意：** 本專案目前正在積極開發中。目前已實現的功能：

- ✅ 用戶認證與授權（註冊、登錄、JWT令牌）
- ✅ 用戶資料管理
- ✅ 團隊管理（創建、更新、刪除團隊，成員管理，團隊等級）
- ✅ 資源頁面管理和基於角色的訪問控制
- ✅ WebSocket 實時團隊叫號系統
- ✅ 球場管理（創建、更新、查詢球場，球員分配）
- ✅ 比賽管理（創建、完成比賽，追蹤比賽狀態）

其他功能（球員管理、佇列歷史記錄、統計報表）計劃在未來版本中發布。

## 技術棧

### 前端
- **React 19**
- **Next.js 16**
- **TypeScript**
- **Tailwind CSS**
- **Radix UI**

### 後端
- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **Gradle**
- **H2 Database** (開發) / **PostgreSQL** (生產)

## 專案結構

```
shuttle-shout/
├── shuttle-shout-frontend/  # React前端專案
│   ├── app/                 # Next.js應用目錄
│   ├── components/          # React組件
│   ├── hooks/               # 自定義Hooks
│   └── lib/                 # 工具庫
│
└── src/                     # Spring Boot後端專案
    ├── main/java/com/shuttleshout/
    │   ├── controller/       # REST API控制器
    │   ├── service/          # 業務邏輯層
    │   ├── repository/       # 數據訪問層
    │   └── model/            # 實體類和DTO
    └── resources/            # 配置文件
```

## 快速開始

### 前置要求
- Node.js 18+ 和 npm/pnpm
- JDK 17+
- Gradle 8.5+ (或使用專案自帶的 Gradle Wrapper)

### 啟動前端

```bash
cd shuttle-shout-frontend
npm install  # 或 pnpm install
npm run dev  # 或 pnpm dev
```

前端將在 http://localhost:3000 運行

### 啟動後端

```bash
# Windows
gradlew.bat bootRun

# Linux/Mac
./gradlew bootRun
```

後端將在 http://localhost:8080/api 運行

### WebSocket 連接

團隊叫號系統的 WebSocket 端點：
- WebSocket URL: `ws://localhost:18080/api/ws?teamId={teamId}`

### API文檔

啟動後端後，訪問以下地址查看API文檔：
- Swagger UI: http://localhost:8080/api/swagger-ui.html
- API Docs: http://localhost:8080/api/api-docs

## 功能特性

### 用戶認證與授權
- ✅ 用戶註冊和登錄
- ✅ 基於角色的訪問控制 (RBAC)
- ✅ JWT 令牌認證
- ✅ 頁面資源管理
- ✅ 用戶資料管理

### 團隊管理
- ✅ 創建和管理團隊
- ✅ 團隊成員管理
- ✅ 團隊等級管理

### 球員管理
- ⏳ 創建和管理球員資訊 (計劃中)
- ⏳ 搜尋球員 (計劃中)
- ⏳ 更新球員資訊 (計劃中)

### 球場管理
- ✅ 創建和管理球場
- ✅ 初始化團隊球場
- ✅ 分配球員到球場
- ✅ 從球場移除球員
- ✅ 查看球場狀態和球員分配
- ✅ 批量更新球場球員

### 團隊叫號系統 (WebSocket)
- ✅ 實時 WebSocket 通信
- ✅ 實時分配球員到球場
- ✅ 從球場移除球員
- ✅ 自動分配球員到球場
- ✅ 完成比賽
- ✅ 實時球場狀態更新
- ✅ 實時佇列更新
- ✅ 多客戶端同步

### 比賽管理
- ✅ 創建比賽
- ✅ 追蹤比賽狀態（進行中、已完成、已取消）
- ✅ 完成比賽
- ✅ 查詢進行中的比賽
- ✅ 比賽歷史記錄

### 叫號佇列
- ⏳ 加入等待佇列 (計劃中)
- ⏳ 佇列歷史記錄 (計劃中)
- ⏳ 統計報表功能 (計劃中)

## 界面演示

以下是系統主要功能的界面截圖：

### 登錄頁面
![登錄頁面](image/login.png)

### 個人頁面
![個人頁面](image/personal-page.png)

### 團隊概覽
![團隊概覽](image/team-overview.png)

### 創建團隊
![創建團隊](image/create-team.png)

### 編輯團隊
![編輯團隊](image/edit-team.png)

### 團隊成員管理
![團隊成員管理](image/team-members.png)

### 匿名團隊概覽
![匿名團隊概覽](image/anosy-team-overview.png)

## 開發計劃

- [x] WebSocket即時通知
- [x] 球場管理
- [x] 比賽管理
- [x] 用戶認證和授權
- [ ] 佇列歷史記錄
- [ ] 統計報表功能
- [ ] 多語言支援

## 許可證

MIT License
