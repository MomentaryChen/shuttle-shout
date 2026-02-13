# 更新日誌 (Changelog)

本文檔記錄專案的所有重要變更。

格式基於 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，
版本號遵循 [語義化版本](https://semver.org/lang/zh-CN/)。

## [未發布] - 2025-12-12

### 新增
- **文檔**
  - 新增 `doc/人員權限說明.md`，說明目前人員（用戶）與權限設計：角色、頁面資源、權限類型、後端判斷方式與相關檔案索引。
- **WebSocket 團隊叫號系統**
  - 新增 `TeamCallingWebSocketHandler` 處理實時 WebSocket 連接
  - 實現策略模式 (`MessageStrategyFactory`) 處理不同類型的 WebSocket 消息
  - 支援以下消息類型：
    - `ASSIGN_PLAYER` - 分配球員到場地
    - `REMOVE_PLAYER` - 從場地移除球員
    - `AUTO_ASSIGN` - 自動批量分配球員
    - `FINISH_MATCH` - 完成比賽
    - `COURT_UPDATE` - 場地狀態更新
    - `QUEUE_UPDATE` - 等待隊列更新
  - 自動更新數據庫 `team_courts` 表，實現實時球員分配的持久化
  - 支援多客戶端實時同步
  - 前端新增團隊叫號系統組件 (`team-calling-system.tsx`)

- **球場管理功能**
  - 新增 `CourtController` 和 `CourtService` 實現球場管理
  - 支援根據團隊ID獲取場地列表 (`/api/courts/team/{teamId}`)
  - 支援初始化團隊場地 (`/api/courts/team/{teamId}/initialize`)
  - 支援創建、更新、查詢場地
  - 支援球員分配到場地功能 (`assignPlayerToCourt`)
  - 支援從場地移除球員功能 (`removePlayerFromCourt`)
  - 支援批量更新場地球員 (`updateCourtPlayers`)
  - 支援清空場地球員信息 (`clearCourtPlayers`)

- **比賽管理功能**
  - 新增 `MatchService` 和 `MatchServiceImpl` 實現比賽管理
  - 支援創建比賽記錄 (`createMatch`)
  - 支援創建待開始比賽 (`createPendingMatch`)
  - 支援完成比賽 (`finishMatch`)
  - 支援查詢進行中的比賽 (`getOngoingMatches`)
  - 比賽狀態管理（ONGOING, FINISHED, CANCELLED）

- **用戶註冊功能**
  - 新增用戶註冊 API 端點 (`/api/auth/register`)
  - 新增 `RegisterRequest` DTO，支援用戶名、密碼、郵箱、手機號和真實姓名註冊
  - 前端新增註冊表單組件 (`register-form.tsx`)
  - 登錄頁面整合註冊對話框

- **用戶資料管理**
  - 新增個人資料頁面 (`/profile`)
  - 新增資料表單組件 (`profile-form.tsx`)
  - 支援查看和編輯用戶個人資訊
  - 更新認證上下文以支援用戶資料管理

- **全域異常處理**
  - 新增 `GlobalExceptionHandler` 統一異常處理
  - 新增 `ApiException` 自定義異常類
  - 新增 `ErrorResponse` 統一錯誤響應格式
  - 支援多種異常類型的統一處理：
    - 參數驗證異常 (`MethodArgumentNotValidException`)
    - 綁定異常 (`BindException`)
    - 約束違反異常 (`ConstraintViolationException`)
    - 認證異常 (`AuthenticationException`)
    - 權限不足異常 (`AccessDeniedException`)
    - 非法狀態/參數異常
    - 運行時異常和其他異常

- **團隊級別管理**
  - 新增 `TeamLevel` 枚舉類型（BEGINNER, INTERMEDIATE, ADVANCED, PROFESSIONAL）
  - 新增 `TeamLevelTypeHandler` 用於 MyBatis 類型轉換
  - 團隊 DTO 和實體類支援級別欄位
  - 前端團隊管理界面支援設置團隊級別

- **前端改進**
  - 重構多個頁面組件，提升用戶體驗
  - 更新側邊欄導航，添加個人資料入口
  - 改進團隊管理組件 (`team-manager.tsx`)
  - 更新用戶團隊概覽組件 (`user-team-overview.tsx`)
  - 新增比賽管理組件 (`match-management.tsx`)
  - 優化 API 調用和類型定義

### 修改
- **管理員完整頁面瀏覽權限**
  - 具 `SYSTEM_ADMIN` 角色的使用者現可瀏覽系統內所有已啟用的資源頁面，無需逐頁授權
  - 更新 `ResourcePageServiceImpl`：`getResourcePagesByUserId` 對管理員短路回傳所有已啟用頁面；`hasPermission` 對管理員一律回傳 true
  - 新增輔助方法 `isAdminUser(UserPO)`、`getAllActiveResourcePages()`，並補充正體中文註解
  - 非管理員之可存取頁面與權限檢查行為維持不變

- **後端改進**
  - 更新 `AuthController` 支援註冊功能
  - 更新 `TeamController` 和相關服務支援團隊級別
  - 更新多個 DTO 類 (`TeamDTO`, `TeamCreateDTO`, `TeamUpdateDTO`)
  - 更新 `TeamPO` 和 `UserPO` 實體類
  - 改進控制器日誌記錄切面 (`ControllerLoggingAspect`)
  - 優化 WebSocket 配置 (`WebSocketConfig`, `SimpleWebSocketConfig`)
  - 改進 `CourtService` 實現，支援球員分配和移除操作
  - 完善 `MatchService` 實現，支援比賽生命週期管理

- **前端改進**
  - 更新所有主要頁面組件
  - 改進登錄表單組件
  - 更新系統設置組件
  - 優化 API 客戶端 (`api.ts`)
  - 更新類型定義 (`types/api.ts`)
  - 改進 WebSocket 連接管理和錯誤處理

### 修復
- **球場管理**
  - 後端：新增 `GET /courts`（全部球場）、`GET /courts/active`（活躍球場），供前端 `courtApi.getAll()`、`courtApi.getActive()` 使用；新增 `PUT /courts/{id}`，並改為僅更新請求體中傳入的欄位，避免部分更新時覆寫為 null
  - 前端：新增球場管理頁面 (`app/court-management/page.tsx`) 與元件 `CourtManagement`，可檢視所有球場、所屬團隊、啟用狀態並切換啟用/停用
  - 儀表板「球場管理」連結現可正常進入並顯示資料；叫號系統與人員配置載入球場資料不再 404

### 文檔
- 新增 Cursor 規則 `changelog.mdc`：每次改動都需同步更新 CHANGELOG
- 新增 `README_ZH.md` 中文版專案說明
- 更新 `README.md` 專案文檔
- 新增專案截圖展示
- 新增 `TEAM_CALLING_UPDATE_SUMMARY.md` 團隊叫號系統更新文檔

## [0.1.0] - 2025-12-11

### 新增
- **初始版本發布**
  - 專案初始化和基礎架構搭建
  - 前端：React 19 + Next.js 16 + TypeScript + Tailwind CSS
  - 後端：Spring Boot 3.2.0 + Java 17 + Gradle
  - 數據庫：H2 (開發) / PostgreSQL (生產)

- **核心功能**
  - 用戶認證和授權系統
    - JWT 令牌認證
    - 基於角色的訪問控制 (RBAC)
    - 用戶登錄功能
  - 球員管理
    - 創建和管理球員資訊
    - 搜尋球員
    - 更新球員資訊
  - 球場管理
    - 創建和管理球場
    - 啟用/禁用球場
    - 查看球場狀態
  - 叫號佇列系統
    - 加入等待佇列
    - 叫號功能
    - 完成服務
    - 取消佇列
    - 查看等待列表
  - 團隊管理
    - 創建和管理團隊
    - 團隊成員管理
  - 資源頁面管理
    - 資源頁面配置
    - 角色資源關聯

- **API 文檔**
  - Swagger UI 整合
  - API 文檔自動生成

---

## 變更類型說明

- **新增**: 新功能
- **修改**: 現有功能的變更
- **棄用**: 即將移除的功能
- **移除**: 已移除的功能
- **修復**: 錯誤修復
- **安全**: 安全相關的修復
