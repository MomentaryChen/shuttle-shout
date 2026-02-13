# Implementation Plan: 叫號系統重新設計與結束流程修正

**Branch**: `001-redesign-calling-system` | **Date**: 2025-02-13 | **Spec**: [spec.md](./spec.md)  
**Input**: Feature specification from `/specs/001-redesign-calling-system/spec.md`

## Summary

修正團隊叫號系統兩大問題：(1) **結束即離開**：使用者在叫號頁點擊「結束」後，須結束連線並直接離開叫號系統頁面（導向上一頁或首頁），不得僅斷線仍停留；(2) **叫號邏輯正確可預期**：等待隊列排序、分配與場地/隊列狀態須符合先到先服務（或既定規則），且狀態同步至所有連線客戶端。實作上沿用既有 Spring WebSocket + Next.js 架構，前端「結束」按鈕改為：發送清除隊列（可選）、斷線、再執行導向離開；後端維持現有 CLEAR_QUEUE / 狀態廣播，必要時補強叫號順序與狀態一致性。

## Technical Context

**Language/Version**: Java 8 (backend), TypeScript / Node (frontend Next.js)  
**Primary Dependencies**: Spring Boot 2.7.18, Spring WebSocket, MyBatis-Flex, MySQL; Next.js, React  
**Storage**: MySQL (queues, players, courts, matches, user_teams)  
**Testing**: JUnit 5 (backend), ESLint / Next (frontend)  
**Target Platform**: JVM (backend), Web browser (frontend)  
**Project Type**: Web application (backend `src/`, frontend `shuttle-shout-frontend/`)  
**Performance Goals**: 叫號操作與狀態同步在正常網路下 3 秒內一致（SC-003）；結束後 2 秒內完成導向（SC-001）  
**Constraints**: 不替換現有 WebSocket 通訊架構；叫號規則為先到先服務或依 queueNumber/createdAt  
**Scale/Scope**: 單一團隊多客戶端同時連線、多場地與等待隊列

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Project constitution**: `.specify/memory/constitution.md` 仍為範本佔位，未定義具體原則與 gates。本計畫不強制憲法關卡，依既有程式庫與測試慣例進行。
- **Quality**: 變更需可透過既有或新增單元/整合測試驗證；前端「結束」流程與後端狀態清理需有明確驗收條件（對應 spec 驗收情境）。

## Project Structure

### Documentation (this feature)

```text
specs/001-redesign-calling-system/
├── plan.md              # This file
├── research.md          # Phase 0
├── data-model.md        # Phase 1
├── quickstart.md        # Phase 1
├── contracts/           # Phase 1 (WebSocket message contract)
└── tasks.md             # Phase 2 (/speckit.tasks – not created by plan)
```

### Source Code (repository root)

```text
src/main/java/com/shuttleshout/
├── handler/
│   ├── TeamCallingWebSocketHandler.java
│   └── strategy/
│       ├── ClearQueueStrategy.java
│       ├── LoadQueueStrategy.java
│       ├── AutoAssignStrategy.java
│       ├── FinishMatchStrategy.java
│       ├── ConfirmStartMatchStrategy.java
│       ├── CancelPendingAssignmentStrategy.java
│       └── ...
├── service/
│   ├── QueueService.java
│   ├── CourtService.java
│   └── ...
├── repository/
│   ├── PlayerRepository.java
│   ├── QueueRepository.java
│   └── ...
└── common/model/po/
    ├── Queue.java
    ├── Player.java
    └── Court (match/court state)

shuttle-shout-frontend/
├── app/team-calling/
│   └── page.tsx
├── components/
│   └── team-calling-system.tsx
└── lib/
    └── api.ts
```

**Structure Decision**: 既有 Web 專案結構；叫號邏輯與「結束」行為的變更集中在 backend handler/strategy 與 frontend team-calling 頁面及元件，不新增頂層目錄。

## Complexity Tracking

*(None – no constitution violations to justify.)*
