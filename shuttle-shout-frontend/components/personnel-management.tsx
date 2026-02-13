"use client"

import { useEffect, useState, useMemo } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { adminApi } from "@/lib/api"
import { useAuth } from "@/contexts/AuthContext"
import { UserDto } from "@/types/api"
import { Spinner } from "@/components/ui/spinner"
import { toast } from "sonner"
import { format, formatDistanceToNow } from "date-fns"
import { zhCN } from "date-fns/locale/zh-CN"
import {
  Users,
  Shield,
  Activity,
  Clock,
  Search,
  X,
  Filter,
  UserCheck,
  UserX,
  Mail,
  Phone
} from "lucide-react"

const PAGE_CODE_PERSONNEL_MANAGEMENT = "PERSONNEL_MANAGEMENT"

export function PersonnelManagement() {
  const { user: currentUser, isLoading: authLoading, hasPageAccess } = useAuth()
  const hasPermission = hasPageAccess(PAGE_CODE_PERSONNEL_MANAGEMENT)
  const [users, setUsers] = useState<UserDto[]>([])
  const [loading, setLoading] = useState(true)
  const [searchQuery, setSearchQuery] = useState("")
  const [searchField, setSearchField] = useState<"username" | "email" | "realName" | "all">("all")

  useEffect(() => {
    if (!hasPermission || authLoading) {
      if (!authLoading) setLoading(false)
      return
    }
    let cancelled = false
    adminApi.getAllUsers()
      .then((usersData) => {
        if (cancelled) return
        const sortedUsers = [...usersData].sort((a, b) => {
          if (!a.createdAt && !b.createdAt) return 0
          if (!a.createdAt) return 1
          if (!b.createdAt) return -1
          return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        })
        setUsers(sortedUsers)
      })
      .catch((err) => {
        if (!cancelled) {
          console.error("加載用戶數據失敗:", err)
          toast.error("加載用戶數據失敗，請稍後重試")
        }
      })
      .finally(() => { if (!cancelled) setLoading(false) })
    return () => { cancelled = true }
  }, [hasPermission, authLoading])

  // 搜索和过滤逻辑
  const filteredUsers = useMemo(() => {
    if (!searchQuery.trim()) {
      return users
    }

    const query = searchQuery.toLowerCase().trim()

    return users.filter(user => {
      if (searchField === "username") {
        return user.username.toLowerCase().includes(query)
      } else if (searchField === "email") {
        return user.email?.toLowerCase().includes(query)
      } else if (searchField === "realName") {
        return user.realName?.toLowerCase().includes(query)
      } else {
        // 搜索所有字段
        return (
          user.username.toLowerCase().includes(query) ||
          user.email?.toLowerCase().includes(query) ||
          user.realName?.toLowerCase().includes(query) ||
          user.phoneNumber?.toLowerCase().includes(query) ||
          user.id.toString().includes(query)
        )
      }
    })
  }, [users, searchQuery, searchField])

  // 計算簡單統計數據（管理員：roleCodes 含 SYSTEM_ADMIN 或 Admin，或 roleNames 含對應名稱）
  const isAdminByRole = (u: UserDto) =>
    (u.roleCodes?.length
      ? u.roleCodes.some(c => c === "SYSTEM_ADMIN" || c === "Admin" || c?.toUpperCase() === "ADMIN")
      : u.roleNames?.some(name => name === "系統管理員" || name === "管理員" || name === "Admin" || name?.toUpperCase() === "ADMIN"))
  const stats = useMemo(() => {
    const activeUsers = filteredUsers.filter(u => u.isActive !== false).length
    const inactiveUsers = filteredUsers.length - activeUsers
    const adminUsers = filteredUsers.filter(isAdminByRole).length

    return {
      totalUsers: filteredUsers.length,
      activeUsers,
      inactiveUsers,
      adminUsers,
    }
  }, [filteredUsers])

  // 權限與載入：依後端可存取頁面判斷（管理員已含全部頁面）
  if (authLoading || (loading && hasPermission)) {
    return (
      <div className="w-full min-h-[400px] flex items-center justify-center">
        <div className="flex flex-col items-center gap-4">
          <Spinner className="w-8 h-8 text-blue-600 dark:text-primary" />
          <p className="text-muted-foreground font-medium">正在載入...</p>
        </div>
      </div>
    )
  }
  if (!hasPermission) {
    return (
      <div className="w-full min-h-[400px] flex items-center justify-center">
        <Card className="w-full max-w-md">
          <CardContent className="py-16">
            <div className="text-center space-y-4">
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-red-100 dark:bg-red-900/20 mb-4">
                <Shield className="h-8 w-8 text-red-500 dark:text-red-400" />
              </div>
              <div>
                <h3 className="text-xl font-semibold text-foreground mb-2">
                  權限不足
                </h3>
                <p className="text-muted-foreground">
                  只有系統管理員才能訪問此頁面
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    )
  }

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* 页面标题 */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-blue-900 dark:text-blue-100">
            人員配置管理
          </h2>
          <p className="text-sm text-muted-foreground mt-1">
            查看和管理系統中的所有用戶信息
          </p>
        </div>
        <div className="text-right">
          <p className="text-xs text-muted-foreground">
            共 {users.length} 個用戶
          </p>
        </div>
      </div>

      {/* 简要统计 */}
      <div className="grid grid-cols-4 gap-4">
        <div className="bg-blue-50 dark:bg-blue-950/20 rounded-lg p-4 border border-blue-100 dark:border-blue-900/30">
          <div className="flex items-center gap-3">
            <Users className="h-5 w-5 text-blue-600 dark:text-blue-400" />
            <div>
              <p className="text-2xl font-bold text-blue-900 dark:text-blue-100">{stats.totalUsers}</p>
              <p className="text-sm text-blue-700 dark:text-blue-300">總用戶數</p>
            </div>
          </div>
        </div>

        <div className="bg-green-50 dark:bg-green-950/20 rounded-lg p-4 border border-green-100 dark:border-green-900/30">
          <div className="flex items-center gap-3">
            <UserCheck className="h-5 w-5 text-green-600 dark:text-green-400" />
            <div>
              <p className="text-2xl font-bold text-green-900 dark:text-green-100">{stats.activeUsers}</p>
              <p className="text-sm text-green-700 dark:text-green-300">活躍用戶</p>
            </div>
          </div>
        </div>

        <div className="bg-red-50 dark:bg-red-950/20 rounded-lg p-4 border border-red-100 dark:border-red-900/30">
          <div className="flex items-center gap-3">
            <UserX className="h-5 w-5 text-red-600 dark:text-red-400" />
            <div>
              <p className="text-2xl font-bold text-red-900 dark:text-red-100">{stats.inactiveUsers}</p>
              <p className="text-sm text-red-700 dark:text-red-300">非活躍用戶</p>
            </div>
          </div>
        </div>

        <div className="bg-purple-50 dark:bg-purple-950/20 rounded-lg p-4 border border-purple-100 dark:border-purple-900/30">
          <div className="flex items-center gap-3">
            <Shield className="h-5 w-5 text-purple-600 dark:text-purple-400" />
            <div>
              <p className="text-2xl font-bold text-purple-900 dark:text-purple-100">{stats.adminUsers}</p>
              <p className="text-sm text-purple-700 dark:text-purple-300">管理員</p>
            </div>
          </div>
        </div>
      </div>

      {/* 搜索区域 */}
      <div className="bg-gradient-to-r from-blue-50/30 to-white dark:from-blue-950/10 dark:to-card/50 rounded-xl border border-blue-100 dark:border-border/50 p-4 shadow-sm">
        <div className="flex flex-col sm:flex-row gap-3">
          {/* 搜索输入框 */}
          <div className="relative flex-1 max-w-md">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-blue-500 dark:text-blue-400 h-4 w-4" />
            <Input
              placeholder="搜尋用戶..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-9 pr-9 h-9 bg-white dark:bg-card border-blue-200 dark:border-border/50 focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 text-sm placeholder:text-muted-foreground/70"
            />
            {searchQuery && (
              <button
                onClick={() => setSearchQuery("")}
                className="absolute right-2 top-1/2 transform -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors rounded-full hover:bg-muted p-1"
              >
                <X className="h-3 w-3" />
              </button>
            )}
          </div>

          {/* 搜索类型切换 */}
          <div className="flex gap-1">
            <Button
              variant={searchField === "all" ? "default" : "outline"}
              size="sm"
              onClick={() => setSearchField("all")}
              className={`h-9 px-3 text-xs font-medium transition-all duration-200 ${
                searchField === "all"
                  ? "bg-blue-600 hover:bg-blue-700 text-white shadow-md"
                  : "hover:bg-blue-50 dark:hover:bg-blue-950/50 border-blue-200 dark:border-border/50"
              }`}
            >
              <Filter className="h-3 w-3 mr-1.5" />
              全部
            </Button>
            <Button
              variant={searchField === "username" ? "default" : "outline"}
              size="sm"
              onClick={() => setSearchField("username")}
              className={`h-9 px-3 text-xs font-medium transition-all duration-200 ${
                searchField === "username"
                  ? "bg-blue-600 hover:bg-blue-700 text-white shadow-md"
                  : "hover:bg-blue-50 dark:hover:bg-blue-950/50 border-blue-200 dark:border-border/50"
              }`}
            >
              用戶名
            </Button>
            <Button
              variant={searchField === "email" ? "default" : "outline"}
              size="sm"
              onClick={() => setSearchField("email")}
              className={`h-9 px-3 text-xs font-medium transition-all duration-200 ${
                searchField === "email"
                  ? "bg-blue-600 hover:bg-blue-700 text-white shadow-md"
                  : "hover:bg-blue-50 dark:hover:bg-blue-950/50 border-blue-200 dark:border-border/50"
              }`}
            >
              郵箱
            </Button>
            <Button
              variant={searchField === "realName" ? "default" : "outline"}
              size="sm"
              onClick={() => setSearchField("realName")}
              className={`h-9 px-3 text-xs font-medium transition-all duration-200 ${
                searchField === "realName"
                  ? "bg-blue-600 hover:bg-blue-700 text-white shadow-md"
                  : "hover:bg-blue-50 dark:hover:bg-blue-950/50 border-blue-200 dark:border-border/50"
              }`}
            >
              真實姓名
            </Button>
          </div>

          {/* 搜索结果统计 */}
          {searchQuery && (
            <div className="flex items-center gap-2 sm:ml-auto">
              <span className="text-xs font-medium text-blue-700 dark:text-blue-400">
                找到 {filteredUsers.length} 個用戶
              </span>
              <Badge variant="secondary" className="text-xs px-2 py-0.5 h-5">
                "{searchQuery}"
              </Badge>
            </div>
          )}
        </div>

        {/* 搜索状态提示 */}
        {searchQuery && filteredUsers.length === 0 && (
          <div className="mt-3 p-3 bg-amber-50 dark:bg-amber-950/20 border border-amber-200 dark:border-amber-800/30 rounded-lg">
            <p className="text-xs text-amber-700 dark:text-amber-400 flex items-center gap-2">
              <Search className="h-3 w-3" />
              沒有找到包含 "{searchQuery}" 的用戶
            </p>
          </div>
        )}
      </div>

      {/* 用户展示区域 */}
      {filteredUsers.length === 0 ? (
        <Card className="border-2 border-blue-100 dark:border-border/50 bg-gradient-to-br from-blue-50/50 to-white dark:from-card/30 dark:to-card/50">
          <CardContent className="py-16">
            <div className="text-center space-y-4">
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-blue-100 dark:bg-blue-900/20 mb-4">
                <Users className="h-8 w-8 text-blue-500 dark:text-blue-400" />
              </div>
              <div>
                <h3 className="text-xl font-semibold text-foreground mb-2">
                  {searchQuery ? "沒有找到符合條件的用戶" : "目前沒有任何用戶"}
                </h3>
                <p className="text-muted-foreground">
                  {searchQuery ? "請嘗試調整搜尋條件" : "系統中沒有用戶數據"}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      ) : (
        <Card className="border-blue-100 dark:border-border/50">
          <CardHeader>
            <CardTitle className="flex items-center justify-between">
              <span>用戶列表</span>
              <Badge variant="outline" className="text-blue-600 border-blue-200">
                {searchQuery ? `共 ${filteredUsers.length} 個用戶` : `共 ${users.length} 個用戶`}
              </Badge>
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow className="border-blue-100 dark:border-border/50">
                    <TableHead className="font-semibold text-blue-900 dark:text-blue-100">用戶名</TableHead>
                    <TableHead className="font-semibold text-blue-900 dark:text-blue-100">真實姓名</TableHead>
                    <TableHead className="font-semibold text-blue-900 dark:text-blue-100">郵箱</TableHead>
                    <TableHead className="font-semibold text-blue-900 dark:text-blue-100">手機號碼</TableHead>
                    <TableHead className="font-semibold text-blue-900 dark:text-blue-100 text-center">狀態</TableHead>
                    <TableHead className="font-semibold text-blue-900 dark:text-blue-100">角色</TableHead>
                    <TableHead className="font-semibold text-blue-900 dark:text-blue-100 text-center">最後登錄</TableHead>
                    <TableHead className="font-semibold text-blue-900 dark:text-blue-100 text-center">註冊時間</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredUsers.map((user) => (
                    <TableRow key={user.id} className="border-blue-50 dark:border-border/30 hover:bg-blue-50/30 dark:hover:bg-blue-950/10 transition-colors">
                      <TableCell className="font-medium">
                        <div className="flex items-center gap-2">
                          <span className="text-foreground">{user.username}</span>
                          {user.id === currentUser?.id && (
                            <Badge variant="secondary" className="text-xs">我</Badge>
                          )}
                        </div>
                      </TableCell>
                      <TableCell className="text-foreground">
                        {user.realName || "-"}
                      </TableCell>
                      <TableCell className="text-sm">
                        {user.email ? (
                          <div className="flex items-center gap-1">
                            <Mail className="h-3 w-3 text-muted-foreground" />
                            <span className="text-foreground">{user.email}</span>
                          </div>
                        ) : (
                          <span className="text-muted-foreground">-</span>
                        )}
                      </TableCell>
                      <TableCell className="text-sm">
                        {user.phoneNumber ? (
                          <div className="flex items-center gap-1">
                            <Phone className="h-3 w-3 text-muted-foreground" />
                            <span className="text-foreground">{user.phoneNumber}</span>
                          </div>
                        ) : (
                          <span className="text-muted-foreground">-</span>
                        )}
                      </TableCell>
                      <TableCell className="text-center">
                        {user.isActive !== false ? (
                          <Badge className="bg-green-100 text-green-700 border-green-200 dark:bg-green-500/20 dark:text-green-300 dark:border-green-500/30">
                            <Activity className="w-3 h-3 mr-1" />
                            活躍
                          </Badge>
                        ) : (
                          <Badge variant="secondary">
                            <UserX className="w-3 h-3 mr-1" />
                            停用
                          </Badge>
                        )}
                      </TableCell>
                      <TableCell>
                        <div className="flex flex-wrap gap-1">
                          {(user.roleNames?.length || user.roleCodes?.length) ? (
                            (user.roleCodes?.length ? user.roleCodes : user.roleNames ?? []).map((roleKey) => {
                              const isAdminRole = roleKey === "SYSTEM_ADMIN" || roleKey === "Admin" || roleKey?.toUpperCase() === "ADMIN" ||
                                ["系統管理員", "管理員"].includes(roleKey)
                              return (
                              <Badge
                                key={roleKey}
                                variant={isAdminRole ? "default" : "secondary"}
                                className={`text-xs ${
                                  isAdminRole
                                    ? "bg-red-100 text-red-700 border-red-200 dark:bg-red-500/20 dark:text-red-300 dark:border-red-500/30"
                                    : ""
                                }`}
                              >
                                {isAdminRole && <Shield className="w-3 h-3 mr-1" />}
                                {roleKey}
                              </Badge>
                            )})
                          ) : (
                            <Badge variant="outline" className="text-xs">
                              普通用戶
                            </Badge>
                          )}
                        </div>
                      </TableCell>
                      <TableCell className="text-center text-sm text-muted-foreground">
                        {user.lastLoginAt ? (
                          <div className="flex flex-col gap-1">
                            <span>{format(new Date(user.lastLoginAt), "MM/dd HH:mm", { locale: zhCN })}</span>
                            <span className="text-xs">
                              {formatDistanceToNow(new Date(user.lastLoginAt), {
                                addSuffix: true,
                                locale: zhCN
                              })}
                            </span>
                          </div>
                        ) : (
                          <span>從未登錄</span>
                        )}
                      </TableCell>
                      <TableCell className="text-center text-sm text-muted-foreground">
                        {user.createdAt ? (
                          <div className="flex flex-col gap-1">
                            <span>{format(new Date(user.createdAt), "MM/dd", { locale: zhCN })}</span>
                            <span className="text-xs">
                              {formatDistanceToNow(new Date(user.createdAt), {
                                addSuffix: true,
                                locale: zhCN
                              })}
                            </span>
                          </div>
                        ) : (
                          <span>-</span>
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  )
}
