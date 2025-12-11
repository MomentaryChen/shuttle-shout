"use client"

import { useState, useEffect } from "react"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Button } from "@/components/ui/button"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuLabel, DropdownMenuSeparator, DropdownMenuTrigger } from "@/components/ui/dropdown-menu"
import { TeamManager } from "./team-manager"
import { PlayerManager } from "./player-manager"
import { CallingSystem } from "./calling-system"
import { TeamList } from "./team-list"
import { UserTeamOverview } from "./user-team-overview"
import { TeamManagement } from "./team-management"
import { playerApi, courtApi, queueApi, teamApi } from "@/lib/api"
import { PlayerDto, CourtDto, QueueDto, QueueStatus, TeamDto } from "@/types/api"
import { Team, Player, Court } from "@/types/api"
import { Spinner } from "@/components/ui/spinner"
import { useAuth } from "@/contexts/AuthContext"
import { LogOut, User, Settings } from "lucide-react"

// 叫號系統 Wrapper - 載入 teams, courts, players, queues
function CallingSystemWrapper() {
  const [loading, setLoading] = useState(true)
  const [teams, setTeams] = useState<Team[]>([])
  const [courts, setCourts] = useState<Court[]>([])
  const [players, setPlayers] = useState<Player[]>([])
  const [queues, setQueues] = useState<QueueDto[]>([])

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      setLoading(true)
      const [teamsData, playersData, courtsData, queuesData] = await Promise.all([
        teamApi.getAll(),
        playerApi.getAll(),
        courtApi.getAll(),
        queueApi.getAll(),
      ])

      setTeams(
        teamsData.map((team) => ({
          id: team.id.toString(),
          name: team.name,
          color: team.color || "bg-blue-500",
          maxPlayers: team.maxPlayers,
          courtCount: team.courtCount,
        }))
      )

      setCourts(
        courtsData.map((court) => ({
          id: court.id.toString(),
          name: court.name,
          teamId: court.teamId?.toString() || "",
          status: court.isActive ? "available" : "occupied",
        }))
      )

      setPlayers(
        playersData.map((player) => {
          const queue = queuesData.find((q) => q.playerId === player.id)
          const status = queue
            ? queue.status === QueueStatus.WAITING
              ? "waiting"
              : queue.status === QueueStatus.CALLED
              ? "playing"
              : "rest"
            : "waiting"

          return {
            id: player.id.toString(),
            name: player.name,
            teamId: player.teamId?.toString() || "",
            courtId: queue?.courtId?.toString(),
            status: status as "waiting" | "playing" | "rest",
          }
        })
      )

      setQueues(queuesData)
    } catch (error) {
      console.error("載入叫號系統數據失敗:", error)
    } finally {
      setLoading(false)
    }
  }

  const refreshQueues = async () => {
    try {
      const [queuesData, playersData] = await Promise.all([
        queueApi.getAll(),
        playerApi.getAll(),
      ])
      
      setQueues(queuesData)
      
      setPlayers(
        playersData.map((player) => {
          const queue = queuesData.find((q) => q.playerId === player.id)
          const status = queue
            ? queue.status === QueueStatus.WAITING
              ? "waiting"
              : queue.status === QueueStatus.CALLED
              ? "playing"
              : "rest"
            : "waiting"

          return {
            id: player.id.toString(),
            name: player.name,
            teamId: player.teamId?.toString() || "",
            courtId: queue?.courtId?.toString(),
            status: status as "waiting" | "playing" | "rest",
          }
        })
      )
    } catch (error) {
      console.error("刷新隊列失敗:", error)
    }
  }

  const queue = queues
    .filter((q) => q.status === QueueStatus.WAITING)
    .sort((a, b) => (a.queueNumber || 0) - (b.queueNumber || 0))
    .map((q) => q.playerId.toString())

  if (loading) {
    return (
      <div className="w-full flex items-center justify-center py-12">
        <div className="flex flex-col items-center gap-4">
          <Spinner className="w-8 h-8 text-blue-600" />
          <p className="text-muted-foreground">載入叫號系統數據中...</p>
        </div>
      </div>
    )
  }

  return (
    <CallingSystem
      teams={teams}
      courts={courts}
      players={players}
      queue={queue}
      onQueueUpdate={async () => {
        await refreshQueues()
      }}
      onPlayerStatusChange={async () => {
        await refreshQueues()
      }}
      onPlayerCourtChange={async () => {
        await refreshQueues()
      }}
      onCourtStatusChange={async (id, status) => {
        const courtId = parseInt(id)
        const court = courts.find((c) => c.id === id)
        if (court) {
          await courtApi.update(courtId, {
            isActive: status === "available",
          })
          await loadData()
        }
      }}
    />
  )
}

// 球隊管理 Wrapper - 只載入 teams
function TeamManagerWrapper() {
  const [loading, setLoading] = useState(true)
  const [teams, setTeams] = useState<Team[]>([])
  const [teamsData, setTeamsData] = useState<TeamDto[]>([])

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      setLoading(true)
      const teamsDataRes = await teamApi.getAll()
      
      setTeamsData(teamsDataRes)
      setTeams(
        teamsDataRes.map((team) => ({
          id: team.id.toString(),
          name: team.name,
          color: team.color || "bg-blue-500",
          maxPlayers: team.maxPlayers,
          courtCount: team.courtCount,
        }))
      )
    } catch (error) {
      console.error("載入球隊數據失敗:", error)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="w-full flex items-center justify-center py-12">
        <div className="flex flex-col items-center gap-4">
          <Spinner className="w-8 h-8 text-blue-600" />
          <p className="text-muted-foreground">載入球隊數據中...</p>
        </div>
      </div>
    )
  }

  return (
    <TeamManager
      teams={teams}
      teamsData={teamsData}
      onTeamsChange={async () => {
        await loadData()
      }}
    />
  )
}


// 人員配置 Wrapper - 載入 players, teams, courts
function PlayerManagerWrapper() {
  const [loading, setLoading] = useState(true)
  const [players, setPlayers] = useState<Player[]>([])
  const [teams, setTeams] = useState<Team[]>([])
  const [courts, setCourts] = useState<Court[]>([])

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      setLoading(true)
      const [playersData, teamsData, courtsData] = await Promise.all([
        playerApi.getAll(),
        teamApi.getAll(),
        courtApi.getAll(),
      ])

      setPlayers(
        playersData.map((player) => ({
          id: player.id.toString(),
          name: player.name,
          teamId: player.teamId?.toString() || "",
          courtId: player.courtId?.toString(),
          status: "waiting" as "waiting" | "playing" | "rest",
        }))
      )

      setTeams(
        teamsData.map((team) => ({
          id: team.id.toString(),
          name: team.name,
          color: team.color || "bg-blue-500",
          maxPlayers: team.maxPlayers,
          courtCount: team.courtCount,
        }))
      )

      setCourts(
        courtsData.map((court) => ({
          id: court.id.toString(),
          name: court.name,
          teamId: court.teamId?.toString() || "",
          status: court.isActive ? "available" : "occupied",
        }))
      )
    } catch (error) {
      console.error("載入人員數據失敗:", error)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="w-full flex items-center justify-center py-12">
        <div className="flex flex-col items-center gap-4">
          <Spinner className="w-8 h-8 text-blue-600" />
          <p className="text-muted-foreground">載入人員數據中...</p>
        </div>
      </div>
    )
  }

  return (
    <PlayerManager
      players={players}
      teams={teams}
      courts={courts}
      onPlayersChange={async () => {
        await loadData()
      }}
    />
  )
}

export function BadmintonSystem() {
  const { user, logout } = useAuth()
  const [activeTab, setActiveTab] = useState("user-teams")

  return (
    <div className="w-full h-full bg-background text-foreground">
      {/* Header */}
      <header className="bg-gradient-to-r from-white via-blue-50/50 to-white dark:from-card dark:via-card/95 dark:to-card border-b border-blue-200 dark:border-border/50 shadow-sm">
        <div className="max-w-7xl mx-auto px-6 py-4">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold bg-gradient-to-r from-blue-600 to-blue-500 dark:from-primary dark:to-primary/80 bg-clip-text text-transparent">羽球叫號系統</h1>
              <p className="text-muted-foreground text-sm mt-1">專業場地管理和人員配置系統</p>
            </div>
            <div className="flex items-center gap-4">
              <div className="text-sm text-muted-foreground">
                歡迎, <span className="font-medium text-foreground">{user?.realName || user?.username}</span>
              </div>
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="ghost" className="relative h-10 w-10 rounded-full">
                    <Avatar className="h-10 w-10">
                      <AvatarFallback className="bg-blue-100 text-blue-700 dark:bg-blue-900 dark:text-blue-300">
                        {user?.realName?.charAt(0) || user?.username?.charAt(0) || "U"}
                      </AvatarFallback>
                    </Avatar>
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent className="w-56" align="end" forceMount>
                  <DropdownMenuLabel className="font-normal">
                    <div className="flex flex-col space-y-1">
                      <p className="text-sm font-medium leading-none">
                        {user?.realName || user?.username}
                      </p>
                      <p className="text-xs leading-none text-muted-foreground">
                        {user?.email || user?.username}
                      </p>
                    </div>
                  </DropdownMenuLabel>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem className="cursor-pointer">
                    <User className="mr-2 h-4 w-4" />
                    <span>個人資料</span>
                  </DropdownMenuItem>
                  <DropdownMenuItem className="cursor-pointer">
                    <Settings className="mr-2 h-4 w-4" />
                    <span>設定</span>
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem
                    className="cursor-pointer text-red-600 focus:text-red-600"
                    onClick={logout}
                  >
                    <LogOut className="mr-2 h-4 w-4" />
                    <span>登出</span>
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-6 py-8 animate-in fade-in slide-in-from-bottom-4 duration-500">
        <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full space-y-6">
          <TabsList className="bg-blue-50/50 dark:bg-secondary border border-blue-100 dark:border-border/50 p-1.5 rounded-xl shadow-sm">
            <TabsTrigger value="user-teams" className="data-[state=active]:bg-white data-[state=active]:text-blue-700 dark:data-[state=active]:bg-background dark:data-[state=active]:text-foreground data-[state=active]:shadow-md">
              團隊總覽
            </TabsTrigger>
            <TabsTrigger value="calling" className="data-[state=active]:bg-white data-[state=active]:text-blue-700 dark:data-[state=active]:bg-background dark:data-[state=active]:text-foreground data-[state=active]:shadow-md">
              叫號系統
            </TabsTrigger>
            <TabsTrigger value="team-management" className="data-[state=active]:bg-white data-[state=active]:text-blue-700 dark:data-[state=active]:bg-background dark:data-[state=active]:text-foreground data-[state=active]:shadow-md">
              球隊管理
            </TabsTrigger>
            <TabsTrigger value="players" className="data-[state=active]:bg-white data-[state=active]:text-blue-700 dark:data-[state=active]:bg-background dark:data-[state=active]:text-foreground data-[state=active]:shadow-md">
              人員配置
            </TabsTrigger>
          </TabsList>

          {/* 用戶團隊總覽 - 只需要 teams API */}
          <TabsContent value="user-teams" className="space-y-6 mt-6">
            <div className="animate-in fade-in slide-in-from-right-4 duration-300">
              <UserTeamOverview />
            </div>
          </TabsContent>

          {/* 叫號系統 - 需要 teams, courts, players, queues */}
          <TabsContent value="calling" className="space-y-6 mt-6">
            <div className="animate-in fade-in slide-in-from-right-4 duration-300">
              <CallingSystemWrapper />
            </div>
          </TabsContent>

          {/* 球隊管理 - 整合總覽和管理功能 */}
          <TabsContent value="team-management" className="space-y-6 mt-6">
            <div className="animate-in fade-in slide-in-from-right-4 duration-300">
              <TeamManagement />
            </div>
          </TabsContent>

          {/* 人員配置 - 需要 players, teams, courts */}
          <TabsContent value="players" className="space-y-6 mt-6">
            <div className="animate-in fade-in slide-in-from-right-4 duration-300">
              <PlayerManagerWrapper />
            </div>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  )
}
