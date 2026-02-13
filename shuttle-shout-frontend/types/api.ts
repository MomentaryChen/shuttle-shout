/**
 * API类型定义
 */

export interface PlayerDto {
  id: number
  name: string
  phoneNumber?: string
  notes?: string
  teamId?: number
  teamName?: string
  createdAt?: string
  updatedAt?: string
}

export interface CourtDto {
  id: number
  name: string
  description?: string
  isActive?: boolean
  teamId?: number
  teamName?: string
  createdAt?: string
  updatedAt?: string
}

export enum QueueStatus {
  WAITING = "WAITING",
  CALLED = "CALLED",
  SERVED = "SERVED",
  CANCELLED = "CANCELLED",
}

export interface QueueDto {
  id: number
  playerId: number
  playerName?: string
  courtId?: number
  courtName?: string
  status: QueueStatus
  queueNumber?: number
  calledAt?: string
  servedAt?: string
  createdAt?: string
  updatedAt?: string
}

// 球队等级枚举（与后端TeamLevel枚举对应）
export type TeamLevel = "新手" | "初階" | "中等" | "強" | "超強" | "世界強"

// 前端使用的类型（兼容现有组件）
export interface TeamDto {
  id: number
  name: string
  description?: string // 球队描述
  color?: string
  level?: TeamLevel // 球队等级
  maxPlayers: number
  courtCount: number
  isActive?: boolean
  currentPlayerCount?: number
  currentCourtCount?: number
  createdAt?: string
  updatedAt?: string
  userId?: number // 球队创建者用户ID
  playerIds?: number[] // 球员ID列表
}

/**
 * 團隊總覽統計數據
 */
export interface TeamOverviewStatsDto {
  totalPlayers: number // 總人數（所有活躍團隊的成員總數）
  totalCourts: number // 使用場地（所有活躍團隊使用的場地總數）
  activeTeams: number // 活躍團隊數量
}

export interface Team {
  id: string
  name: string
  color: string
  maxPlayers?: number
  courtCount?: number
}

export interface Player {
  id: string
  name: string
  teamId: string
  courtId?: string
  status: "waiting" | "playing" | "rest"
}

export interface Court {
  id: string
  name: string
  teamId: string
  status: "available" | "occupied"
}

/**
 * 羽球等級對照（台灣羽球推廣協會）：級數 1–18 → 比賽階級
 * 後端儲存級數，前端顯示比賽階級與級數
 */
export const BADMINTON_LEVELS: { level: number; tier: string }[] = [
  ...Array.from({ length: 3 }, (_, i) => ({ level: i + 1, tier: "新手階" })),
  ...Array.from({ length: 2 }, (_, i) => ({ level: i + 4, tier: "初階" })),
  ...Array.from({ length: 2 }, (_, i) => ({ level: i + 6, tier: "初中階" })),
  ...Array.from({ length: 2 }, (_, i) => ({ level: i + 8, tier: "中階" })),
  ...Array.from({ length: 3 }, (_, i) => ({ level: i + 10, tier: "中進階" })),
  ...Array.from({ length: 3 }, (_, i) => ({ level: i + 13, tier: "高階" })),
  ...Array.from({ length: 3 }, (_, i) => ({ level: i + 16, tier: "職業級" })),
]

export function getBadmintonLevelLabel(level: number | null | undefined): string {
  if (level == null) return "未設定"
  const item = BADMINTON_LEVELS.find((x) => x.level === level)
  return item ? `${item.tier}（${level}）` : `級數 ${level}`
}

/**
 * 用户相关类型
 */
export interface UserDto {
  id: number
  username: string
  email?: string
  phoneNumber?: string
  realName?: string
  avatar?: string
  isActive?: boolean
  lastLoginAt?: string
  /** 羽球等級級數（1–18），null 表示未設定 */
  badmintonLevel?: number | null
  roleNames?: string[]
  /** 角色代碼（如 SYSTEM_ADMIN），與後端一致，供權限與管理員統計使用 */
  roleCodes?: string[]
  createdAt?: string
  updatedAt?: string
}

/**
 * 登录请求
 */
export interface LoginRequest {
  username: string
  password: string
}

/**
 * 登录响应
 */
export interface LoginResponse {
  token: string
  tokenType: string
  user: UserDto
}

/**
 * 资源页面相关类型
 */
export interface ResourcePageDto {
  id: number
  name: string
  code: string
  path: string
  description?: string
  icon?: string
  sortOrder?: number
  createdAt?: string
  updatedAt?: string
}

/**
 * 用户团队关系相关类型
 */
export interface UserTeamDto {
  id: number
  userId: number
  teamId: number
  isOwner?: boolean
  createdAt?: string
  updatedAt?: string
  // 关联信息
  userName?: string
  userRealName?: string
  userEmail?: string
  teamName?: string
  teamColor?: string
}
