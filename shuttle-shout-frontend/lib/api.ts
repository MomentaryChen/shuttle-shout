/**
 * API服務 - 與後端API通信
 */

import { PlayerDto, CourtDto, QueueDto, QueueStatus, TeamDto, LoginRequest, LoginResponse, UserDto, ResourcePageDto, UserTeamDto } from "@/types/api"

// 獲取存儲的token（已經在上面定義）
// getStoredToken, setStoredToken, clearStoredToken 函數已經在上面定義

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:18080/api"

/**
 * 獲取存儲的token
 */
export function getStoredToken(): string | null {
  if (typeof window === "undefined") return null
  return localStorage.getItem("auth_token")
}

/**
 * 存儲token
 */
export function setStoredToken(token: string): void {
  if (typeof window === "undefined") return
  localStorage.setItem("auth_token", token)
}

/**
 * 清除token
 */
export function clearStoredToken(): void {
  if (typeof window === "undefined") return
  localStorage.removeItem("auth_token")
}

/**
 * 通用API請求函數
 */
async function apiRequest<T>(
  endpoint: string,
  options?: RequestInit,
  includeAuth: boolean = true
): Promise<T> {
  const url = `${API_BASE_URL}${endpoint}`
  const token = getStoredToken()

  const headers: HeadersInit = {
    "Content-Type": "application/json",
    ...options?.headers,
  }

  // 如果需要認證且有token，添加到請求頭
  if (includeAuth && token) {
    headers["X-AUTHORIZATION"] = token
  }

  const response = await fetch(url, {
    ...options,
    headers,
  })

  if (!response.ok) {
    const errorText = await response.text()
    let errorMessage = `API請求失敗: ${response.status} ${response.statusText}`
    
    // 嘗試解析錯誤響應JSON，提取message字段
    try {
      if (errorText) {
        const errorJson = JSON.parse(errorText)
        if (errorJson.message) {
          errorMessage = errorJson.message
        }
      }
    } catch (parseError) {
      // 如果解析失敗，使用原始錯誤文本
      if (errorText) {
        errorMessage = errorText
      }
    }
    
    throw new Error(errorMessage)
  }

  // 處理204 No Content響應
  if (response.status === 204) {
    return null as T
  }

  // 檢查響應體是否為空
  // 注意：response.text() 只能調用一次，所以先讀取文本
  const text = await response.text()
  
  // 如果響應體為空，返回null
  if (!text || text.trim() === "") {
    return null as T
  }

  // 嘗試解析JSON
  try {
    return JSON.parse(text) as T
  } catch (error) {
    // 如果解析失敗（例如logout返回空響應），返回null而不是拋出錯誤
    console.warn("JSON解析失敗，響應體為空或格式錯誤:", text.substring(0, 100))
    return null as T
  }
}

/**
 * 無認證API請求函數（用於login等不需要token的請求）
 */
async function apiRequestWithoutAuth<T>(
  endpoint: string,
  options?: RequestInit
): Promise<T> {
  return apiRequest<T>(endpoint, options, false)
}

/**
 * 球員API
 */
export const playerApi = {
  /**
   * 獲取所有球員
   */
  getAll: async (): Promise<PlayerDto[]> => {
    return apiRequest<PlayerDto[]>("/players")
  },

  /**
   * 根據ID獲取球員
   */
  getById: async (id: number): Promise<PlayerDto> => {
    return apiRequest<PlayerDto>(`/players/${id}`)
  },

  /**
   * 創建球員
   */
  create: async (player: Partial<PlayerDto>): Promise<PlayerDto> => {
    return apiRequest<PlayerDto>("/players", {
      method: "POST",
      body: JSON.stringify(player),
    })
  },

  /**
   * 更新球員
   */
  update: async (id: number, player: Partial<PlayerDto>): Promise<PlayerDto> => {
    return apiRequest<PlayerDto>(`/players/${id}`, {
      method: "PUT",
      body: JSON.stringify(player),
    })
  },

  /**
   * 刪除球員
   */
  delete: async (id: number): Promise<void> => {
    return apiRequest<void>(`/players/${id}`, {
      method: "DELETE",
    })
  },

  /**
   * 搜尋球員
   */
  search: async (keyword: string): Promise<PlayerDto[]> => {
    return apiRequest<PlayerDto[]>(`/players/search?keyword=${encodeURIComponent(keyword)}`)
  },
}

/**
 * 球場API
 */
export const courtApi = {
  /**
   * 獲取所有球場
   */
  getAll: async (): Promise<CourtDto[]> => {
    return apiRequest<CourtDto[]>("/courts")
  },

  /**
   * 獲取所有活躍的球場
   */
  getActive: async (): Promise<CourtDto[]> => {
    return apiRequest<CourtDto[]>("/courts/active")
  },

  /**
   * 根據ID獲取球場
   */
  getById: async (id: number): Promise<CourtDto> => {
    return apiRequest<CourtDto>(`/courts/${id}`)
  },

  /**
   * 創建球場
   */
  create: async (court: Partial<CourtDto>): Promise<CourtDto> => {
    return apiRequest<CourtDto>("/courts", {
      method: "POST",
      body: JSON.stringify(court),
    })
  },

  /**
   * 更新球場
   */
  update: async (id: number, court: Partial<CourtDto>): Promise<CourtDto> => {
    return apiRequest<CourtDto>(`/courts/${id}`, {
      method: "PUT",
      body: JSON.stringify(court),
    })
  },

  /**
   * 刪除球場
   */
  delete: async (id: number): Promise<void> => {
    return apiRequest<void>(`/courts/${id}`, {
      method: "DELETE",
    })
  },
}

/**
 * 球隊API
 */
export const teamApi = {
  /**
   * 獲取所有球隊（不需要認證）
   */
  getAll: async (): Promise<TeamDto[]> => {
    return apiRequestWithoutAuth<TeamDto[]>("/teams")
  },

  /**
   * 獲取當前登錄用戶創建的球隊（需要認證）
   */
  getMyTeams: async (): Promise<TeamDto[]> => {
    return apiRequest<TeamDto[]>("/teams/my")
  },

  /**
   * 獲取所有活躍的球隊
   */
  getActive: async (): Promise<TeamDto[]> => {
    return apiRequest<TeamDto[]>("/teams/active")
  },

  /**
   * 根據ID獲取球隊
   */
  getById: async (id: number): Promise<TeamDto> => {
    return apiRequest<TeamDto>(`/teams/${id}`)
  },

  /**
   * 創建球隊
   */
  create: async (team: Partial<TeamDto>): Promise<TeamDto> => {
    return apiRequest<TeamDto>("/teams", {
      method: "POST",
      body: JSON.stringify(team),
    })
  },

  /**
   * 更新球隊
   */
  update: async (id: number, team: Partial<TeamDto>): Promise<TeamDto> => {
    return apiRequest<TeamDto>(`/teams/${id}`, {
      method: "PUT",
      body: JSON.stringify(team),
    })
  },

  /**
   * 刪除球隊
   */
  delete: async (id: number): Promise<void> => {
    return apiRequest<void>(`/teams/${id}`, {
      method: "DELETE",
    })
  },
}

/**
 * 隊列API
 */
export const queueApi = {
  /**
   * 獲取所有隊列
   */
  getAll: async (): Promise<QueueDto[]> => {
    return apiRequest<QueueDto[]>("/queues")
  },

  /**
   * 獲取等待中的隊列
   */
  getWaiting: async (): Promise<QueueDto[]> => {
    return apiRequest<QueueDto[]>("/queues/waiting")
  },

  /**
   * 根據球場獲取等待中的隊列
   */
  getWaitingByCourt: async (courtId: number): Promise<QueueDto[]> => {
    return apiRequest<QueueDto[]>(`/queues/waiting/court/${courtId}`)
  },

  /**
   * 加入隊列
   */
  join: async (playerId: number, courtId?: number): Promise<QueueDto> => {
    const params = new URLSearchParams({ playerId: playerId.toString() })
    if (courtId) {
      params.append("courtId", courtId.toString())
    }
    return apiRequest<QueueDto>(`/queues/join?${params.toString()}`, {
      method: "POST",
    })
  },

  /**
   * 叫號
   */
  call: async (queueId: number, courtId?: number): Promise<QueueDto> => {
    const params = courtId ? `?courtId=${courtId}` : ""
    return apiRequest<QueueDto>(`/queues/${queueId}/call${params}`, {
      method: "POST",
    })
  },

  /**
   * 完成服務
   */
  serve: async (queueId: number): Promise<QueueDto> => {
    return apiRequest<QueueDto>(`/queues/${queueId}/serve`, {
      method: "POST",
    })
  },

  /**
   * 取消隊列
   */
  cancel: async (queueId: number): Promise<QueueDto> => {
    return apiRequest<QueueDto>(`/queues/${queueId}/cancel`, {
      method: "POST",
    })
  },
}

/**
 * 認證API
 * 注意：後端配置了 context-path: /api，所以所有路徑都會自動加上 /api 前綴
 */
export const authApi = {
  /**
   * 用戶登錄
   */
  login: async (loginRequest: LoginRequest): Promise<LoginResponse> => {
    const response = await apiRequestWithoutAuth<LoginResponse>("/auth/login", {
      method: "POST",
      body: JSON.stringify(loginRequest),
    })

    // 登錄成功後存儲token
    if (response.token) {
      setStoredToken(response.token)
    }

    return response
  },

  /**
   * 用戶登出
   */
  logout: async (): Promise<void> => {
    const token = getStoredToken()
    if (token) {
      try {
        await apiRequest<void>("/auth/logout", {
          method: "POST",
          body: JSON.stringify(token),
        })
      } catch (error) {
        console.error("登出失敗:", error)
      }
    }
    clearStoredToken()
  },

  /**
   * 獲取當前用戶信息
   */
  getCurrentUser: async (): Promise<UserDto> => {
    return apiRequest<UserDto>("/users/me")
  },
}

/**
 * 用戶API
 */
export const userApi = {
  /**
   * 獲取所有用戶
   */
  getAll: async (): Promise<UserDto[]> => {
    return apiRequest<UserDto[]>("/users")
  },

  /**
   * 根據ID獲取用戶
   */
  getById: async (id: number): Promise<UserDto> => {
    return apiRequest<UserDto>(`/users/${id}`)
  },

  /**
   * 用戶註冊（不需要認證）
   * 註冊成功後返回用戶信息（不包含token，需要單獨登錄）
   */
  register: async (userData: {
    username: string
    password: string
    email?: string
    phoneNumber?: string
    realName?: string
  }): Promise<UserDto> => {
    const response = await apiRequestWithoutAuth<UserDto>("/auth/register", {
      method: "POST",
      body: JSON.stringify(userData),
    })

    return response
  },

  /**
   * 更新當前用戶信息
   */
  updateMe: async (userData: {
    email?: string
    phoneNumber?: string
    realName?: string
    avatar?: string
    password?: string
  }): Promise<UserDto> => {
    return apiRequest<UserDto>("/users/me", {
      method: "PUT",
      body: JSON.stringify(userData),
    })
  },

  /**
   * 更新指定用戶信息
   */
  update: async (id: number, userData: {
    email?: string
    phoneNumber?: string
    realName?: string
    avatar?: string
    password?: string
    isActive?: boolean
  }): Promise<UserDto> => {
    return apiRequest<UserDto>(`/users/${id}`, {
      method: "PUT",
      body: JSON.stringify(userData),
    })
  },
}

/**
 * 管理員API
 */
export const adminApi = {
  /**
   * 獲取所有用戶（管理員權限）
   * 僅限SYSTEM_ADMIN角色訪問的人員配置頁面
   */
  getAllUsers: async (): Promise<UserDto[]> => {
    return apiRequest<UserDto[]>("/users/admin/users")
  },
}

/**
 * 用戶團隊關係API
 */
export const userTeamApi = {
  /**
   * 獲取所有用戶團隊關係
   */
  getAll: async (): Promise<UserTeamDto[]> => {
    return apiRequest<UserTeamDto[]>("/user-teams")
  },

  /**
   * 根據團隊ID獲取團隊成員
   */
  getTeamMembers: async (teamId: number): Promise<UserTeamDto[]> => {
    return apiRequest<UserTeamDto[]>(`/user-teams/team/${teamId}`)
  },

  /**
   * 根據用戶ID獲取用戶加入的團隊
   */
  getUserTeams: async (userId: number): Promise<UserTeamDto[]> => {
    return apiRequest<UserTeamDto[]>(`/user-teams/user/${userId}`)
  },

  /**
   * 用戶加入團隊
   */
  joinTeam: async (data: { userId: number; teamId: number; isOwner?: boolean }): Promise<any> => {
    return apiRequest<any>("/user-teams", {
      method: "POST",
      body: JSON.stringify(data),
    })
  },

  /**
   * 用戶離開團隊
   */
  leaveTeam: async (userId: number, teamId: number): Promise<void> => {
    return apiRequest<void>(`/user-teams/${userId}/${teamId}`, {
      method: "DELETE",
    })
  },

  /**
   * 團隊所有者移除成員
   */
  removeMember: async (targetUserId: number, teamId: number): Promise<void> => {
    return apiRequest<void>(`/user-teams/remove/${targetUserId}/${teamId}`, {
      method: "DELETE",
    })
  },

  /**
   * 當前用戶加入團隊
   */
  currentUserJoinTeam: async (teamId: number): Promise<any> => {
    return apiRequest<any>(`/user-teams/join/${teamId}`, {
      method: "POST",
    })
  },

  /**
   * 當前用戶離開團隊
   */
  currentUserLeaveTeam: async (teamId: number): Promise<void> => {
    return apiRequest<void>(`/user-teams/leave/${teamId}`, {
      method: "DELETE",
    })
  },
}

/**
 * 資源頁面API
 */
export const resourcePageApi = {
  /**
   * 獲取當前用戶可訪問的所有資源頁面
   */
  getMyAccessible: async (): Promise<ResourcePageDto[]> => {
    return apiRequest<ResourcePageDto[]>("/resource-pages/my-accessible")
  },

  /**
   * 檢查用戶是否有特定頁面的權限
   */
  checkPermission: async (resourcePageCode: string, permission: string): Promise<boolean> => {
    return apiRequest<boolean>(`/resource-pages/check-permission/${resourcePageCode}/${permission}`)
  },
}
