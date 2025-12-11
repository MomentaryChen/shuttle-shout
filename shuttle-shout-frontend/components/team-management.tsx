"use client"

import { useEffect, useState, useMemo } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Progress } from "@/components/ui/progress"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { Button } from "@/components/ui/button"
import { Label } from "@/components/ui/label"
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip"
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "@/components/ui/collapsible"
import { teamApi, userApi, userTeamApi } from "@/lib/api"
import { TeamDto, UserDto, TeamLevel } from "@/types/api"
import { Spinner } from "@/components/ui/spinner"
import { toast } from "sonner"
import { format, formatDistanceToNow } from "date-fns"
import { zhCN } from "date-fns/locale/zh-CN"
import { useAuth } from "@/contexts/AuthContext"
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
  Plus,
  Trash2,
  Edit,
  Eye,
  ChevronDown,
  ChevronUp
} from "lucide-react"

const COLORS = [
  "bg-blue-500",
  "bg-red-500",
  "bg-green-500",
  "bg-yellow-500",
  "bg-purple-500",
  "bg-pink-500",
  "bg-indigo-500",
  "bg-cyan-500",
]

// 颜色映射：将Tailwind类名转换为CSS颜色值
const COLOR_MAP: Record<string, string> = {
  "bg-blue-500": "#3b82f6",
  "bg-red-500": "#ef4444",
  "bg-green-500": "#22c55e",
  "bg-yellow-500": "#eab308",
  "bg-purple-500": "#a855f7",
  "bg-pink-500": "#ec4899",
  "bg-indigo-500": "#6366f1",
  "bg-cyan-500": "#06b6d4",
}

// 球队等级选项
const TEAM_LEVELS: TeamLevel[] = ["新手", "初階", "中等", "強", "超強", "世界強"]

export function TeamManagement() {
  const { user } = useAuth()
  const [teams, setTeams] = useState<TeamDto[]>([])
  const [loading, setLoading] = useState(true)
  const [searchQuery, setSearchQuery] = useState("")
  const [searchField, setSearchField] = useState<"name" | "all">("all")

  // 球队管理状态
  const [newTeamName, setNewTeamName] = useState("")
  const [newTeamDescription, setNewTeamDescription] = useState("")
  const [selectedColor, setSelectedColor] = useState(COLORS[0])
  const [selectedLevel, setSelectedLevel] = useState<TeamLevel>("中等")
  const [maxPlayers, setMaxPlayers] = useState(20)
  const [courtCount, setCourtCount] = useState(2)
  const [isAdding, setIsAdding] = useState(false)
  const [deletingIds, setDeletingIds] = useState<Set<string>>(new Set())

  // 编辑状态
  const [editingTeam, setEditingTeam] = useState<TeamDto | null>(null)
  const [editTeamName, setEditTeamName] = useState("")
  const [editTeamDescription, setEditTeamDescription] = useState("")
  const [editSelectedColor, setEditSelectedColor] = useState(COLORS[0])
  const [editSelectedLevel, setEditSelectedLevel] = useState<TeamLevel>("中等")
  const [editMaxPlayers, setEditMaxPlayers] = useState(20)
  const [editCourtCount, setEditCourtCount] = useState(2)
  const [isUpdating, setIsUpdating] = useState(false)
  const [editDialogOpen, setEditDialogOpen] = useState(false)

  // 查看成员状态
  const [viewingTeam, setViewingTeam] = useState<TeamDto | null>(null)
  const [teamMembers, setTeamMembers] = useState<UserDto[]>([])
  const [membersLoading, setMembersLoading] = useState(false)
  const [membersDialogOpen, setMembersDialogOpen] = useState(false)

  // 加入成员状态
  const [allUsers, setAllUsers] = useState<UserDto[]>([])
  const [addMemberDialogOpen, setAddMemberDialogOpen] = useState(false)
  const [addingMember, setAddingMember] = useState(false)

  // 新增球队区块折叠状态
  const [isAddTeamOpen, setIsAddTeamOpen] = useState(false)

  useEffect(() => {
    loadTeams()
  }, [])

  const loadTeams = async () => {
    try {
      setLoading(true)
      // 使用新的API端点，后端会根据JWT token返回当前用户创建的球队
      const teamsData = await teamApi.getMyTeams()

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

  // 搜索和过滤逻辑
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
          (team.color && team.color.toLowerCase().includes(query)) ||
          (team.description && team.description.toLowerCase().includes(query))
        )
      }
    })
  }, [teams, searchQuery, searchField])

  // 计算简单统计数据（基于过滤后的结果）
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

  // 球队管理功能
  const handleAddTeam = async () => {
    if (!newTeamName.trim()) return

    try {
      setIsAdding(true)
      const createdTeam = await teamApi.create({
        name: newTeamName.trim(),
        description: newTeamDescription.trim() || undefined,
        userId: user?.id,
        color: selectedColor,
        level: selectedLevel,
        maxPlayers: maxPlayers,
        courtCount: courtCount,
        isActive: true,
      })

      const newTeam = {
        id: createdTeam.id,
        name: createdTeam.name,
        description: createdTeam.description,
        color: createdTeam.color || selectedColor,
        maxPlayers: createdTeam.maxPlayers,
        courtCount: createdTeam.courtCount,
        isActive: createdTeam.isActive,
        currentPlayerCount: createdTeam.currentPlayerCount || 0,
        currentCourtCount: createdTeam.currentCourtCount || 0,
        createdAt: createdTeam.createdAt,
        updatedAt: createdTeam.updatedAt,
        userId: user?.id
      }

      setTeams(prev => [newTeam, ...prev])
      setNewTeamName("")
      setNewTeamDescription("")
      setSelectedLevel("中等")
      setMaxPlayers(20)
      setCourtCount(2)
      toast.success("球隊添加成功")
    } catch (error) {
      console.error("添加球隊失敗:", error)
      toast.error("添加球隊失敗，請重試")
    } finally {
      setIsAdding(false)
    }
  }

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
      setAllUsers(allUsers) // 保存所有用户用于加入成员功能
    } catch (error) {
      console.error("加載團隊成員失敗:", error)
      toast.error("加載團隊成員失敗")
      setTeamMembers([])
    } finally {
      setMembersLoading(false)
    }
  }

  const handleAddMember = async (userId: number) => {
    if (!viewingTeam) return

    try {
      setAddingMember(true)
      await userTeamApi.joinTeam({
        userId: userId,
        teamId: viewingTeam.id,
        isOwner: false
      })

      // 重新加载团队成员
      await handleViewTeamMembers(viewingTeam)
      toast.success("成員加入成功")
    } catch (error: any) {
      console.error("加入成員失敗:", error)
      toast.error(error.message || "加入成員失敗")
    } finally {
      setAddingMember(false)
      setAddMemberDialogOpen(false)
    }
  }

  const handleRemoveMember = async (userId: number) => {
    if (!viewingTeam) return

    if (!confirm("確定要將此成員移出團隊嗎？")) return

    try {
      await userTeamApi.removeMember(userId, viewingTeam.id)

      // 重新加载团队成员
      await handleViewTeamMembers(viewingTeam)
      toast.success("成員移出成功")
    } catch (error: any) {
      console.error("移出成員失敗:", error)
      toast.error(error.message || "移出成員失敗")
    }
  }

  const handleEditTeam = (team: TeamDto) => {
    setEditingTeam(team)
    setEditTeamName(team.name)
    setEditTeamDescription(team.description || "")
    setEditSelectedColor(team.color || COLORS[0])
    setEditSelectedLevel(team.level || "中等")
    setEditMaxPlayers(team.maxPlayers)
    setEditCourtCount(team.courtCount)
    setEditDialogOpen(true)
  }

  const handleUpdateTeam = async () => {
    if (!editingTeam || !editTeamName.trim()) return

    try {
      setIsUpdating(true)
      const updatedTeam = await teamApi.update(editingTeam.id, {
        name: editTeamName.trim(),
        description: editTeamDescription.trim() || undefined,
        color: editSelectedColor,
        level: editSelectedLevel,
        maxPlayers: editMaxPlayers,
        courtCount: editCourtCount,
      })

      setTeams(prev => prev.map(t =>
        t.id === editingTeam.id
          ? { ...t, ...updatedTeam }
          : t
      ))
      setEditDialogOpen(false)
      setEditingTeam(null)
      toast.success("球隊更新成功")
    } catch (error: any) {
      console.error("更新球隊失敗:", error)
      toast.error(error.message || "更新球隊失敗，請重試")
    } finally {
      setIsUpdating(false)
    }
  }

  const handleDeleteTeam = async (id: string) => {
    if (!confirm("確定要刪除此球隊嗎？刪除後相關的球員和場地也會受到影響。")) return

    try {
      setDeletingIds((prev) => new Set(prev).add(id))
      const teamId = parseInt(id)
      await teamApi.delete(teamId)
      setTeams(prev => prev.filter(t => t.id !== teamId))
      toast.success("球隊刪除成功")
    } catch (error: any) {
      console.error("刪除球隊失敗:", error)
      toast.error(error.message || "刪除球隊失敗，請重試")
    } finally {
      setDeletingIds((prev) => {
        const newSet = new Set(prev)
        newSet.delete(id)
        return newSet
      })
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
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* 页面标题 */}
      <div className="flex flex-col gap-2">
        <h2 className="text-3xl font-bold bg-gradient-to-r from-blue-600 to-blue-500 bg-clip-text text-transparent">
          球隊管理
        </h2>
        <p className="text-muted-foreground">
          查看和管理您創建的所有團隊的統計資訊和使用狀況
        </p>
      </div>

      {/* 简要统计 - 紧凑版 */}
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

      {/* 搜索区域 - 紧凑版 */}
      <div className="bg-gradient-to-r from-blue-50/30 to-white dark:from-blue-950/10 dark:to-card/50 rounded-xl border border-blue-100 dark:border-border/50 p-4 shadow-sm">
        <div className="flex flex-col sm:flex-row gap-3">
          {/* 搜索输入框 */}
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

          {/* 搜索结果统计 */}
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

        {/* 搜索状态提示 */}
        {searchQuery && filteredTeams.length === 0 && (
          <div className="mt-3 p-3 bg-amber-50 dark:bg-amber-950/20 border border-amber-200 dark:border-amber-800/30 rounded-lg">
            <p className="text-xs text-amber-700 dark:text-amber-400 flex items-center gap-2">
              <Search className="h-3 w-3" />
              沒有找到包含 "{searchQuery}" 的團隊
            </p>
          </div>
        )}
      </div>

      {/* 添加球队区域 */}
      <Collapsible open={isAddTeamOpen} onOpenChange={setIsAddTeamOpen}>
        <Card className="bg-card border-border">
          <CollapsibleTrigger asChild>
            <CardHeader className="cursor-pointer hover:bg-muted/50 transition-colors">
              <CardTitle className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Plus className="h-5 w-5" />
                  新增球隊
                </div>
                {isAddTeamOpen ? (
                  <ChevronUp className="h-5 w-5 text-muted-foreground" />
                ) : (
                  <ChevronDown className="h-5 w-5 text-muted-foreground" />
                )}
              </CardTitle>
            </CardHeader>
          </CollapsibleTrigger>
          <CollapsibleContent>
            <CardContent className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* 球队信息 */}
            <div className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="teamName">球隊名稱</Label>
                <Input
                  id="teamName"
                  placeholder="輸入球隊名稱"
                  value={newTeamName}
                  onChange={(e) => setNewTeamName(e.target.value)}
                  className="bg-input text-foreground border-border placeholder:text-muted-foreground"
                  onKeyPress={(e) => e.key === "Enter" && handleAddTeam()}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="teamDescription">球隊描述</Label>
                <Textarea
                  id="teamDescription"
                  placeholder="輸入球隊描述（可選，最多255字）"
                  value={newTeamDescription}
                  onChange={(e) => setNewTeamDescription(e.target.value)}
                  className="bg-input text-foreground border-border placeholder:text-muted-foreground resize-none"
                  maxLength={255}
                  rows={4}
                />
                <div className="text-xs text-muted-foreground text-right">
                  {newTeamDescription.length}/255
                </div>
              </div>

              <div className="space-y-2">
                <Label>球隊顏色</Label>
                <div className="flex gap-2 flex-wrap">
                  {COLORS.map((color) => (
                    <button
                      key={color}
                      className={`w-8 h-8 rounded-full ${color} cursor-pointer ${
                        selectedColor === color ? "ring-2 ring-offset-2 ring-offset-card ring-foreground" : ""
                      }`}
                      onClick={() => setSelectedColor(color)}
                    />
                  ))}
                </div>
              </div>
            </div>

            {/* 配置信息 */}
            <div className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="maxPlayers">最大人數</Label>
                <Input
                  id="maxPlayers"
                  type="number"
                  min="1"
                  value={maxPlayers}
                  onChange={(e) => setMaxPlayers(parseInt(e.target.value) || 1)}
                  className="bg-input text-foreground border-border"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="courtCount">場地數量</Label>
                <Input
                  id="courtCount"
                  type="number"
                  min="1"
                  value={courtCount}
                  onChange={(e) => setCourtCount(parseInt(e.target.value) || 1)}
                  className="bg-input text-foreground border-border"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="level">球隊等級</Label>
                <Select value={selectedLevel} onValueChange={(value) => setSelectedLevel(value as TeamLevel)}>
                  <SelectTrigger id="level" className="w-full bg-input text-foreground border-border">
                    <SelectValue placeholder="選擇球隊等級" />
                  </SelectTrigger>
                  <SelectContent>
                    {TEAM_LEVELS.map((level) => (
                      <SelectItem key={level} value={level}>
                        {level}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>

            <Button
              onClick={handleAddTeam}
              disabled={isAdding || !newTeamName.trim()}
              className="w-full bg-primary text-primary-foreground hover:bg-primary/90"
            >
              <Plus className="w-4 h-4 mr-2" />
              {isAdding ? "添加中..." : "新增球隊"}
            </Button>
          </div>
            </CardContent>
          </CollapsibleContent>
        </Card>
      </Collapsible>

      {/* 团队列表 - 表格形式 */}
      <Card className="border-blue-100 dark:border-border/50">
        <CardHeader>
          <CardTitle className="flex items-center justify-between">
            <span>球隊管理</span>
            <Badge variant="outline" className="text-blue-600 border-blue-200">
              {searchQuery ? `共 ${filteredTeams.length} 個團隊` : `共 ${teams.length} 個團隊`}
            </Badge>
          </CardTitle>
        </CardHeader>
        <CardContent>
          {filteredTeams.length === 0 ? (
            <div className="text-center py-16">
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-blue-100 dark:bg-blue-900/20 mb-4">
                <Users className="h-8 w-8 text-blue-500 dark:text-blue-400" />
              </div>
              <div>
                <h3 className="text-xl font-semibold text-foreground mb-2">
                  {searchQuery ? "沒有找到符合條件的團隊" : "您還沒有創建任何團隊"}
                </h3>
                <p className="text-muted-foreground">
                  {searchQuery ? "請嘗試調整搜尋條件" : "點擊上方「新增球隊」來創建您的第一個團隊"}
                </p>
              </div>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow className="border-blue-100 dark:border-border/50">
                    <TableHead className="font-semibold text-blue-900 dark:text-blue-100">團隊名稱</TableHead>
                    <TableHead className="font-semibold text-blue-900 dark:text-blue-100">描述</TableHead>
                    <TableHead className="font-semibold text-blue-900 dark:text-blue-100 text-center">狀態</TableHead>
                    <TableHead className="font-semibold text-blue-900 dark:text-blue-100 text-center">等級</TableHead>
                    <TableHead className="font-semibold text-blue-900 dark:text-blue-100 text-center">隊員</TableHead>
                    <TableHead className="font-semibold text-blue-900 dark:text-blue-100 text-center">隊員滿團率</TableHead>
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

                    return (
                      <TableRow key={team.id} className="border-blue-50 dark:border-border/30 hover:bg-blue-50/30 dark:hover:bg-blue-950/10 transition-colors">
                        <TableCell className="font-medium">
                          <div className="flex items-center gap-3">
                            {team.color && COLOR_MAP[team.color] && (
                              <div
                                className="w-4 h-4 rounded-full border border-white shadow-sm"
                                style={{ backgroundColor: COLOR_MAP[team.color] }}
                              />
                            )}
                            <button
                              onClick={() => handleViewTeamMembers(team)}
                              className="text-left text-foreground hover:text-blue-600 dark:hover:text-blue-400 transition-colors hover:underline"
                            >
                              {team.name}
                            </button>
                          </div>
                        </TableCell>
                        <TableCell className="max-w-xs">
                          <div className="text-sm text-muted-foreground truncate" title={team.description}>
                            {team.description || <span className="text-muted-foreground/50">-</span>}
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
                          <div className="flex flex-col items-center gap-1">
                            <span className="text-sm">{team.currentCourtCount || 0} / {team.courtCount || 0}</span>
                          </div>
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
                          <div className="flex items-center justify-center gap-1">
                            {/* 查看成员按钮 - 所有用户都可以查看 */}
                            <Tooltip>
                              <TooltipTrigger asChild>
                                <Button
                                  size="sm"
                                  variant="ghost"
                                  onClick={() => handleViewTeamMembers(team)}
                                  className="text-green-600 hover:bg-green-50 dark:hover:bg-green-950/50"
                                >
                                  <Eye className="w-4 h-4" />
                                </Button>
                              </TooltipTrigger>
                              <TooltipContent side="top">
                                <p className="text-xs">查看成員</p>
                              </TooltipContent>
                            </Tooltip>
                            {/* 只有团队所有者才能编辑和删除团队 */}
                            {team.userId === user?.id ? (
                              <>
                                <Tooltip>
                                  <TooltipTrigger asChild>
                                    <Button
                                      size="sm"
                                      variant="ghost"
                                      onClick={() => handleEditTeam(team)}
                                      className="text-blue-600 hover:bg-blue-50 dark:hover:bg-blue-950/50"
                                    >
                                      <Edit className="w-4 h-4" />
                                    </Button>
                                  </TooltipTrigger>
                                  <TooltipContent side="top">
                                    <p className="text-xs">編輯球隊</p>
                                  </TooltipContent>
                                </Tooltip>
                                <Tooltip>
                                  <TooltipTrigger asChild>
                                    <Button
                                      size="sm"
                                      variant="ghost"
                                      onClick={() => handleDeleteTeam(team.id.toString())}
                                      disabled={deletingIds.has(team.id.toString())}
                                      className="text-destructive hover:bg-destructive/10 hover:text-destructive"
                                    >
                                      <Trash2 className="w-4 h-4" />
                                    </Button>
                                  </TooltipTrigger>
                                  <TooltipContent side="top">
                                    <p className="text-xs">刪除球隊</p>
                                  </TooltipContent>
                                </Tooltip>
                              </>
                            ) : (
                              <span className="text-xs text-muted-foreground">非所有者</span>
                            )}
                          </div>
                        </TableCell>
                      </TableRow>
                    )
                  })}
                </TableBody>
              </Table>
            </div>
          )}
        </CardContent>
      </Card>

      {/* 编辑球队对话框 */}
      <Dialog open={editDialogOpen} onOpenChange={setEditDialogOpen}>
        <DialogContent className="sm:max-w-[600px]">
          <DialogHeader>
            <DialogTitle>編輯球隊</DialogTitle>
            <DialogDescription>
              修改球隊資訊，保存後將立即生效。
            </DialogDescription>
          </DialogHeader>

          <div className="grid gap-4 py-4">
            {/* 球队信息 */}
            <div className="grid gap-4">
              <div className="grid gap-2">
                <Label htmlFor="edit-teamName">球隊名稱</Label>
                <Input
                  id="edit-teamName"
                  value={editTeamName}
                  onChange={(e) => setEditTeamName(e.target.value)}
                  placeholder="輸入球隊名稱"
                  className="bg-input text-foreground border-border"
                />
              </div>

              <div className="grid gap-2">
                <Label htmlFor="edit-teamDescription">球隊描述</Label>
                <Textarea
                  id="edit-teamDescription"
                  value={editTeamDescription}
                  onChange={(e) => setEditTeamDescription(e.target.value)}
                  placeholder="輸入球隊描述（可選，最多255字）"
                  className="bg-input text-foreground border-border resize-none"
                  maxLength={255}
                  rows={4}
                />
                <div className="text-xs text-muted-foreground text-right">
                  {editTeamDescription.length}/255
                </div>
              </div>

              <div className="grid gap-2">
                <Label>球隊顏色</Label>
                <div className="flex gap-2 flex-wrap">
                  {COLORS.map((color) => (
                    <button
                      key={color}
                      className={`w-10 h-10 rounded-full ${color} cursor-pointer ${
                        editSelectedColor === color ? "ring-2 ring-offset-2 ring-offset-card ring-foreground" : ""
                      }`}
                      onClick={() => setEditSelectedColor(color)}
                    />
                  ))}
                </div>
              </div>

            </div>

            {/* 配置信息 */}
            <div className="grid gap-2">
              <Label htmlFor="edit-maxPlayers">最大人數</Label>
              <Input
                id="edit-maxPlayers"
                type="number"
                min="1"
                value={editMaxPlayers}
                onChange={(e) => setEditMaxPlayers(parseInt(e.target.value) || 1)}
                className="bg-input text-foreground border-border"
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="edit-courtCount">場地數量</Label>
              <Input
                id="edit-courtCount"
                type="number"
                min="1"
                value={editCourtCount}
                onChange={(e) => setEditCourtCount(parseInt(e.target.value) || 1)}
                className="bg-input text-foreground border-border"
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="edit-level">球隊等級</Label>
              <Select value={editSelectedLevel} onValueChange={(value) => setEditSelectedLevel(value as TeamLevel)}>
                <SelectTrigger id="edit-level" className="w-full bg-input text-foreground border-border">
                  <SelectValue placeholder="選擇球隊等級" />
                </SelectTrigger>
                <SelectContent>
                  {TEAM_LEVELS.map((level) => (
                    <SelectItem key={level} value={level}>
                      {level}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>

          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setEditDialogOpen(false)}
              disabled={isUpdating}
            >
              取消
            </Button>
            <Button
              onClick={handleUpdateTeam}
              disabled={isUpdating || !editTeamName.trim()}
              className="bg-primary text-primary-foreground hover:bg-primary/90"
            >
              {isUpdating ? "更新中..." : "保存變更"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* 查看团队成员对话框 */}
      <Dialog open={membersDialogOpen} onOpenChange={setMembersDialogOpen}>
        <DialogContent className="sm:max-w-[800px]">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-3">
              {viewingTeam && (
                <>
                  {viewingTeam.color && COLOR_MAP[viewingTeam.color] && (
                    <div
                      className="w-5 h-5 rounded-full border border-white shadow-sm"
                      style={{ backgroundColor: COLOR_MAP[viewingTeam.color] }}
                    />
                  )}
                  {viewingTeam.name} - 團隊成員
                </>
              )}
            </DialogTitle>
            <DialogDescription>
              查看該團隊的所有成員資訊
            </DialogDescription>
            {viewingTeam?.description && (
              <div className="mt-2 p-3 bg-muted/50 rounded-lg border">
                <p className="text-sm text-muted-foreground">
                  <span className="font-medium">團隊描述：</span>
                  {viewingTeam.description}
                </p>
              </div>
            )}
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
                  {/* 只有团队所有者才能添加成员 */}
                  {viewingTeam?.userId === user?.id && (
                    <Button
                      onClick={() => setAddMemberDialogOpen(true)}
                      size="sm"
                      className="bg-blue-600 hover:bg-blue-700 text-white"
                    >
                      <Plus className="w-4 h-4 mr-2" />
                      加入成員
                    </Button>
                  )}
                </div>
                <div className="grid gap-2 max-h-80 overflow-y-auto">
                  {teamMembers.map((member) => (
                    <div
                      key={member.id}
                      className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-800/50 rounded-lg border border-gray-200 dark:border-gray-700"
                    >
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded-full bg-blue-100 dark:bg-blue-900 flex items-center justify-center">
                          <span className="text-sm font-medium text-blue-700 dark:text-blue-300">
                            {(member.realName || member.username).charAt(0).toUpperCase()}
                          </span>
                        </div>
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
                                <span
                                  key={role}
                                  className="text-xs px-2 py-0.5 bg-blue-100 dark:bg-blue-900 text-blue-700 dark:text-blue-300 rounded"
                                >
                                  {role}
                                </span>
                              ))}
                            </div>
                          )}
                        </div>
                        {/* 只有团队所有者才能移除成员，且不能移除自己 */}
                        {viewingTeam?.userId === user?.id && member.id !== user?.id && (
                          <Button
                            size="sm"
                            variant="ghost"
                            onClick={() => handleRemoveMember(member.id)}
                            className="text-red-600 hover:bg-red-50 dark:hover:bg-red-950/50 hover:text-red-700 flex-shrink-0"
                          >
                            <X className="w-4 h-4" />
                          </Button>
                        )}
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

      {/* 加入成员对话框 */}
      <Dialog open={addMemberDialogOpen} onOpenChange={setAddMemberDialogOpen}>
        <DialogContent className="sm:max-w-[700px]">
          <DialogHeader>
            <DialogTitle>加入新成員</DialogTitle>
            <DialogDescription>
              選擇要加入 {viewingTeam?.name} 團隊的用戶
            </DialogDescription>
          </DialogHeader>

          <div className="py-4">
            {allUsers.length === 0 ? (
              <div className="text-center py-8">
                <p className="text-muted-foreground">無法載入用戶列表</p>
              </div>
            ) : (
              <div className="space-y-2 max-h-80 overflow-y-auto">
                {allUsers
                  .filter(user =>
                    user.isActive !== false &&
                    !teamMembers.some(member => member.id === user.id)
                  )
                  .map((user) => (
                    <div
                      key={user.id}
                      className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-800/50 rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-700/50 transition-colors"
                    >
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded-full bg-blue-100 dark:bg-blue-900 flex items-center justify-center">
                          <span className="text-sm font-medium text-blue-700 dark:text-blue-300">
                            {(user.realName || user.username).charAt(0).toUpperCase()}
                          </span>
                        </div>
                        <div>
                          <p className="font-medium text-foreground">
                            {user.realName || user.username}
                          </p>
                          <p className="text-xs text-muted-foreground">
                            {user.email || user.username}
                          </p>
                        </div>
                      </div>
                      <Button
                        size="sm"
                        onClick={() => handleAddMember(user.id)}
                        disabled={addingMember}
                        className="bg-green-600 hover:bg-green-700 text-white"
                      >
                        {addingMember ? "加入中..." : "加入"}
                      </Button>
                    </div>
                  ))}
                {allUsers.filter(user =>
                  user.isActive !== false &&
                  !teamMembers.some(member => member.id === user.id)
                ).length === 0 && (
                  <div className="text-center py-8">
                    <p className="text-muted-foreground">所有活躍用戶都已經是團隊成員了</p>
                  </div>
                )}
              </div>
            )}
          </div>

          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setAddMemberDialogOpen(false)}
              disabled={addingMember}
            >
              取消
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
