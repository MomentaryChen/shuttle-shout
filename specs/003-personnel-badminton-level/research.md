# Research: 003-personnel-badminton-level

**Branch**: `003-personnel-badminton-level` | **Phase**: 0

## 等級對照表實作方式

**Decision**: 後端以 Java 枚舉（enum）定義級數 1–18 與對應比賽階級顯示名；前端以常數陣列或對照表實作相同對應，供下拉選單與顯示使用。

**Rationale**:
- 台灣羽球推廣協會分級為固定 18 級，不需執行期從資料庫或設定檔載入。
- 枚舉可集中驗證（1–18）、提供 getDisplayName() 回傳比賽階級，與既有 `TeamLevel` 模式一致。
- 前端同步一份級數→比賽階級對照即可顯示「比賽階級 + 級數」，無需額外 API。

**Alternatives considered**:
- 參考表存資料庫：可維護但本規格不要求參考表維護功能，且 18 筆固定資料增加遷移與查詢複雜度，捨棄。
- 僅後端枚舉、前端呼叫 GET /api/badminton-levels：可行，但增加一次請求與快取考量；MVP 採前後端各自常數以簡化。

## 儲存型別與驗證

**Decision**: 後端以 `Integer` 儲存級數（1–18），null 表示未設定；資料庫欄位 `badminton_level` TINYINT NULL。寫入時驗證須在 1–18 或 null，否則回傳 400 與明確錯誤訊息。

**Rationale**: 與 spec 一致（後端存級數、預設 null）；TINYINT 足夠表示 1–18，且與既有 `users` 表風格一致。

**Alternatives considered**: 存比賽階級字串（如「中階」）：不利於日後篩選與排序且需額外對照表還原級數，捨棄。

## 前端顯示格式

**Decision**: 列表與詳情顯示「比賽階級（級數）」或「級數 - 比賽階級」（例如「中階（8）」或「8 - 中階」）；下拉選單選項同格式，未設定時顯示「未設定」。

**Rationale**: Spec 要求前端同時呈現比賽階級與級數；單一格式統一即可，具體用「中階（8）」或「8 - 中階」由實作時擇一。
