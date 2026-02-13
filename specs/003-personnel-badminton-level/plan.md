# Implementation Plan: 人員羽球等級制度

**Branch**: `003-personnel-badminton-level` | **Date**: 2025-02-13 | **Spec**: [spec.md](./spec.md)  
**Input**: Feature specification from `/specs/003-personnel-badminton-level/spec.md`

## Summary

為人員（User）新增「羽球等級」屬性：後端儲存**級數**（1–18），前端顯示**比賽階級**與**級數**。等級依台灣羽球推廣協會分級制度，預設為 null（未設定）。實作上於既有 `users` 表新增可為 null 的等級欄位，後端以枚舉或整數 1–18 驗證；前端人員列表、詳情與編輯表單顯示並可選等級，不呈現程度說明。

## Technical Context

**Language/Version**: Java 8 (backend), TypeScript / Node (frontend Next.js)  
**Primary Dependencies**: Spring Boot 2.7.x, MyBatis-Flex, MySQL; Next.js, React  
**Storage**: MySQL (`users` 表新增 `badminton_level` 欄位，TINYINT NULL，1–18)  
**Testing**: JUnit 5 (backend), 既有前端測試 / 手動驗收  
**Target Platform**: JVM (backend), Web browser (frontend)  
**Project Type**: Web application (backend `src/`, frontend `shuttle-shout-frontend/`)  
**Performance Goals**: 人員查詢與更新維持既有延遲水準；等級選項為靜態枚舉，無額外查詢負擔  
**Constraints**: 不變更既有用戶認證與權限模型；等級僅加欄位，既有 API 擴充回傳與請求欄位  
**Scale/Scope**: 與現有人員規模一致；等級對照表為固定 18 級，無需動態維護介面（MVP）

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **高品質**：新增程式與既有風格一致（UserPO/UserDTO/UserServiceImpl 擴充、前端 personnel-management 表單與列表）。
- **可測試性**：等級欄位可經 API 與前端表單驗證；預設 null、選級數後儲存與顯示符合 spec 驗收情境。
- **MVP 優先**：僅實作「人員＋等級欄位、後端存級數、前端顯示比賽階級與級數」，不實作參考表維護、程度說明呈現、列表依等級篩選/排序。
- **避免過度設計**：等級對照表以枚舉或常數實作，不新增資料表或管理後台。
- **正體中文**：介面與錯誤訊息使用正體中文。

## Project Structure

### Documentation (this feature)

```text
specs/003-personnel-badminton-level/
├── plan.md              # This file
├── research.md          # Phase 0
├── data-model.md        # Phase 1
├── quickstart.md        # Phase 1
├── contracts/           # Phase 1 (User API + level field)
└── tasks.md             # Phase 2 (/speckit.tasks – not created by plan)
```

### Source Code (repository root)

```text
src/main/java/com/shuttleshout/
├── common/model/
│   ├── po/UserPO.java                    # 新增 badmintonLevel (Integer 1-18, null)
│   ├── dto/UserDTO.java                  # 新增 badmintonLevel
│   ├── dto/UserCreateDTO.java             # 可選 badmintonLevel
│   ├── dto/UserUpdateDTO.java             # 可選 badmintonLevel
│   └── enums/BadmintonLevel.java         # 級數 1-18 + 比賽階級顯示名（新建）
├── service/impl/UserServiceImpl.java     # create/update/convertToDto 處理等級
└── controller/UserController.java        # 無需新增端點，既有 GET/POST/PUT 回傳與接受 level

shuttle-shout-frontend/
├── types/api.ts                          # UserDto 新增 badmintonLevel；等級選項常數（級數→比賽階級）
├── components/personnel-management.tsx   # 列表顯示等級；新增/編輯表單等級下拉（顯示比賽階級+級數）
└── lib/api.ts                            # adminApi 更新用戶時傳遞 badmintonLevel（若需）

data/
└── (遷移腳本) 新增 users.badminton_level 欄位，既有人員預設 NULL
```

**Structure Decision**: 沿用既有 Web 專案結構；變更集中在 User 相關 PO/DTO/Service、前端人員管理元件與類型定義，以及一次資料庫遷移。

## Complexity Tracking

*(None – no constitution violations to justify.)*
