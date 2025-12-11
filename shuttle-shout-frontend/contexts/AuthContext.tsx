"use client"

import React, { createContext, useContext, useState, useEffect, useCallback, ReactNode } from "react"
import { UserDto, ResourcePageDto } from "@/types/api"
import { authApi, resourcePageApi, getStoredToken, clearStoredToken } from "@/lib/api"

interface AuthContextType {
  user: UserDto | null
  token: string | null
  isLoading: boolean
  isAuthenticated: boolean
  accessiblePages: ResourcePageDto[]
  login: (username: string, password: string) => Promise<void>
  setLoginState: (response: { token: string; user: UserDto }) => Promise<void>
  logout: () => Promise<void>
  refreshUser: () => Promise<void>
  loadAccessiblePages: () => Promise<void>
  hasPageAccess: (pageCode: string) => boolean
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserDto | null>(null)
  const [token, setToken] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [accessiblePages, setAccessiblePages] = useState<ResourcePageDto[]>([])

  const refreshUser = useCallback(async () => {
    try {
      const userData = await authApi.getCurrentUser()
      setUser(userData)
    } catch (error) {
      throw error
    }
  }, [])

  const loadAccessiblePages = useCallback(async () => {
    try {
      const pages = await resourcePageApi.getMyAccessible()
      setAccessiblePages(pages)
    } catch (error) {
      console.error("加载可访问页面失败:", error)
      setAccessiblePages([])
    }
  }, [])

  const hasPageAccess = useCallback((pageCode: string): boolean => {
    return accessiblePages.some(page => page.code === pageCode)
  }, [accessiblePages])

  // 初始化时检查是否有token
  useEffect(() => {
    const storedToken = getStoredToken()
    if (storedToken) {
      setToken(storedToken)
      // 尝试获取用户信息和可访问页面
      Promise.all([refreshUser(), loadAccessiblePages()]).catch(() => {
        // 如果获取失败，清除token
        clearStoredToken()
        setToken(null)
        setAccessiblePages([])
      }).finally(() => {
        setIsLoading(false)
      })
    } else {
      setIsLoading(false)
    }
  }, [refreshUser, loadAccessiblePages])

  const login = async (username: string, password: string) => {
    try {
      const response = await authApi.login({ username, password })
      setToken(response.token)
      setUser(response.user)
      // 登录成功后获取可访问页面，如果失败不影响登录状态
      try {
        await loadAccessiblePages()
      } catch (pageError) {
        console.error("加载可访问页面失败:", pageError)
        // 即使加载页面失败，也不影响登录状态
        setAccessiblePages([])
      }
    } catch (error) {
      throw error
    }
  }

  // 使用LoginResponse直接设置登录状态（用于注册后自动登录）
  const setLoginState = useCallback(async (response: { token: string; user: UserDto }) => {
    setToken(response.token)
    setUser(response.user)
    // 获取可访问页面
    await loadAccessiblePages()
  }, [loadAccessiblePages])

  const logout = async () => {
    try {
      await authApi.logout()
    } catch (error) {
      console.error("登出失败:", error)
    } finally {
      setToken(null)
      setUser(null)
      setAccessiblePages([])
      clearStoredToken()
    }
  }

  const value: AuthContextType = {
    user,
    token,
    isLoading,
    isAuthenticated: !!token && !!user,
    accessiblePages,
    login,
    setLoginState,
    logout,
    refreshUser,
    loadAccessiblePages,
    hasPageAccess,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider")
  }
  return context
}

