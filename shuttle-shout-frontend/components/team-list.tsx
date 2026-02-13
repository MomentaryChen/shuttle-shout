"use client"

import { useEffect, useState, useMemo } from "react"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import { Progress } from "@/components/ui/progress"
import { Button } from "@/components/ui/button"
import { teamApi } from "@/lib/api"
import { TeamDto } from "@/types/api"
import { useAuth } from "@/contexts/AuthContext"
import { Spinner } from "@/components/ui/spinner"
import { toast } from "sonner"
import { format, formatDistanceToNow } from "date-fns"
import { zhCN } from "date-fns/locale/zh-CN"
import { 
  Users, 
  MapPin, 
  Activity, 
  Search, 
  RefreshCw, 
  TrendingUp,
  Clock,
  Filter,
  X
} from "lucide-react"

interface StatCardProps {
  title: string
  value: string | number
  description?: string
  icon: React.ReactNode
  trend?: string
  className?: string
}

function StatCard({ title, value, description, icon, trend, className }: StatCardProps) {
  return (
    <Card className={`hover:shadow-lg transition-all duration-200 border-blue-100 dark:border-border/50 bg-gradient-to-br from-white to-blue-50/30 dark:from-card dark:to-card/50 ${className || ""}`}>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm font-medium text-muted-foreground">
          {title}
        </CardTitle>
        <div className="text-blue-600 dark:text-primary/70">{icon}</div>
      </CardHeader>
      <CardContent>
        <div className="text-2xl font-bold text-blue-900 dark:text-foreground">{value}</div>
        {description && (
          <p className="text-xs text-muted-foreground mt-1">{description}</p>
        )}
        {trend && (
          <div className="flex items-center gap-1 mt-2 text-xs text-blue-600 dark:text-blue-500">
            <TrendingUp className="w-3 h-3" />
            <span>{trend}</span>
          </div>
        )}
      </CardContent>
    </Card>
  )
}

export function TeamList() {
  const { user } = useAuth()
  const [teams, setTeams] = useState<TeamDto[]>([])
  const [loading, setLoading] = useState(true)
  const [searchQuery, setSearchQuery] = useState("")
  const [filterActive, setFilterActive] = useState<boolean | null>(null)
  const [autoRefresh, setAutoRefresh] = useState(false)

  useEffect(() => {
    // 只在組件首次掛載時載入數據
    console.log("TeamList: 組件掛載，開始載入數據")
    loadTeams()
  }, [])

  useEffect(() => {
    console.log("TeamList: loading狀態變更", { loading, teamsCount: teams.length })
  }, [loading, teams.length])

  useEffect(() => {
    // 自動刷新功能
    if (autoRefresh) {
      const interval = setInterval(() => {
        loadTeams()
      }, 30000) // 每30秒自動刷新
      return () => clearInterval(interval)
    }
  }, [autoRefresh])

  const loadTeams = async () => {
    try {
      setLoading(true)
      const teamsData = await teamApi.getAll()

      // 按創立時間排序，最新的在前
      // 过滤只显示当前用户创建的球队
      const userTeams = teamsData.filter(team => team.userId === user?.id)

      const sortedTeams = [...userTeams].sort((a, b) => {
        if (!a.createdAt && !b.createdAt) return 0
        if (!a.createdAt) return 1
        if (!b.createdAt) return -1
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      })

      setTeams(sortedTeams)
    } catch (error) {
      console.error("加載Team列表失敗:", error)
      toast.error("加載Team列表失敗，請檢查後端服務是否運行")
      // 即使出错也要清除loading状态
      setTeams([]) // 确保teams不为empty，避免loading状态卡住
    } finally {
      setLoading(false)
    }
  }

  // 計算統計數據（總人數以不重複帳號/人為準，同一人跨多隊只計一次）
  const stats = useMemo(() => {
    const totalTeams = teams.length
    const activeTeams = teams.filter(t => t.isActive !== false).length
    const allPlayerIds = teams.flatMap(t => t.playerIds ?? [])
    const totalPlayers = allPlayerIds.length > 0
      ? new Set(allPlayerIds).size
      : teams.reduce((sum, t) => sum + (t.currentPlayerCount || 0), 0)
    const totalCourts = teams.reduce((sum, t) => sum + (t.currentCourtCount || 0), 0)
    const maxPlayers = teams.reduce((sum, t) => sum + (t.maxPlayers || 0), 0)
    const maxCourts = teams.reduce((sum, t) => sum + (t.courtCount || 0), 0)

    return {
      totalTeams,
      activeTeams,
      totalPlayers,
      totalCourts,
      maxPlayers,
      maxCourts,
      playerUtilization: maxPlayers > 0 ? Math.round((totalPlayers / maxPlayers) * 100) : 0,
      courtUtilization: maxCourts > 0 ? Math.round((totalCourts / maxCourts) * 100) : 0,
    }
  }, [teams])

  // 過濾和搜索
  const filteredTeams = useMemo(() => {
    return teams.filter(team => {
      const matchesSearch = team.name.toLowerCase().includes(searchQuery.toLowerCase())
      const matchesFilter = filterActive === null || team.isActive === filterActive
      return matchesSearch && matchesFilter
    })
  }, [teams, searchQuery, filterActive])

  if (loading && teams.length === 0) {
    return (
      <div className="w-full min-h-[400px] flex items-center justify-center">
        <div className="flex flex-col items-center gap-4">
          <Spinner className="w-8 h-8 text-blue-600 dark:text-primary" />
          <p className="text-muted-foreground font-medium">正在載入團隊數據...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* 頁面標題 */}
      <div className="flex flex-col gap-2">
        <h2 className="text-3xl font-bold bg-gradient-to-r from-blue-600 to-blue-500 bg-clip-text text-transparent">
          我的團隊
        </h2>
        <p className="text-muted-foreground">
          查看您創建的所有團隊的統計資訊和使用狀況
        </p>
      </div>

      {/* 統計卡片 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="總Team數"
          value={stats.totalTeams}
          description={`${stats.activeTeams} 個活躍`}
          icon={<Users className="h-4 w-4" />}
          className="animate-in fade-in slide-in-from-left-4 duration-300"
        />
        <StatCard
          title="總人數"
          value={stats.totalPlayers}
          description={`最大容量 ${stats.maxPlayers}`}
          icon={<Users className="h-4 w-4" />}
          trend={`使用率 ${stats.playerUtilization}%`}
          className="animate-in fade-in slide-in-from-left-4 duration-300 delay-75"
        />
        <StatCard
          title="總場地數"
          value={stats.totalCourts}
          description={`最大容量 ${stats.maxCourts}`}
          icon={<MapPin className="h-4 w-4" />}
          trend={`使用率 ${stats.courtUtilization}%`}
          className="animate-in fade-in slide-in-from-left-4 duration-300 delay-150"
        />
        <StatCard
          title="活躍Team"
          value={stats.activeTeams}
          description={`${stats.totalTeams - stats.activeTeams} 個非活躍`}
          icon={<Activity className="h-4 w-4" />}
          className="animate-in fade-in slide-in-from-left-4 duration-300 delay-200"
        />
      </div>

      {/* 搜索和過濾欄 */}
      <Card className="border-blue-100 dark:border-border/50 bg-gradient-to-r from-white to-blue-50/30 dark:from-card dark:to-card/50 shadow-sm">
        <CardHeader className="pb-4">
          <CardTitle className="text-lg">搜尋與篩選</CardTitle>
          <CardDescription>快速找到您需要的團隊資訊</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex flex-col sm:flex-row gap-3">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-blue-500 h-4 w-4" />
              <Input
                placeholder="搜尋團隊名稱..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-10 border-blue-200 focus:border-blue-400 focus:ring-blue-400/20"
              />
            </div>
            <div className="flex flex-wrap gap-2">
              <Button
                variant={filterActive === null ? "default" : "outline"}
                size="sm"
                onClick={() => setFilterActive(null)}
                className="transition-all duration-200 hover:scale-105 active:scale-95"
              >
                <Filter className="h-4 w-4 mr-2" />
                全部
              </Button>
              <Button
                variant={filterActive === true ? "default" : "outline"}
                size="sm"
                onClick={() => setFilterActive(true)}
                className="transition-all duration-200 hover:scale-105 active:scale-95"
              >
                活躍
              </Button>
              <Button
                variant={filterActive === false ? "default" : "outline"}
                size="sm"
                onClick={() => setFilterActive(false)}
                className="transition-all duration-200 hover:scale-105 active:scale-95"
              >
                非活躍
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={loadTeams}
                disabled={loading}
                className="transition-all duration-200 hover:scale-105 active:scale-95"
              >
                <RefreshCw className={`h-4 w-4 mr-2 ${loading ? "animate-spin" : ""}`} />
                刷新
              </Button>
              <Button
                variant={autoRefresh ? "default" : "outline"}
                size="sm"
                onClick={() => setAutoRefresh(!autoRefresh)}
                className="transition-all duration-200 hover:scale-105 active:scale-95"
              >
                {autoRefresh ? "✓ " : ""}自動刷新
              </Button>
            </div>
          </div>
          {(searchQuery || filterActive !== null) && (
            <div className="flex items-center justify-between pt-2 border-t border-blue-100 dark:border-border">
              <div className="flex items-center gap-2">
                <span className="text-sm font-medium text-blue-700 dark:text-blue-400">
                  找到 {filteredTeams.length} 個團隊
                </span>
                {searchQuery && (
                  <Badge variant="secondary" className="gap-1">
                    <span>搜尋: {searchQuery}</span>
                    <button
                      onClick={() => setSearchQuery("")}
                      className="ml-1 hover:bg-destructive/20 rounded-full p-0.5 transition-colors"
                    >
                      <X className="h-3 w-3" />
                    </button>
                  </Badge>
                )}
                {filterActive !== null && (
                  <Badge variant="secondary" className="gap-1">
                    <span>狀態: {filterActive ? "活躍" : "非活躍"}</span>
                    <button
                      onClick={() => setFilterActive(null)}
                      className="ml-1 hover:bg-destructive/20 rounded-full p-0.5 transition-colors"
                    >
                      <X className="h-3 w-3" />
                    </button>
                  </Badge>
                )}
              </div>
              {(searchQuery || filterActive !== null) && (
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => {
                    setSearchQuery("")
                    setFilterActive(null)
                  }}
                  className="text-xs h-7"
                >
                  清除所有篩選
                </Button>
              )}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Team卡片網格 */}
      {/* 團隊卡片網格 */}
      {filteredTeams.length === 0 ? (
        <Card className="border-blue-100 dark:border-border/50 bg-gradient-to-br from-blue-50/50 to-white dark:from-card/30 dark:to-card/50">
          <CardContent className="py-16">
            <div className="text-center space-y-4">
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-blue-100 dark:bg-blue-900/20 mb-4">
                <Users className="h-8 w-8 text-blue-500 dark:text-blue-400" />
              </div>
              <div>
                <h3 className="text-lg font-semibold text-foreground mb-2">
                  {searchQuery || filterActive !== null
                    ? "沒有找到符合條件的團隊"
                    : "您還沒有創建任何團隊"}
                </h3>
                <p className="text-sm text-muted-foreground">
                  {searchQuery || filterActive !== null
                    ? "請嘗試調整搜尋條件或篩選器"
                    : "前往「球隊管理」頁面創建您的第一個團隊"}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-semibold text-foreground">
              團隊列表 ({filteredTeams.length})
            </h3>
            <span className="text-sm text-muted-foreground">
              按創立時間排序，最新的在前
            </span>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredTeams.map((team, index) => {
            const playerUsage = team.maxPlayers > 0 
              ? ((team.currentPlayerCount || 0) / team.maxPlayers) * 100 
              : 0
            const courtUsage = team.courtCount > 0 
              ? ((team.currentCourtCount || 0) / team.courtCount) * 100 
              : 0

            return (
              <Card 
                key={team.id} 
                className={`group border-2 border-blue-100 dark:border-border/50 bg-white dark:bg-card/50 hover:border-blue-300 dark:hover:border-blue-500/50 hover:shadow-xl transition-all duration-300 hover:scale-[1.02] animate-in fade-in slide-in-from-bottom-4 duration-300 overflow-hidden`}
                style={{ animationDelay: `${index * 50}ms` }}
              >
                {/* 頂部顏色條 */}
                {team.color && (
                  <div 
                    className="h-1 w-full"
                    style={{ backgroundColor: team.color }}
                  />
                )}
                <CardHeader className="pb-3">
                  <div className="flex items-start justify-between gap-3">
                    <div className="flex items-center gap-3 flex-1 min-w-0">
                      {team.color && (
                        <div
                          className="w-14 h-14 rounded-xl flex-shrink-0 shadow-lg group-hover:shadow-xl transition-all duration-300 group-hover:scale-110 border-2 border-white dark:border-card"
                          style={{ backgroundColor: team.color }}
                        />
                      )}
                      <div className="flex-1 min-w-0">
                        <CardTitle className="text-xl font-bold truncate text-foreground group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">
                          {team.name}
                        </CardTitle>
                        {team.createdAt && (
                          <CardDescription className="flex items-center gap-1.5 mt-1.5">
                            <Clock className="h-3.5 w-3.5" />
                            <span className="text-xs">
                              {formatDistanceToNow(new Date(team.createdAt), { 
                                addSuffix: true, 
                                locale: zhCN 
                              })}
                            </span>
                          </CardDescription>
                        )}
                      </div>
                    </div>
                    {team.isActive !== false ? (
                      <Badge className="bg-blue-100 text-blue-700 border-blue-200 dark:bg-blue-500/20 dark:text-blue-300 dark:border-blue-500/30 shrink-0">
                        活躍
                      </Badge>
                    ) : (
                      <Badge variant="secondary" className="shrink-0">非活躍</Badge>
                    )}
                  </div>
                </CardHeader>
                <CardContent className="space-y-4 pt-0">
                  {/* 人數統計 */}
                  <div className="space-y-2.5 p-3 rounded-lg bg-blue-50/50 dark:bg-blue-950/20 border border-blue-100 dark:border-blue-900/30">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2 text-sm font-medium text-blue-900 dark:text-blue-100">
                        <Users className="h-4 w-4" />
                        <span>人數</span>
                      </div>
                      <div className="font-bold text-blue-700 dark:text-blue-300">
                        {team.currentPlayerCount || 0} / {team.maxPlayers || 0}
                      </div>
                    </div>
                    <Progress value={playerUsage} className="h-2.5 bg-blue-100 dark:bg-blue-900/30" />
                    <div className="flex items-center justify-between">
                      <span className="text-xs text-muted-foreground">使用率</span>
                      <span className={`text-xs font-semibold ${
                        playerUsage >= 80 ? "text-red-600 dark:text-red-400" :
                        playerUsage >= 50 ? "text-orange-600 dark:text-orange-400" :
                        "text-green-600 dark:text-green-400"
                      }`}>
                        {Math.round(playerUsage)}%
                      </span>
                    </div>
                  </div>

                  {/* 場地統計 */}
                  <div className="space-y-2.5 p-3 rounded-lg bg-blue-50/50 dark:bg-blue-950/20 border border-blue-100 dark:border-blue-900/30">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2 text-sm font-medium text-blue-900 dark:text-blue-100">
                        <MapPin className="h-4 w-4" />
                        <span>場地</span>
                      </div>
                      <div className="font-bold text-blue-700 dark:text-blue-300">
                        {team.currentCourtCount || 0} / {team.courtCount || 0}
                      </div>
                    </div>
                    <Progress value={courtUsage} className="h-2.5 bg-blue-100 dark:bg-blue-900/30" />
                    <div className="flex items-center justify-between">
                      <span className="text-xs text-muted-foreground">使用率</span>
                      <span className={`text-xs font-semibold ${
                        courtUsage >= 80 ? "text-red-600 dark:text-red-400" :
                        courtUsage >= 50 ? "text-orange-600 dark:text-orange-400" :
                        "text-green-600 dark:text-green-400"
                      }`}>
                        {Math.round(courtUsage)}%
                      </span>
                    </div>
                  </div>

                  {/* 創立時間 */}
                  {team.createdAt && (
                    <div className="pt-2 border-t border-blue-100 dark:border-border">
                      <div className="flex items-center gap-2 text-xs text-muted-foreground">
                        <Clock className="h-3.5 w-3.5" />
                        <span>
                          創立於 {format(new Date(team.createdAt), "yyyy年MM月dd日 HH:mm", { locale: zhCN })}
                        </span>
                      </div>
                    </div>
                  )}
                </CardContent>
              </Card>
            )
          })}
          </div>
        </div>
      )}
    </div>
  )
}
