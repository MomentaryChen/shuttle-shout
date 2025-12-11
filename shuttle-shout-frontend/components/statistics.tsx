"use client"

import { useEffect, useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { authApi } from "@/lib/api"
import { UserDto } from "@/types/api"
import { Spinner } from "@/components/ui/spinner"
import { toast } from "sonner"
import { format } from "date-fns"
import { zhCN } from "date-fns/locale/zh-CN"
import {
  BarChart3,
  Users,
  Shield,
  Activity,
  Clock,
  TrendingUp,
  Calendar,
  MapPin,
  Trophy,
  UserCheck,
  RefreshCw
} from "lucide-react"

export function Statistics() {
  const [loading, setLoading] = useState(true)
  const [currentUser, setCurrentUser] = useState<UserDto | null>(null)
  const [hasPermission, setHasPermission] = useState(false)
  const [stats, setStats] = useState({
    totalUsers: 0,
    activeUsers: 0,
    totalTeams: 0,
    activeTeams: 0,
    totalCourts: 0,
    availableCourts: 0,
    totalMatches: 0,
    todayMatches: 0,
    systemUptime: "99.9%",
    lastUpdated: new Date()
  })

  useEffect(() => {
    checkPermissionAndLoadStats()
  }, [])

  const checkPermissionAndLoadStats = async () => {
    try {
      setLoading(true)

      // 获取当前用户信息
      const userInfo = await authApi.getCurrentUser()
      setCurrentUser(userInfo)

      // 检查是否具有管理員角色
      const isAdmin = userInfo.roleNames?.includes("管理員") || false
      setHasPermission(isAdmin)

      if (isAdmin) {
        // 模拟加载统计数据
        await loadStatistics()
      } else {
        toast.error("您沒有權限訪問此頁面")
      }
    } catch (error) {
      console.error("加載統計數據失敗:", error)
      toast.error("加載統計數據失敗，請稍後重試")
      setHasPermission(false)
    } finally {
      setLoading(false)
    }
  }

  const loadStatistics = async () => {
    // 模拟API调用 - 这里应该调用实际的统计API
    try {
      // 模拟延迟
      await new Promise(resolve => setTimeout(resolve, 1000))

      // 这里应该从API获取真实数据
      setStats({
        totalUsers: 156,
        activeUsers: 142,
        totalTeams: 23,
        activeTeams: 19,
        totalCourts: 8,
        availableCourts: 6,
        totalMatches: 89,
        todayMatches: 5,
        systemUptime: "99.8%",
        lastUpdated: new Date()
      })
    } catch (error) {
      console.error("獲取統計數據失敗:", error)
      toast.error("獲取統計數據失敗")
    }
  }

  const refreshStats = async () => {
    await loadStatistics()
    toast.success("統計數據已更新")
  }

  // 如果没有权限，显示错误信息
  if (!loading && !hasPermission) {
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
                  只有管理員才能訪問此頁面
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    )
  }

  if (loading) {
    return (
      <div className="w-full min-h-[400px] flex items-center justify-center">
        <div className="flex flex-col items-center gap-4">
          <Spinner className="w-8 h-8 text-blue-600 dark:text-primary" />
          <p className="text-muted-foreground font-medium">正在載入統計數據...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* 页面标题和刷新按钮 */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-blue-900 dark:text-blue-100">
            統計報表
          </h2>
          <p className="text-sm text-muted-foreground mt-1">
            系統數據統計和報表總覽
          </p>
        </div>
        <div className="flex items-center gap-3">
          <div className="text-right text-xs text-muted-foreground">
            <p>最後更新: {format(stats.lastUpdated, "HH:mm:ss", { locale: zhCN })}</p>
            <p>系統正常運行時間: {stats.systemUptime}</p>
          </div>
          <Button
            onClick={refreshStats}
            variant="outline"
            size="sm"
            className="h-8"
          >
            <RefreshCw className="h-4 w-4 mr-2" />
            刷新
          </Button>
        </div>
      </div>

      {/* 用户统计 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card className="border-blue-100 dark:border-border/50">
          <CardContent className="p-4">
            <div className="flex items-center gap-3">
              <div className="inline-flex items-center justify-center w-10 h-10 rounded-lg bg-blue-100 dark:bg-blue-900/20">
                <Users className="h-5 w-5 text-blue-600 dark:text-blue-400" />
              </div>
              <div>
                <p className="text-2xl font-bold text-blue-900 dark:text-blue-100">{stats.totalUsers}</p>
                <p className="text-sm text-blue-700 dark:text-blue-300">總用戶數</p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="border-green-100 dark:border-border/50">
          <CardContent className="p-4">
            <div className="flex items-center gap-3">
              <div className="inline-flex items-center justify-center w-10 h-10 rounded-lg bg-green-100 dark:bg-green-900/20">
                <UserCheck className="h-5 w-5 text-green-600 dark:text-green-400" />
              </div>
              <div>
                <p className="text-2xl font-bold text-green-900 dark:text-green-100">{stats.activeUsers}</p>
                <p className="text-sm text-green-700 dark:text-green-300">活躍用戶</p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="border-purple-100 dark:border-border/50">
          <CardContent className="p-4">
            <div className="flex items-center gap-3">
              <div className="inline-flex items-center justify-center w-10 h-10 rounded-lg bg-purple-100 dark:bg-purple-900/20">
                <Shield className="h-5 w-5 text-purple-600 dark:text-purple-400" />
              </div>
              <div>
                <p className="text-2xl font-bold text-purple-900 dark:text-purple-100">12</p>
                <p className="text-sm text-purple-700 dark:text-purple-300">管理員</p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="border-orange-100 dark:border-border/50">
          <CardContent className="p-4">
            <div className="flex items-center gap-3">
              <div className="inline-flex items-center justify-center w-10 h-10 rounded-lg bg-orange-100 dark:bg-orange-900/20">
                <Activity className="h-5 w-5 text-orange-600 dark:text-orange-400" />
              </div>
              <div>
                <p className="text-2xl font-bold text-orange-900 dark:text-orange-100">92.3%</p>
                <p className="text-sm text-orange-700 dark:text-orange-300">用戶活躍度</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* 球队和球场统计 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card className="border-indigo-100 dark:border-border/50">
          <CardContent className="p-4">
            <div className="flex items-center gap-3">
              <div className="inline-flex items-center justify-center w-10 h-10 rounded-lg bg-indigo-100 dark:bg-indigo-900/20">
                <Users className="h-5 w-5 text-indigo-600 dark:text-indigo-400" />
              </div>
              <div>
                <p className="text-2xl font-bold text-indigo-900 dark:text-indigo-100">{stats.totalTeams}</p>
                <p className="text-sm text-indigo-700 dark:text-indigo-300">總球隊數</p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="border-teal-100 dark:border-border/50">
          <CardContent className="p-4">
            <div className="flex items-center gap-3">
              <div className="inline-flex items-center justify-center w-10 h-10 rounded-lg bg-teal-100 dark:bg-teal-900/20">
                <Activity className="h-5 w-5 text-teal-600 dark:text-teal-400" />
              </div>
              <div>
                <p className="text-2xl font-bold text-teal-900 dark:text-teal-100">{stats.activeTeams}</p>
                <p className="text-sm text-teal-700 dark:text-teal-300">活躍球隊</p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="border-cyan-100 dark:border-border/50">
          <CardContent className="p-4">
            <div className="flex items-center gap-3">
              <div className="inline-flex items-center justify-center w-10 h-10 rounded-lg bg-cyan-100 dark:bg-cyan-900/20">
                <MapPin className="h-5 w-5 text-cyan-600 dark:text-cyan-400" />
              </div>
              <div>
                <p className="text-2xl font-bold text-cyan-900 dark:text-cyan-100">{stats.totalCourts}</p>
                <p className="text-sm text-cyan-700 dark:text-cyan-300">總球場數</p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="border-emerald-100 dark:border-border/50">
          <CardContent className="p-4">
            <div className="flex items-center gap-3">
              <div className="inline-flex items-center justify-center w-10 h-10 rounded-lg bg-emerald-100 dark:bg-emerald-900/20">
                <Clock className="h-5 w-5 text-emerald-600 dark:text-emerald-400" />
              </div>
              <div>
                <p className="text-2xl font-bold text-emerald-900 dark:text-emerald-100">{stats.availableCourts}</p>
                <p className="text-sm text-emerald-700 dark:text-emerald-300">可用球場</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* 比赛统计 */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <Card className="border-amber-100 dark:border-border/50">
          <CardContent className="p-4">
            <div className="flex items-center gap-3">
              <div className="inline-flex items-center justify-center w-10 h-10 rounded-lg bg-amber-100 dark:bg-amber-900/20">
                <Trophy className="h-5 w-5 text-amber-600 dark:text-amber-400" />
              </div>
              <div>
                <p className="text-2xl font-bold text-amber-900 dark:text-amber-100">{stats.totalMatches}</p>
                <p className="text-sm text-amber-700 dark:text-amber-300">總比賽場次</p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="border-rose-100 dark:border-border/50">
          <CardContent className="p-4">
            <div className="flex items-center gap-3">
              <div className="inline-flex items-center justify-center w-10 h-10 rounded-lg bg-rose-100 dark:bg-rose-900/20">
                <Calendar className="h-5 w-5 text-rose-600 dark:text-rose-400" />
              </div>
              <div>
                <p className="text-2xl font-bold text-rose-900 dark:text-rose-100">{stats.todayMatches}</p>
                <p className="text-sm text-rose-700 dark:text-rose-300">今日比賽</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* 图表区域预留 */}
      <Card className="border-blue-100 dark:border-border/50">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <BarChart3 className="h-5 w-5 text-blue-600 dark:text-blue-400" />
            數據趨勢圖表
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="h-64 flex items-center justify-center border-2 border-dashed border-blue-200 dark:border-border/50 rounded-lg">
            <div className="text-center space-y-2">
              <BarChart3 className="h-12 w-12 text-blue-400 dark:text-blue-600 mx-auto" />
              <p className="text-muted-foreground">圖表功能開發中...</p>
              <p className="text-xs text-muted-foreground">將顯示用戶活躍度、比賽統計等趨勢數據</p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* 系统状态 */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card className="border-green-100 dark:border-border/50">
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-green-900 dark:text-green-100">系統狀態</p>
                <p className="text-xs text-green-700 dark:text-green-300">運行正常</p>
              </div>
              <div className="inline-flex items-center justify-center w-8 h-8 rounded-full bg-green-100 dark:bg-green-900/20">
                <Activity className="h-4 w-4 text-green-600 dark:text-green-400" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="border-blue-100 dark:border-border/50">
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-blue-900 dark:text-blue-100">數據庫連接</p>
                <p className="text-xs text-blue-700 dark:text-blue-300">正常</p>
              </div>
              <div className="inline-flex items-center justify-center w-8 h-8 rounded-full bg-blue-100 dark:bg-blue-900/20">
                <TrendingUp className="h-4 w-4 text-blue-600 dark:text-blue-400" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="border-purple-100 dark:border-border/50">
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-purple-900 dark:text-purple-100">API響應</p>
                <p className="text-xs text-purple-700 dark:text-purple-300">良好</p>
              </div>
              <div className="inline-flex items-center justify-center w-8 h-8 rounded-full bg-purple-100 dark:bg-purple-900/20">
                <Clock className="h-4 w-4 text-purple-600 dark:text-purple-400" />
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
