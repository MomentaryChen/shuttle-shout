"use client"

import { useEffect, useState, useMemo } from "react"
import { useRouter } from "next/navigation"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Progress } from "@/components/ui/progress"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog"
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from "@/components/ui/alert-dialog"
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip"
import { RegisterForm } from "@/components/register-form"
import { useAuth } from "@/contexts/AuthContext"
import { teamApi, userTeamApi, userApi } from "@/lib/api"
import { TeamDto, UserDto } from "@/types/api"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { Spinner } from "@/components/ui/spinner"
import { toast } from "sonner"
import { format, formatDistanceToNow } from "date-fns"
import { zhCN } from "date-fns/locale/zh-CN"
import {
  Users,
  MapPin,
  Activity,
  Clock,
  Target,
  TrendingUp,
  Search,
  X,
  Filter,
  UserPlus,
  CheckCircle2,
  Eye,
  LogOut
} from "lucide-react"

export function UserTeamOverview() {
  const { user, isAuthenticated } = useAuth()
  const router = useRouter()
  const [teams, setTeams] = useState<TeamDto[]>([])
  const [loading, setLoading] = useState(true)
  const [searchQuery, setSearchQuery] = useState("")
  const [searchField, setSearchField] = useState<"name" | "all">("all")
  const [joinLoading, setJoinLoading] = useState<number | null>(null)
  const [showRegisterDialog, setShowRegisterDialog] = useState(false)
  const [selectedTeamId, setSelectedTeamId] = useState<number | null>(null)
  
  // 用户已加入的团队ID列表
  const [joinedTeamIds, setJoinedTeamIds] = useState<Set<number>>(new Set())
  
  // 退出团队状态
  const [leaveLoading, setLeaveLoading] = useState<number | null>(null)
  const [leaveDialogOpen, setLeaveDialogOpen] = useState(false)
  const [teamToLeave, setTeamToLeave] = useState<TeamDto | null>(null)
  
  // 查看成员状态
  const [viewingTeam, setViewingTeam] = useState<TeamDto | null>(null)
  const [teamMembers, setTeamMembers] = useState<UserDto[]>([])
  const [membersLoading, setMembersLoading] = useState(false)
  const [membersDialogOpen, setMembersDialogOpen] = useState(false)

  useEffect(() => {
    loadTeams()
    if (isAuthenticated && user?.id) {
      loadUserJoinedTeams()
    }
  }, [isAuthenticated, user?.id])

  const loadTeams = async () => {
    try {
      setLoading(true)
      const teamsData = await teamApi.getAll()

      // 按創立時間排序，最新的在前
      const sortedTeams = [...teamsData].sort((a, b) => {
        if (!a.createdAt && !b.createdAt) return 0
        if (!a.createdAt) return 1
        if (!b.createdAt) return -1
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      })

      setTeams(sortedTeams)
    } catch (error) {
      console.error("加載團隊列表失敗:", error)
      toast.error("加載團隊列表失敗，請稍後重試")
    } finally {
      setLoading(false)
    }
  }

  // 加载用户已加入的团队
  const loadUserJoinedTeams = async () => {
    if (!user?.id) return

    try {
      const userTeams = await userTeamApi.getUserTeams(user.id)
      const teamIds = new Set(userTeams.map((ut: any) => ut.teamId))
      setJoinedTeamIds(teamIds)
    } catch (error) {
      console.error("加載用戶已加入團隊失敗:", error)
      // 不显示错误提示，因为这不是关键功能
    }
  }

  // 搜尋和過濾邏輯
  const filteredTeams = useMemo(() => {
    if (!searchQuery.trim()) {
      return teams
    }

    const query = searchQuery.toLowerCase().trim()

    return teams.filter(team => {
      if (searchField === "name") {
        return team.name.toLowerCase().includes(query)
      } else {
        // 搜索所有字段
        return (
          team.name.toLowerCase().includes(query) ||
          team.id.toString().includes(query) ||
          (team.color && team.color.toLowerCase().includes(query))
        )
      }
    })
  }, [teams, searchQuery, searchField])

  // 計算簡單統計數據（基於過濾後的結果）
  const stats = useMemo(() => {
    const activeTeams = filteredTeams.filter(t => t.isActive !== false).length
    const totalPlayers = filteredTeams.reduce((sum, t) => sum + (t.currentPlayerCount || 0), 0)
    const totalCourts = filteredTeams.reduce((sum, t) => sum + (t.currentCourtCount || 0), 0)

    return {
      activeTeams,
      totalPlayers,
      totalCourts,
    }
  }, [filteredTeams])

  // 加入团队
  const handleJoinTeam = async (teamId: number) => {
    // 如果未登录，显示注册对话框
    if (!isAuthenticated) {
      setSelectedTeamId(teamId)
      setShowRegisterDialog(true)
      return
    }

    try {
      setJoinLoading(teamId)
      await userTeamApi.currentUserJoinTeam(teamId)
      toast.success("成功加入團隊！")
      // 更新已加入团队列表
      setJoinedTeamIds(prev => new Set([...prev, teamId]))
      // 重新加载团队列表以更新数据
      await loadTeams()
    } catch (error: any) {
      console.error("加入團隊失敗:", error)
      // 如果错误是"用户已经在团队中"，更新已加入列表
      if (error.message?.includes("已经在团队中") || error.message?.includes("already")) {
        setJoinedTeamIds(prev => new Set([...prev, teamId]))
      }
      toast.error(error.message || "加入團隊失敗，請稍後重試")
    } finally {
      setJoinLoading(null)
    }
  }

  // 注册成功后的回调
  const handleRegisterSuccess = () => {
    setShowRegisterDialog(false)
    // 延迟一下让AuthContext状态更新完成
    setTimeout(() => {
      if (selectedTeamId !== null) {
        handleJoinTeam(selectedTeamId)
      }
      router.refresh()
    }, 300)
  }

  // 退出团队
  const handleLeaveTeam = async (team: TeamDto) => {
    setTeamToLeave(team)
    setLeaveDialogOpen(true)
  }

  // 确认退出团队
  const confirmLeaveTeam = async () => {
    if (!teamToLeave) return

    try {
      setLeaveLoading(teamToLeave.id)
      await userTeamApi.currentUserLeaveTeam(teamToLeave.id)
      toast.success("已成功退出團隊")
      // 更新已加入团队列表
      setJoinedTeamIds(prev => {
        const newSet = new Set(prev)
        newSet.delete(teamToLeave.id)
        return newSet
      })
      // 重新加载团队列表以更新数据
      await loadTeams()
      setLeaveDialogOpen(false)
      setTeamToLeave(null)
    } catch (error: any) {
      console.error("退出團隊失敗:", error)
      toast.error(error.message || "退出團隊失敗，請稍後重試")
    } finally {
      setLeaveLoading(null)
    }
  }

  // 查看团队成员
  const handleViewTeamMembers = async (team: TeamDto) => {
    setViewingTeam(team)
    setMembersLoading(true)
    setMembersDialogOpen(true)

    try {
      // 通过api/users获取所有用户
      const allUsers = await userApi.getAll()

      // 如果团队有playerIds，使用这些ID来过滤用户
      // 否则显示所有活跃用户作为临时解决方案
      let teamMembers: UserDto[]
      if (team.playerIds && team.playerIds.length > 0) {
        teamMembers = allUsers.filter(user => team.playerIds!.includes(user.id))
      } else {
        // 如果没有playerIds，显示所有活跃用户作为临时解决方案
        teamMembers = allUsers.filter(user => user.isActive !== false)
      }

      setTeamMembers(teamMembers)
    } catch (error) {
      console.error("加載團隊成員失敗:", error)
      toast.error("加載團隊成員失敗")
      setTeamMembers([])
    } finally {
      setMembersLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="w-full min-h-[400px] flex items-center justify-center">
        <div className="flex flex-col items-center gap-4">
          <Spinner className="w-8 h-8 text-blue-600 dark:text-primary" />
          <p className="text-muted-foreground font-medium">正在載入團隊資訊...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* 頁面標題 - 緊湊版 */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-blue-900 dark:text-blue-100">
            團隊總覽
          </h2>
          <p className="text-sm text-muted-foreground mt-1">
            查看所有活躍團隊的資訊和統計
          </p>
        </div>
        <div className="text-right">
          <p className="text-xs text-muted-foreground">
            共 {teams.length} 個活躍團隊
          </p>
        </div>
      </div>

      {/* 簡要統計 - 緊湊版 */}
      <div className="grid grid-cols-3 gap-4">
        <div className="bg-blue-50 dark:bg-blue-950/20 rounded-lg p-4 border border-blue-100 dark:border-blue-900/30">
          <div className="flex items-center gap-3">
            <Activity className="h-5 w-5 text-blue-600 dark:text-blue-400" />
            <div>
              <p className="text-2xl font-bold text-blue-900 dark:text-blue-100">{stats.activeTeams}</p>
              <p className="text-sm text-blue-700 dark:text-blue-300">活躍團隊</p>
            </div>
          </div>
        </div>

        <div className="bg-green-50 dark:bg-green-950/20 rounded-lg p-4 border border-green-100 dark:border-green-900/30">
          <div className="flex items-center gap-3">
            <Users className="h-5 w-5 text-green-600 dark:text-green-400" />
            <div>
              <p className="text-2xl font-bold text-green-900 dark:text-green-100">{stats.totalPlayers}</p>
              <p className="text-sm text-green-700 dark:text-green-300">總人數</p>
            </div>
          </div>
        </div>

        <div className="bg-purple-50 dark:bg-purple-950/20 rounded-lg p-4 border border-purple-100 dark:border-purple-900/30">
          <div className="flex items-center gap-3">
            <MapPin className="h-5 w-5 text-purple-600 dark:text-purple-400" />
            <div>
              <p className="text-2xl font-bold text-purple-900 dark:text-purple-100">{stats.totalCourts}</p>
              <p className="text-sm text-purple-700 dark:text-purple-300">使用場地</p>
            </div>
          </div>
        </div>
      </div>

      {/* 搜索區域 - 緊湊版 */}
      <div className="bg-gradient-to-r from-blue-50/30 to-white dark:from-blue-950/10 dark:to-card/50 rounded-xl border border-blue-100 dark:border-border/50 p-4 shadow-sm">
        <div className="flex flex-col sm:flex-row gap-3">
          {/* 搜索輸入框 */}
          <div className="relative flex-1 max-w-md">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-blue-500 dark:text-blue-400 h-4 w-4" />
            <Input
              placeholder="搜尋團隊..."
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

          {/* 搜索類型切換 */}
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
              variant={searchField === "name" ? "default" : "outline"}
              size="sm"
              onClick={() => setSearchField("name")}
              className={`h-9 px-3 text-xs font-medium transition-all duration-200 ${
                searchField === "name"
                  ? "bg-blue-600 hover:bg-blue-700 text-white shadow-md"
                  : "hover:bg-blue-50 dark:hover:bg-blue-950/50 border-blue-200 dark:border-border/50"
              }`}
            >
              名稱
            </Button>
          </div>

          {/* 搜索結果統計 */}
          {searchQuery && (
            <div className="flex items-center gap-2 sm:ml-auto">
              <span className="text-xs font-medium text-blue-700 dark:text-blue-400">
                找到 {filteredTeams.length} 個團隊
              </span>
              <Badge variant="secondary" className="text-xs px-2 py-0.5 h-5">
                "{searchQuery}"
              </Badge>
            </div>
          )}
        </div>

        {/* 搜索狀態提示 */}
        {searchQuery && filteredTeams.length === 0 && (
          <div className="mt-3 p-3 bg-amber-50 dark:bg-amber-950/20 border border-amber-200 dark:border-amber-800/30 rounded-lg">
            <p className="text-xs text-amber-700 dark:text-amber-400 flex items-center gap-2">
              <Search className="h-3 w-3" />
              沒有找到包含 "{searchQuery}" 的團隊
            </p>
          </div>
        )}
      </div>

      {/* 團隊展示區域 */}
          {filteredTeams.length === 0 ? (
        <Card className="border-2 border-blue-100 dark:border-border/50 bg-gradient-to-br from-blue-50/50 to-white dark:from-card/30 dark:to-card/50">
          <CardContent className="py-16">
            <div className="text-center space-y-4">
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-blue-100 dark:bg-blue-900/20 mb-4">
                <Users className="h-8 w-8 text-blue-500 dark:text-blue-400" />
              </div>
              <div>
                <h3 className="text-xl font-semibold text-foreground mb-2">
                  {searchQuery ? "沒有找到符合條件的團隊" : "目前沒有任何團隊"}
                </h3>
                <p className="text-muted-foreground">
                  {searchQuery ? "請嘗試調整搜尋條件" : "請聯繫管理員創建團隊"}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      ) : (
        <Card className="border-blue-100 dark:border-border/50">
          <CardHeader>
            <CardTitle className="flex items-center justify-between">
              <span>團隊列表</span>
              <Badge variant="outline" className="text-blue-600 border-blue-200">
                {searchQuery ? `共 ${filteredTeams.length} 個團隊` : `共 ${teams.length} 個團隊`}
              </Badge>
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow className="border-blue-100 dark:border-border/50">
                    <TableHead className="font-semibold text-blue-900 dark:text-blue-100">團隊名稱</TableHead>
                    <TableHead className="font-semibold text-blue-900 dark:text-blue-100 text-center">狀態</TableHead>
                    <TableHead className="font-semibold text-blue-900 dark:text-blue-100 text-center">等級</TableHead>
                    <TableHead className="font-semibold text-blue-900 dark:text-blue-100 text-center">隊員</TableHead>
                    <TableHead className="font-semibold text-blue-900 dark:text-blue-100 text-center">使用率</TableHead>
                    <TableHead className="font-semibold text-blue-900 dark:text-blue-100 text-center">場地</TableHead>
                    <TableHead className="font-semibold text-blue-900 dark:text-blue-100 text-center">創建時間</TableHead>
                    <TableHead className="font-semibold text-blue-900 dark:text-blue-100 text-center">操作</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredTeams.map((team) => {
                    const playerUsage = team.maxPlayers > 0
                      ? ((team.currentPlayerCount || 0) / team.maxPlayers) * 100
                      : 0
                    const isJoined = isAuthenticated && user?.id && joinedTeamIds.has(team.id)

                    return (
                      <TableRow key={team.id} className="border-blue-50 dark:border-border/30 hover:bg-blue-50/30 dark:hover:bg-blue-950/10 transition-colors">
                        <TableCell className="font-medium">
                          <div className="flex items-center gap-3">
                            {team.color && (
                              <div
                                className={`w-4 h-4 rounded-full border border-white shadow-sm ${
                                  team.color.startsWith('#')
                                    ? ''
                                    : team.color.startsWith('bg-')
                                      ? team.color
                                      : `bg-${team.color}-500`
                                }`}
                                style={team.color.startsWith('#') ? { backgroundColor: team.color } : {}}
                              />
                            )}
                            <div className="flex items-center gap-2">
                              <button
                                onClick={() => handleViewTeamMembers(team)}
                                className="text-left text-foreground hover:text-green-600 dark:hover:text-green-400 transition-colors hover:underline"
                              >
                                {team.name}
                              </button>
                              {isJoined && (
                                <Badge className="bg-green-100 text-green-700 border-green-200 dark:bg-green-500/20 dark:text-green-300 dark:border-green-500/30 text-xs">
                                  <CheckCircle2 className="w-3 h-3 mr-1" />
                                  已加入
                                </Badge>
                              )}
                            </div>
                          </div>
                        </TableCell>
                        <TableCell className="text-center">
                          {team.isActive !== false ? (
                            <Badge className="bg-green-100 text-green-700 border-green-200 dark:bg-green-500/20 dark:text-green-300 dark:border-green-500/30">
                              <Activity className="w-3 h-3 mr-1" />
                              活躍
                            </Badge>
                          ) : (
                            <Badge variant="secondary">非活躍</Badge>
                          )}
                        </TableCell>
                        <TableCell className="text-center">
                          {team.level ? (
                            <Badge variant="outline" className="text-blue-600 border-blue-200">
                              {team.level}
                            </Badge>
                          ) : (
                            <span className="text-muted-foreground text-sm">-</span>
                          )}
                        </TableCell>
                        <TableCell className="text-center font-medium">
                          <div className="flex flex-col items-center gap-1">
                            <span className="text-sm">{team.currentPlayerCount || 0} / {team.maxPlayers || 0}</span>
                          </div>
                        </TableCell>
                        <TableCell className="text-center">
                          <div className="flex flex-col items-center gap-1">
                            <Progress value={playerUsage} className="w-16 h-2" />
                            <span className={`text-xs font-semibold ${
                              playerUsage >= 90 ? "text-red-600 dark:text-red-400" :
                              playerUsage >= 70 ? "text-orange-600 dark:text-orange-400" :
                              "text-green-600 dark:text-green-400"
                            }`}>
                              {Math.round(playerUsage)}%
                            </span>
                          </div>
                        </TableCell>
                        <TableCell className="text-center font-medium">
                          <span className="text-sm">{team.courtCount || 0}</span>
                        </TableCell>
                        <TableCell className="text-center text-sm text-muted-foreground">
                          {team.createdAt ? (
                            <div className="flex flex-col gap-1">
                              <span>{format(new Date(team.createdAt), "MM/dd", { locale: zhCN })}</span>
                              <span className="text-xs">
                                {formatDistanceToNow(new Date(team.createdAt), {
                                  addSuffix: true,
                                  locale: zhCN
                                })}
                              </span>
                            </div>
                          ) : (
                            <span>-</span>
                          )}
                        </TableCell>
                        <TableCell className="text-center">
                          <div className="flex items-center justify-center gap-2">
                            <Tooltip>
                              <TooltipTrigger asChild>
                                <Button
                                  size="icon"
                                  variant="ghost"
                                  onClick={() => handleViewTeamMembers(team)}
                                  className="h-8 w-8 rounded-full hover:bg-green-50 dark:hover:bg-green-950/50 hover:text-green-600 dark:hover:text-green-400 transition-all duration-200"
                                >
                                  <Eye className="h-4 w-4 text-green-600 dark:text-green-400" />
                                </Button>
                              </TooltipTrigger>
                              <TooltipContent side="top">
                                <p className="text-xs">查看成員</p>
                              </TooltipContent>
                            </Tooltip>
                            {isJoined ? (
                              <Tooltip>
                                <TooltipTrigger asChild>
                                  <Button
                                    size="icon"
                                    variant="ghost"
                                    onClick={() => handleLeaveTeam(team)}
                                    disabled={leaveLoading === team.id}
                                    className="h-8 w-8 rounded-full hover:bg-red-50 dark:hover:bg-red-950/50 hover:text-red-600 dark:hover:text-red-400 transition-all duration-200 disabled:opacity-40 disabled:cursor-not-allowed"
                                  >
                                    {leaveLoading === team.id ? (
                                      <Spinner className="h-4 w-4 text-red-600 dark:text-red-400" />
                                    ) : (
                                      <LogOut className="h-4 w-4 text-red-600 dark:text-red-400" />
                                    )}
                                  </Button>
                                </TooltipTrigger>
                                <TooltipContent side="top">
                                  <p className="text-xs">
                                    {leaveLoading === team.id ? "退出中..." : "退出團隊"}
                                  </p>
                                </TooltipContent>
                              </Tooltip>
                            ) : (
                              <Tooltip>
                                <TooltipTrigger asChild>
                                  <Button
                                    size="icon"
                                    variant="ghost"
                                    onClick={() => handleJoinTeam(team.id)}
                                    disabled={joinLoading === team.id || !team.isActive}
                                    className="h-8 w-8 rounded-full hover:bg-blue-50 dark:hover:bg-blue-950/50 hover:text-blue-600 dark:hover:text-blue-400 transition-all duration-200 disabled:opacity-40 disabled:cursor-not-allowed disabled:hover:bg-transparent"
                                  >
                                    {joinLoading === team.id ? (
                                      <Spinner className="h-4 w-4 text-blue-600 dark:text-blue-400" />
                                    ) : (
                                      <UserPlus className={`h-4 w-4 ${!team.isActive ? "text-muted-foreground" : "text-blue-600 dark:text-blue-400"}`} />
                                    )}
                                  </Button>
                                </TooltipTrigger>
                                <TooltipContent side="top">
                                  <p className="text-xs">
                                    {joinLoading === team.id
                                      ? "加入中..."
                                      : !team.isActive
                                      ? "團隊非活躍，無法加入"
                                      : isAuthenticated
                                      ? "點擊加入團隊"
                                      : "登錄後即可加入團隊"}
                                  </p>
                                </TooltipContent>
                              </Tooltip>
                            )}
                          </div>
                        </TableCell>
                      </TableRow>
                    )
                  })}
                </TableBody>
              </Table>
            </div>
          </CardContent>
        </Card>
      )}

      {/* 查看团队成员对话框 */}
      <Dialog open={membersDialogOpen} onOpenChange={setMembersDialogOpen}>
        <DialogContent className="sm:max-w-[800px]">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-3">
              {viewingTeam && (
                <>
                  {viewingTeam.color && (
                    <div
                      className={`w-5 h-5 rounded-full border border-white shadow-sm ${
                        viewingTeam.color.startsWith('#')
                          ? ''
                          : viewingTeam.color.startsWith('bg-')
                            ? viewingTeam.color
                            : `bg-${viewingTeam.color}-500`
                      }`}
                      style={viewingTeam.color.startsWith('#') ? { backgroundColor: viewingTeam.color } : {}}
                    />
                  )}
                  {viewingTeam.name} - 團隊成員
                </>
              )}
            </DialogTitle>
            <DialogDescription>
              查看該團隊的所有成員資訊
            </DialogDescription>
          </DialogHeader>

          <div className="py-4">
            {membersLoading ? (
              <div className="flex items-center justify-center py-8">
                <Spinner className="w-6 h-6 text-blue-600 dark:text-primary" />
                <span className="ml-2 text-muted-foreground">載入中...</span>
              </div>
            ) : teamMembers.length === 0 ? (
              <div className="text-center py-8">
                <div className="inline-flex items-center justify-center w-12 h-12 rounded-full bg-gray-100 dark:bg-gray-800 mb-4">
                  <Users className="h-6 w-6 text-gray-400" />
                </div>
                <p className="text-muted-foreground">該團隊目前沒有成員</p>
              </div>
            ) : (
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <h4 className="font-medium">成員列表 ({teamMembers.length} 人)</h4>
                </div>
                <div className="grid gap-2 max-h-80 overflow-y-auto">
                  {teamMembers.map((member) => (
                    <div
                      key={member.id}
                      className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-800/50 rounded-lg border border-gray-200 dark:border-gray-700"
                    >
                      <div className="flex items-center gap-3">
                        <Avatar className="h-8 w-8 rounded-full">
                          <AvatarFallback className="bg-blue-100 dark:bg-blue-900 text-blue-700 dark:text-blue-300">
                            {(member.realName || member.username).charAt(0).toUpperCase()}
                          </AvatarFallback>
                        </Avatar>
                        <div>
                          <p className="font-medium text-foreground">
                            {member.realName || member.username}
                          </p>
                          <div className="flex items-center gap-2 text-xs text-muted-foreground">
                            {member.email && <span>{member.email}</span>}
                            {member.phoneNumber && <span>• {member.phoneNumber}</span>}
                          </div>
                        </div>
                      </div>
                      <div className="flex items-start gap-3">
                        <div className="flex-1 text-right">
                          <p className="text-xs text-muted-foreground">
                            用戶名: {member.username}
                          </p>
                          {member.roleNames && member.roleNames.length > 0 && (
                            <div className="flex flex-wrap gap-1 justify-end mt-1">
                              {member.roleNames.slice(0, 2).map((role) => (
                                <Badge
                                  key={role}
                                  variant="secondary"
                                  className="text-xs px-2 py-0.5 bg-blue-100 dark:bg-blue-900 text-blue-700 dark:text-blue-300"
                                >
                                  {role}
                                </Badge>
                              ))}
                            </div>
                          )}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>

          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setMembersDialogOpen(false)}
            >
              關閉
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* 注册对话框 */}
      <Dialog open={showRegisterDialog} onOpenChange={setShowRegisterDialog}>
        <DialogContent className="max-w-lg max-h-[90vh] overflow-y-auto sm:max-w-[600px]">
          <DialogHeader>
            <DialogTitle>加入團隊</DialogTitle>
            <DialogDescription>
              請先註冊帳號以加入團隊並使用系統功能
            </DialogDescription>
          </DialogHeader>
          <RegisterForm
            onSuccess={handleRegisterSuccess}
            onCancel={() => setShowRegisterDialog(false)}
          />
        </DialogContent>
      </Dialog>

      {/* 退出团队确认对话框 */}
      <AlertDialog open={leaveDialogOpen} onOpenChange={setLeaveDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>確認退出團隊</AlertDialogTitle>
            <AlertDialogDescription>
              您確定要退出「{teamToLeave?.name}」團隊嗎？
              <br />
              <span className="text-red-600 dark:text-red-400 font-medium mt-2 block">
                此操作無法撤銷，退出後您將無法再訪問該團隊的相關功能。
              </span>
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={leaveLoading !== null}>
              取消
            </AlertDialogCancel>
            <AlertDialogAction
              onClick={confirmLeaveTeam}
              disabled={leaveLoading !== null}
              className="bg-red-600 hover:bg-red-700 focus:ring-red-600"
            >
              {leaveLoading !== null ? (
                <>
                  <Spinner className="h-4 w-4 mr-2" />
                  退出中...
                </>
              ) : (
                <>
                  <LogOut className="h-4 w-4 mr-2" />
                  確認退出
                </>
              )}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}
