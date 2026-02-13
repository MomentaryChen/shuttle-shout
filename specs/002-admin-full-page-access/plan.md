# Implementation Plan: 管理員完整頁面瀏覽權限

**Branch**: `002-admin-full-page-access` | **Date**: 2025-02-13 | **Spec**: [spec.md](./spec.md)  
**Input**: Feature specification from `/specs/002-admin-full-page-access/spec.md`

## Summary

管理員（具 SYSTEM_ADMIN 角色）須能瀏覽系統內所有已定義、已啟用頁面，且導航／可存取頁面列表須涵蓋全部頁面。目前可存取頁面與權限檢查皆依「角色–頁面」關聯表決定，導致未逐頁授權給管理員時部分頁面無法瀏覽。實作策略：在後端「依使用者回傳可存取頁面」與「檢查頁面權限」兩處，若偵測到使用者具管理員角色，則短路回傳「全部頁面」與「有權限」，不依關聯表過濾；不新增 API、不變更資料表結構，符合 MVP 與避免過度設計。

## Technical Context

**Language/Version**: Java 8 (backend), TypeScript (frontend)  
**Primary Dependencies**: Spring Boot 2.7.18, Spring Security, MyBatis-Flex, MySQL；前端 Vite / React、pnpm  
**Storage**: MySQL（users, roles, resource_pages, role_resource_pages 等既有表）  
**Testing**: JUnit 5 (backend)，既有測試慣例  
**Target Platform**: JVM (backend)，Web 瀏覽器 (frontend)  
**Project Type**: Web 應用（後端 `src/`，前端 `shuttle-shout-frontend/`）  
**Performance Goals**: 管理員取得可存取頁面列表與權限檢查維持與現有查詢同數量級，無額外 N+1  
**Constraints**: 不新增 API 端點、不變更資料庫 schema；非管理員行為不變  
**Scale/Scope**: 既有角色與資源頁面數量級，單次請求回傳全站頁面列表可接受

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **高品質**: 變更限於服務層與必要之角色判斷，程式可讀、與既有風格一致。
- **可測試性**: `getResourcePagesByUserId` 與 `hasPermission` 在「使用者具 SYSTEM_ADMIN」時之行為可透過單元或整合測試驗證；對應 spec 驗收情境。
- **MVP 優先**: 僅實作「管理員視為擁有全部頁面存取」，不擴充其他權限維度。
- **避免過度設計**: 不引入新表、新端點、新框架；以現有角色碼 SYSTEM_ADMIN 短路即可。
- **正體中文**: 註解與介面文字使用正體中文（本需求未新增使用者可見字串，註解依正體中文撰寫）。

*Re-check after Phase 1*: 設計僅涉及既有 ResourcePageService 行為擴充與 contracts 行為說明，無架構或依賴變更，合規維持通過。

## Project Structure

### Documentation (this feature)

```text
specs/002-admin-full-page-access/
├── plan.md              # 本檔
├── research.md          # Phase 0
├── data-model.md        # Phase 1
├── quickstart.md        # Phase 1
├── contracts/           # Phase 1（API 行為變更說明）
└── tasks.md             # Phase 2（/speckit.tasks 產出，非 plan 建立）
```

### Source Code (repository root)

```text
src/main/java/com/shuttleshout/
├── controller/
│   └── ResourcePageController.java   # 無簽章變更；行為依服務層
├── service/
│   ├── ResourcePageService.java      # 介面不變
│   └── impl/
│       └── ResourcePageServiceImpl.java  # getResourcePagesByUserId、hasPermission 加入管理員短路
├── common/util/
│   └── SecurityUtil.java             # 既有 hasRole(String) 可複用
└── (repository、model 等既有結構不變)

shuttle-shout-frontend/
└── (無需變更；沿用 GET /resource-pages/my-accessible 與 check-permission)
```

**Structure Decision**: 沿用既有 Web 專案結構；變更僅限後端 `ResourcePageServiceImpl` 內兩處邏輯（依使用者角色短路），前端與 API 契約維持不變。

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

無。本計畫無憲章違反需說明。
