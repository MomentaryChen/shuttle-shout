<!--
Sync Impact Report
- Version change: [template] → 1.0.0
- Modified principles: All placeholders replaced with concrete principles (高品質、可測試性、MVP 優先、避免過度設計、正體中文)
- Added sections: 額外約束、開發流程
- Removed sections: None
- Templates: plan-template.md ✅ (Constitution Check 依本憲章)、spec-template.md ✅ (MVP/獨立可測已對齊)、tasks-template.md ✅ (MVP/Phase 對齊)
- Follow-up TODOs: None
-->

# Code Lab Notes 專案憲章

## Core Principles

### I. 高品質

交付成果 MUST 符合專案既有品質標準：程式可讀、結構清晰、與現有程式碼風格一致。  
**理由**：本專案為學習筆記與演算法視覺化，可讀性與可維護性優先於炫技。

### II. 可測試性

功能與行為 MUST 可被驗證；每個 user story 須具備「獨立可測」的驗收情境。  
**理由**：可測試性確保 MVP 可獨立驗證、重構時不破壞既有行為。

### III. MVP 優先（Minimum Viable Product）

先交付最小可行、可獨立運作與驗證的切片，再依優先級擴充。  
**理由**：避免一次做大而全；每個增量都應可展示、可部署、可回饋。

### IV. 避免過度設計（No Overdesign）

不引入當前需求未要求的架構、模式或依賴；YAGNI（You Aren't Gonna Need It）。  
**理由**：控制複雜度，保持程式庫與 UI 精簡，利於學習與維護。

### V. 正體中文

專案內所有對使用者與開發者可見的說明、註解、文件、介面文字 MUST 使用正體中文。  
**理由**：本專案主要受眾為中文讀者，一致語言降低認知負擔。

## 額外約束

- **技術棧**：依既有專案（Vite、React、pnpm、PowerShell）為準；新增技術須在 plan 中說明理由。
- **部署**：以 GitHub Pages 靜態部署為目標，不引入需後端或額外服務的依賴，除非規格明確要求。

## 開發流程

- **規格**：新功能以 `/speckit.specify` 產出 spec，須含優先級與獨立可測情境。
- **計畫**：`/speckit.plan` 產出之 plan 須通過「Constitution Check」再進入 tasks。
- **實作**：依 tasks 順序實作；每完成一 user story 須可獨立驗證再進入下一項。
- **合規**：PR 或實作前自行對照本憲章；若有違反須在 plan 的 Complexity Tracking 中說明理由。

## Governance

- 本憲章為專案最高治理原則；與既有 `.cursor/rules` 衝突時，以本憲章為準。
- **修訂**：修改憲章須更新版本號（語意化版本）、Last Amended 日期，並在檔案頂端 Sync Impact Report 註記變更。
- **版本**：MAJOR＝不相容之原則刪除或重定義；MINOR＝新增原則或章節；PATCH＝措辭修正、錯字、非語意變更。
- **合規審查**：實作與 plan 須對照本憲章；過度設計或違反 MVP/可測試性時須在 plan 中 justification。

**Version**: 1.0.0 | **Ratified**: 2025-02-13 | **Last Amended**: 2025-02-13
