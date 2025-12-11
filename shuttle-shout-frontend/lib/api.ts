/**
 * API服务 - 与后端API通信
 */

import { PlayerDto, CourtDto, QueueDto, QueueStatus, TeamDto, LoginRequest, LoginResponse, UserDto, ResourcePageDto } from "@/types/api"

// 获取存储的token（已经在上面定义）
// getStoredToken, setStoredToken, clearStoredToken 函数已经在上面定义

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:18080/api"

/**
 * 获取存储的token
 */
export function getStoredToken(): string | null {
  if (typeof window === "undefined") return null
  return localStorage.getItem("auth_token")
}

/**
 * 存储token
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
 * 通用API请求函数
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

  // 如果需要认证且有token，添加到请求头
  if (includeAuth && token) {
    headers["X-AUTHORIZATION"] = token
  }

  const response = await fetch(url, {
    ...options,
    headers,
  })

  if (!response.ok) {
    const errorText = await response.text()
    let errorMessage = `API请求失败: ${response.status} ${response.statusText}`
    
    // 尝试解析错误响应JSON，提取message字段
    try {
      if (errorText) {
        const errorJson = JSON.parse(errorText)
        if (errorJson.message) {
          errorMessage = errorJson.message
        }
      }
    } catch (parseError) {
      // 如果解析失败，使用原始错误文本
      if (errorText) {
        errorMessage = errorText
      }
    }
    
    throw new Error(errorMessage)
  }

  // 处理204 No Content响应
  if (response.status === 204) {
    return null as T
  }

  // 检查响应体是否为空
  // 注意：response.text() 只能调用一次，所以先读取文本
  const text = await response.text()
  
  // 如果响应体为空，返回null
  if (!text || text.trim() === "") {
    return null as T
  }

  // 尝试解析JSON
  try {
    return JSON.parse(text) as T
  } catch (error) {
    // 如果解析失败（例如logout返回空响应），返回null而不是抛出错误
    console.warn("JSON解析失败，响应体为空或格式错误:", text.substring(0, 100))
    return null as T
  }
}

/**
 * 无认证API请求函数（用于login等不需要token的请求）
 */
async function apiRequestWithoutAuth<T>(
  endpoint: string,
  options?: RequestInit
): Promise<T> {
  return apiRequest<T>(endpoint, options, false)
}

/**
 * 球员API
 */
export const playerApi = {
  /**
   * 获取所有球员
   */
  getAll: async (): Promise<PlayerDto[]> => {
    return apiRequest<PlayerDto[]>("/players")
  },

  /**
   * 根据ID获取球员
   */
  getById: async (id: number): Promise<PlayerDto> => {
    return apiRequest<PlayerDto>(`/players/${id}`)
  },

  /**
   * 创建球员
   */
  create: async (player: Partial<PlayerDto>): Promise<PlayerDto> => {
    return apiRequest<PlayerDto>("/players", {
      method: "POST",
      body: JSON.stringify(player),
    })
  },

  /**
   * 更新球员
   */
  update: async (id: number, player: Partial<PlayerDto>): Promise<PlayerDto> => {
    return apiRequest<PlayerDto>(`/players/${id}`, {
      method: "PUT",
      body: JSON.stringify(player),
    })
  },

  /**
   * 删除球员
   */
  delete: async (id: number): Promise<void> => {
    return apiRequest<void>(`/players/${id}`, {
      method: "DELETE",
    })
  },

  /**
   * 搜索球员
   */
  search: async (keyword: string): Promise<PlayerDto[]> => {
    return apiRequest<PlayerDto[]>(`/players/search?keyword=${encodeURIComponent(keyword)}`)
  },
}

/**
 * 球场API
 */
export const courtApi = {
  /**
   * 获取所有球场
   */
  getAll: async (): Promise<CourtDto[]> => {
    return apiRequest<CourtDto[]>("/courts")
  },

  /**
   * 获取所有活跃的球场
   */
  getActive: async (): Promise<CourtDto[]> => {
    return apiRequest<CourtDto[]>("/courts/active")
  },

  /**
   * 根据ID获取球场
   */
  getById: async (id: number): Promise<CourtDto> => {
    return apiRequest<CourtDto>(`/courts/${id}`)
  },

  /**
   * 创建球场
   */
  create: async (court: Partial<CourtDto>): Promise<CourtDto> => {
    return apiRequest<CourtDto>("/courts", {
      method: "POST",
      body: JSON.stringify(court),
    })
  },

  /**
   * 更新球场
   */
  update: async (id: number, court: Partial<CourtDto>): Promise<CourtDto> => {
    return apiRequest<CourtDto>(`/courts/${id}`, {
      method: "PUT",
      body: JSON.stringify(court),
    })
  },

  /**
   * 删除球场
   */
  delete: async (id: number): Promise<void> => {
    return apiRequest<void>(`/courts/${id}`, {
      method: "DELETE",
    })
  },
}

/**
 * 球队API
 */
export const teamApi = {
  /**
   * 获取所有球队（不需要认证）
   */
  getAll: async (): Promise<TeamDto[]> => {
    return apiRequestWithoutAuth<TeamDto[]>("/teams")
  },

  /**
   * 获取当前登录用户创建的球队（需要认证）
   */
  getMyTeams: async (): Promise<TeamDto[]> => {
    return apiRequest<TeamDto[]>("/teams/my")
  },

  /**
   * 获取所有活跃的球队
   */
  getActive: async (): Promise<TeamDto[]> => {
    return apiRequest<TeamDto[]>("/teams/active")
  },

  /**
   * 根据ID获取球队
   */
  getById: async (id: number): Promise<TeamDto> => {
    return apiRequest<TeamDto>(`/teams/${id}`)
  },

  /**
   * 创建球队
   */
  create: async (team: Partial<TeamDto>): Promise<TeamDto> => {
    return apiRequest<TeamDto>("/teams", {
      method: "POST",
      body: JSON.stringify(team),
    })
  },

  /**
   * 更新球队
   */
  update: async (id: number, team: Partial<TeamDto>): Promise<TeamDto> => {
    return apiRequest<TeamDto>(`/teams/${id}`, {
      method: "PUT",
      body: JSON.stringify(team),
    })
  },

  /**
   * 删除球队
   */
  delete: async (id: number): Promise<void> => {
    return apiRequest<void>(`/teams/${id}`, {
      method: "DELETE",
    })
  },
}

/**
 * 队列API
 */
export const queueApi = {
  /**
   * 获取所有队列
   */
  getAll: async (): Promise<QueueDto[]> => {
    return apiRequest<QueueDto[]>("/queues")
  },

  /**
   * 获取等待中的队列
   */
  getWaiting: async (): Promise<QueueDto[]> => {
    return apiRequest<QueueDto[]>("/queues/waiting")
  },

  /**
   * 根据球场获取等待中的队列
   */
  getWaitingByCourt: async (courtId: number): Promise<QueueDto[]> => {
    return apiRequest<QueueDto[]>(`/queues/waiting/court/${courtId}`)
  },

  /**
   * 加入队列
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
   * 叫号
   */
  call: async (queueId: number, courtId?: number): Promise<QueueDto> => {
    const params = courtId ? `?courtId=${courtId}` : ""
    return apiRequest<QueueDto>(`/queues/${queueId}/call${params}`, {
      method: "POST",
    })
  },

  /**
   * 完成服务
   */
  serve: async (queueId: number): Promise<QueueDto> => {
    return apiRequest<QueueDto>(`/queues/${queueId}/serve`, {
      method: "POST",
    })
  },

  /**
   * 取消队列
   */
  cancel: async (queueId: number): Promise<QueueDto> => {
    return apiRequest<QueueDto>(`/queues/${queueId}/cancel`, {
      method: "POST",
    })
  },
}

/**
 * 认证API
 * 注意：后端配置了 context-path: /api，所以所有路径都会自动加上 /api 前缀
 */
export const authApi = {
  /**
   * 用户登录
   */
  login: async (loginRequest: LoginRequest): Promise<LoginResponse> => {
    const response = await apiRequestWithoutAuth<LoginResponse>("/auth/login", {
      method: "POST",
      body: JSON.stringify(loginRequest),
    })

    // 登录成功后存储token
    if (response.token) {
      setStoredToken(response.token)
    }

    return response
  },

  /**
   * 用户登出
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
        console.error("登出失败:", error)
      }
    }
    clearStoredToken()
  },

  /**
   * 获取当前用户信息
   */
  getCurrentUser: async (): Promise<UserDto> => {
    return apiRequest<UserDto>("/users/me")
  },
}

/**
 * 用户API
 */
export const userApi = {
  /**
   * 获取所有用户
   */
  getAll: async (): Promise<UserDto[]> => {
    return apiRequest<UserDto[]>("/users")
  },

  /**
   * 根据ID获取用户
   */
  getById: async (id: number): Promise<UserDto> => {
    return apiRequest<UserDto>(`/users/${id}`)
  },

  /**
   * 用户注册（不需要认证）
   * 注册成功后自动登录，返回登录响应（包含token和用户信息）
   */
  register: async (userData: {
    username: string
    password: string
    email?: string
    phoneNumber?: string
    realName?: string
  }): Promise<LoginResponse> => {
    const response = await apiRequestWithoutAuth<LoginResponse>("/auth/register", {
      method: "POST",
      body: JSON.stringify(userData),
    })

    // 注册成功后存储token（与登录API保持一致）
    if (response.token) {
      setStoredToken(response.token)
    }

    return response
  },

  /**
   * 更新当前用户信息
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
   * 更新指定用户信息
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
 * 管理员API
 */
export const adminApi = {
  /**
   * 获取所有用户（管理员权限）
   * 仅限SYSTEM_ADMIN角色访问的人员配置页面
   */
  getAllUsers: async (): Promise<UserDto[]> => {
    return apiRequest<UserDto[]>("/users/admin/users")
  },
}

/**
 * 用户团队关系API
 */
export const userTeamApi = {
  /**
   * 获取所有用户团队关系
   */
  getAll: async (): Promise<any[]> => {
    return apiRequest<any[]>("/user-teams")
  },

  /**
   * 根据团队ID获取团队成员
   */
  getTeamMembers: async (teamId: number): Promise<any[]> => {
    return apiRequest<any[]>(`/user-teams/team/${teamId}`)
  },

  /**
   * 根据用户ID获取用户加入的团队
   */
  getUserTeams: async (userId: number): Promise<any[]> => {
    return apiRequest<any[]>(`/user-teams/user/${userId}`)
  },

  /**
   * 用户加入团队
   */
  joinTeam: async (data: { userId: number; teamId: number; isOwner?: boolean }): Promise<any> => {
    return apiRequest<any>("/user-teams", {
      method: "POST",
      body: JSON.stringify(data),
    })
  },

  /**
   * 用户离开团队
   */
  leaveTeam: async (userId: number, teamId: number): Promise<void> => {
    return apiRequest<void>(`/user-teams/${userId}/${teamId}`, {
      method: "DELETE",
    })
  },

  /**
   * 团队所有者移除成员
   */
  removeMember: async (targetUserId: number, teamId: number): Promise<void> => {
    return apiRequest<void>(`/user-teams/remove/${targetUserId}/${teamId}`, {
      method: "DELETE",
    })
  },

  /**
   * 当前用户加入团队
   */
  currentUserJoinTeam: async (teamId: number): Promise<any> => {
    return apiRequest<any>(`/user-teams/join/${teamId}`, {
      method: "POST",
    })
  },

  /**
   * 当前用户离开团队
   */
  currentUserLeaveTeam: async (teamId: number): Promise<void> => {
    return apiRequest<void>(`/user-teams/leave/${teamId}`, {
      method: "DELETE",
    })
  },
}

/**
 * 资源页面API
 */
export const resourcePageApi = {
  /**
   * 获取当前用户可访问的所有资源页面
   */
  getMyAccessible: async (): Promise<ResourcePageDto[]> => {
    return apiRequest<ResourcePageDto[]>("/resource-pages/my-accessible")
  },

  /**
   * 检查用户是否有特定页面的权限
   */
  checkPermission: async (resourcePageCode: string, permission: string): Promise<boolean> => {
    return apiRequest<boolean>(`/resource-pages/check-permission/${resourcePageCode}/${permission}`)
  },
}
