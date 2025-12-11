"use client"

import { useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Badge } from "@/components/ui/badge"
import { Trash2, Plus } from "lucide-react"
import { playerApi } from "@/lib/api"
import { toast } from "sonner"
import { TeamDto } from "@/types/api"

interface PlayerManagerProps {
  players: Array<{ id: string; name: string; teamId: string; courtId?: string; status: "waiting" | "playing" | "rest" }>
  teams: Array<{ id: string; name: string; color: string }>
  courts: Array<{ id: string; name: string; teamId: string; status: "available" | "occupied" }>
  onPlayersChange: (
    players: Array<{
      id: string
      name: string
      teamId: string
      courtId?: string
      status: "waiting" | "playing" | "rest"
    }>,
  ) => void
}

const STATUS_COLORS: Record<string, string> = {
  waiting: "bg-blue-500/20 text-blue-300",
  playing: "bg-green-500/20 text-green-300",
  rest: "bg-gray-500/20 text-gray-300",
}

const STATUS_LABELS: Record<string, string> = {
  waiting: "等待中",
  playing: "進行中",
  rest: "休息中",
}

export function PlayerManager({ players, teams, courts, onPlayersChange }: PlayerManagerProps) {
  const [newPlayerName, setNewPlayerName] = useState("")
  const [selectedTeam, setSelectedTeam] = useState(teams[0]?.id || "")
  const [isAdding, setIsAdding] = useState(false)
  const [deletingIds, setDeletingIds] = useState<Set<string>>(new Set())

  const handleAddPlayer = async () => {
    if (!newPlayerName.trim() || !selectedTeam) return

    try {
      setIsAdding(true)
      const createdPlayer = await playerApi.create({
        name: newPlayerName.trim(),
        teamId: parseInt(selectedTeam),
      })

      const newPlayer = {
        id: createdPlayer.id.toString(),
        name: createdPlayer.name,
        teamId: selectedTeam,
        status: "waiting" as const,
      }

      onPlayersChange([...players, newPlayer])
      setNewPlayerName("")
      toast.success("人員添加成功")
    } catch (error) {
      console.error("添加人員失败:", error)
      toast.error("添加人員失败，请重试")
    } finally {
      setIsAdding(false)
    }
  }

  const handleDeletePlayer = async (id: string) => {
    if (!confirm("確定要刪除此人員嗎？")) return

    try {
      setDeletingIds((prev) => new Set(prev).add(id))
      const playerId = parseInt(id)
      await playerApi.delete(playerId)
      onPlayersChange(players.filter((p) => p.id !== id))
      toast.success("人員刪除成功")
    } catch (error) {
      console.error("刪除人員失败:", error)
      toast.error("刪除人員失败，请重试")
    } finally {
      setDeletingIds((prev) => {
        const newSet = new Set(prev)
        newSet.delete(id)
        return newSet
      })
    }
  }

  const getTeamName = (teamId: string) => teams.find((t) => t.id === teamId)?.name || "未知"
  const getCourtName = (courtId?: string) => (courtId ? courts.find((c) => c.id === courtId)?.name : "無")

  // Group players by team
  const playersByTeam = teams.map((team) => ({
    team,
    players: players.filter((p) => p.teamId === team.id),
  }))

  return (
    <Card className="bg-card border-border">
      <CardHeader>
        <CardTitle>人員配置</CardTitle>
      </CardHeader>
      <CardContent className="space-y-6">
        {/* Add Player Form */}
        <div className="space-y-3 p-4 bg-secondary rounded-lg border border-border">
          <h3 className="font-semibold">新增人員</h3>
          <Input
            placeholder="輸入人員名稱"
            value={newPlayerName}
            onChange={(e) => setNewPlayerName(e.target.value)}
            className="bg-input text-foreground border-border placeholder:text-muted-foreground"
          />
          <Select value={selectedTeam} onValueChange={setSelectedTeam}>
            <SelectTrigger className="bg-input text-foreground border-border">
              <SelectValue placeholder="選擇球隊" />
            </SelectTrigger>
            <SelectContent>
              {teams.map((team) => (
                <SelectItem key={team.id} value={team.id}>
                  {team.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Button
            onClick={handleAddPlayer}
            className="w-full bg-primary text-primary-foreground hover:bg-primary/90"
            disabled={!selectedTeam || isAdding}
          >
            <Plus className="w-4 h-4 mr-2" />
            {isAdding ? "添加中..." : "新增人員"}
          </Button>
        </div>

        {/* Players by Team */}
        <div className="space-y-6">
          {playersByTeam.map(({ team, players: teamPlayers }) => (
            <div key={team.id} className="space-y-3">
              <h3 className="font-semibold flex items-center gap-2">
                <div className={`w-3 h-3 rounded ${team.color}`} />
                {team.name} ({teamPlayers.length} 人)
              </h3>
              <div className="space-y-2">
                {teamPlayers.length === 0 ? (
                  <p className="text-sm text-muted-foreground">暫無人員</p>
                ) : (
                  teamPlayers.map((player) => (
                    <div
                      key={player.id}
                      className="flex items-center justify-between p-3 bg-secondary rounded-lg border border-border hover:border-primary/50 transition-colors"
                    >
                      <div className="flex-1">
                        <p className="font-medium">{player.name}</p>
                        <p className="text-sm text-muted-foreground">場地：{getCourtName(player.courtId)}</p>
                      </div>
                      <div className="flex items-center gap-2">
                        <Badge className={STATUS_COLORS[player.status]}>{STATUS_LABELS[player.status]}</Badge>
                        <Button
                          size="sm"
                          variant="ghost"
                          onClick={() => handleDeletePlayer(player.id)}
                          disabled={deletingIds.has(player.id)}
                          className="text-destructive hover:bg-destructive/10 hover:text-destructive"
                        >
                          <Trash2 className="w-4 h-4" />
                        </Button>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  )
}
