package com.shuttleshout.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 錯誤代碼枚舉
 * 統一管理所有API錯誤代碼、HTTP狀態碼和默認錯誤訊息
 * 
 * @author ShuttleShout Team
 */
public enum ErrorCode {
    
    // 通用錯誤
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "伺服器內部錯誤"),
    API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "API錯誤"),
    RUNTIME_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "運行時錯誤"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "參數驗證失敗"),
    BIND_ERROR(HttpStatus.BAD_REQUEST, "參數綁定失敗"),
    CONSTRAINT_VIOLATION(HttpStatus.BAD_REQUEST, "參數約束違反"),
    AUTHENTICATION_ERROR(HttpStatus.UNAUTHORIZED, "認證失敗"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "權限不足"),
    ILLEGAL_STATE(HttpStatus.BAD_REQUEST, "非法狀態"),
    ILLEGAL_ARGUMENT(HttpStatus.BAD_REQUEST, "非法參數"),
    
    // 用戶相關錯誤
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "用戶不存在"),
    USERNAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "用戶名已存在"),
    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "郵箱已存在"),
    BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED, "用戶名或密碼錯誤"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "用戶未認證"),
    GET_USERS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "獲取用戶列表失敗"),
    GET_USER_ERROR(HttpStatus.BAD_REQUEST, "獲取用戶失敗"),
    CREATE_USER_ERROR(HttpStatus.BAD_REQUEST, "創建用戶失敗"),
    UPDATE_USER_ERROR(HttpStatus.BAD_REQUEST, "更新用戶失敗"),
    DELETE_USER_ERROR(HttpStatus.BAD_REQUEST, "刪除用戶失敗"),
    GET_CURRENT_USER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "獲取當前用戶失敗"),
    
    // 角色相關錯誤
    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "角色不存在"),
    ROLE_CODE_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "角色代碼已存在"),
    GET_ROLES_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "獲取角色列表失敗"),
    GET_ROLE_ERROR(HttpStatus.BAD_REQUEST, "獲取角色失敗"),
    CREATE_ROLE_ERROR(HttpStatus.BAD_REQUEST, "創建角色失敗"),
    UPDATE_ROLE_ERROR(HttpStatus.BAD_REQUEST, "更新角色失敗"),
    DELETE_ROLE_ERROR(HttpStatus.BAD_REQUEST, "刪除角色失敗"),
    
    // 球隊相關錯誤
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "球隊不存在"),
    TEAM_NAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "球隊名稱已存在"),
    GET_TEAMS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "獲取球隊列表失敗"),
    GET_MY_TEAMS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "獲取我的球隊列表失敗"),
    GET_TEAM_ERROR(HttpStatus.BAD_REQUEST, "獲取球隊失敗"),
    CREATE_TEAM_ERROR(HttpStatus.BAD_REQUEST, "創建球隊失敗"),
    UPDATE_TEAM_ERROR(HttpStatus.BAD_REQUEST, "更新球隊失敗"),
    DELETE_TEAM_ERROR(HttpStatus.BAD_REQUEST, "刪除球隊失敗"),
    
    // 場地相關錯誤
    COURT_NOT_FOUND(HttpStatus.NOT_FOUND, "場地不存在"),
    GET_COURTS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "獲取場地列表失敗"),
    CREATE_COURT_ERROR(HttpStatus.BAD_REQUEST, "創建場地失敗"),
    
    // 球員相關錯誤
    PLAYER_NOT_FOUND(HttpStatus.NOT_FOUND, "球員不存在"),
    GET_PLAYERS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "獲取球員列表失敗"),
    CREATE_PLAYER_ERROR(HttpStatus.BAD_REQUEST, "創建球員失敗"),
    
    // 隊列相關錯誤
    QUEUE_NOT_FOUND(HttpStatus.NOT_FOUND, "隊列不存在"),
    GET_QUEUES_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "獲取隊列列表失敗"),
    CREATE_QUEUE_ERROR(HttpStatus.BAD_REQUEST, "創建隊列失敗"),
    
    // 用戶團隊關係相關錯誤
    USER_ALREADY_IN_TEAM(HttpStatus.BAD_REQUEST, "用戶已經在團隊中"),
    USER_NOT_IN_TEAM(HttpStatus.BAD_REQUEST, "用戶不在團隊中"),
    TEAM_OWNER_CANNOT_LEAVE(HttpStatus.BAD_REQUEST, "團隊所有者不能離開團隊，請先轉讓所有權或刪除團隊"),
    ONLY_TEAM_OWNER_CAN_REMOVE_MEMBER(HttpStatus.FORBIDDEN, "只有團隊所有者才能移除成員"),
    TARGET_USER_NOT_IN_TEAM(HttpStatus.BAD_REQUEST, "目標用戶不在團隊中"),
    TEAM_OWNER_CANNOT_REMOVE_SELF(HttpStatus.BAD_REQUEST, "團隊所有者不能移除自己，請刪除整個團隊"),
    CANNOT_REMOVE_TEAM_OWNER(HttpStatus.BAD_REQUEST, "不能移除團隊所有者"),
    GET_USER_TEAMS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "獲取用戶團隊關係列表失敗"),
    GET_TEAM_MEMBERS_ERROR(HttpStatus.BAD_REQUEST, "獲取團隊成員失敗"),
    JOIN_TEAM_ERROR(HttpStatus.BAD_REQUEST, "加入團隊失敗"),
    LEAVE_TEAM_ERROR(HttpStatus.BAD_REQUEST, "離開團隊失敗"),
    REMOVE_MEMBER_ERROR(HttpStatus.BAD_REQUEST, "移除成員失敗"),
    
    // 頁面資源相關錯誤
    RESOURCE_PAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "頁面資源不存在"),
    RESOURCE_PAGE_CODE_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "頁面資源代碼已存在"),
    GET_RESOURCE_PAGES_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "獲取頁面資源列表失敗"),
    GET_RESOURCE_PAGE_ERROR(HttpStatus.BAD_REQUEST, "獲取頁面資源失敗"),
    GET_ROLE_RESOURCE_PAGES_ERROR(HttpStatus.BAD_REQUEST, "獲取角色頁面資源失敗"),
    GET_MY_RESOURCE_PAGES_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "獲取我的頁面資源失敗"),
    CREATE_RESOURCE_PAGE_ERROR(HttpStatus.BAD_REQUEST, "創建頁面資源失敗"),
    UPDATE_RESOURCE_PAGE_ERROR(HttpStatus.BAD_REQUEST, "更新頁面資源失敗"),
    DELETE_RESOURCE_PAGE_ERROR(HttpStatus.BAD_REQUEST, "刪除頁面資源失敗"),
    CHECK_PERMISSION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "檢查頁面權限失敗"),
    
    // 角色頁面資源關聯相關錯誤
    ROLE_RESOURCE_PAGE_ASSOCIATION_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "角色與頁面資源的關聯已存在"),
    ROLE_RESOURCE_PAGE_ASSOCIATION_NOT_FOUND(HttpStatus.NOT_FOUND, "角色與頁面資源的關聯不存在"),
    ASSIGN_RESOURCE_PAGE_ERROR(HttpStatus.BAD_REQUEST, "分配頁面權限失敗"),
    REMOVE_RESOURCE_PAGE_ERROR(HttpStatus.BAD_REQUEST, "移除頁面權限失敗"),
    UPDATE_RESOURCE_PAGE_PERMISSION_ERROR(HttpStatus.BAD_REQUEST, "更新頁面權限失敗");
    
    private final HttpStatus httpStatus;
    private final String defaultMessage;
    
    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }
    
    /**
     * 獲取HTTP狀態碼
     * 
     * @return HTTP狀態碼
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
    
    /**
     * 獲取默認錯誤訊息
     * 
     * @return 默認錯誤訊息
     */
    public String getDefaultMessage() {
        return defaultMessage;
    }
    
    /**
     * 獲取錯誤代碼字符串
     * 
     * @return 錯誤代碼字符串
     */
    public String getCode() {
        return this.name();
    }
}
