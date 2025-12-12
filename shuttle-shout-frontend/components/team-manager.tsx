"use client"

import { useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Trash2, Plus } from "lucide-react"
import { teamApi } from "@/lib/api"
import { TeamDto, TeamLevel } from "@/types/api"
import { toast } from "sonner"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"

interface TeamManagerProps {
  teams: Array<{ id: string; name: string; color: string; maxPlayers?: number; courtCount?: number }>
  teamsData: TeamDto[]
  onTeamsChange: (teams: Array<{ id: string; name: string; color: string }>) => void
}

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

// 球队等级选项
const TEAM_LEVELS: TeamLevel[] = ["新手", "初階", "中等", "強", "超強", "世界強"]

export function TeamManager({ teams, teamsData, onTeamsChange }: TeamManagerProps) {
  const [newTeamName, setNewTeamName] = useState("")
  const [selectedColor, setSelectedColor] = useState(COLORS[0])
  const [selectedLevel, setSelectedLevel] = useState<TeamLevel>("中等")
  const [maxPlayers, setMaxPlayers] = useState(20)
  const [courtCount, setCourtCount] = useState(2)
  const [isAdding, setIsAdding] = useState(false)
  const [deletingIds, setDeletingIds] = useState<Set<string>>(new Set())

  const handleAddTeam = async () => {
    if (!newTeamName.trim()) return

    try {
      setIsAdding(true)
      const createdTeam = await teamApi.create({
        name: newTeamName.trim(),
        color: selectedColor,
        level: selectedLevel,
        maxPlayers: maxPlayers,
        courtCount: courtCount,
        isActive: true,
      })

      const newTeam = {
        id: createdTeam.id.toString(),
        name: createdTeam.name,
        color: createdTeam.color || selectedColor,
        maxPlayers: createdTeam.maxPlayers,
        courtCount: createdTeam.courtCount,
      }

      onTeamsChange([...teams, newTeam])
      setNewTeamName("")
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

  const handleDeleteTeam = async (id: string) => {
    if (!confirm("確定要刪除此球隊嗎？刪除後相關的球員和場地也會受到影響。")) return

    try {
      setDeletingIds((prev) => new Set(prev).add(id))
      const teamId = parseInt(id)
      await teamApi.delete(teamId)
      onTeamsChange(teams.filter((t) => t.id !== id))
      toast.success("球隊刪除成功")
    } catch (error: any) {
      console.error("刪除球隊失败:", error)
      toast.error(error.message || "刪除球隊失败，请重试")
    } finally {
      setDeletingIds((prev) => {
        const newSet = new Set(prev)
        newSet.delete(id)
        return newSet
      })
    }
  }

  const getTeamData = (teamId: string) => {
    return teamsData.find((t) => t.id.toString() === teamId)
  }

  return (
    <Card className="bg-card border-border">
      <CardHeader>
        <CardTitle>球隊管理</CardTitle>
      </CardHeader>
      <CardContent className="space-y-6">
        {/* Add Team Form */}
        <div className="space-y-3 p-4 bg-secondary rounded-lg border border-border">
          <h3 className="font-semibold">新增球隊</h3>
          <Input
            placeholder="輸入球隊名稱"
            value={newTeamName}
            onChange={(e) => setNewTeamName(e.target.value)}
            className="bg-input text-foreground border-border placeholder:text-muted-foreground"
            onKeyPress={(e) => e.key === "Enter" && handleAddTeam()}
          />
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
          <div className="space-y-2">
            <Label htmlFor="level">球隊等級</Label>
            <Select value={selectedLevel} onValueChange={(value) => setSelectedLevel(value as TeamLevel)}>
              <SelectTrigger id="level" className="bg-input text-foreground border-border">
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
          <div className="grid grid-cols-2 gap-3">
            <div>
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
            <div>
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
          </div>
          <Button
            onClick={handleAddTeam}
            disabled={isAdding}
            className="w-full bg-primary text-primary-foreground hover:bg-primary/90"
          >
            <Plus className="w-4 h-4 mr-2" />
            {isAdding ? "添加中..." : "新增球隊"}
          </Button>
        </div>

        {/* Teams List */}
        <div className="space-y-2">
          {teams.map((team) => {
            const teamData = getTeamData(team.id)
            return (
              <div
                key={team.id}
                className="flex items-center justify-between p-4 bg-secondary rounded-lg border border-border hover:border-primary/50 transition-colors"
              >
                <div className="flex-1">
                  <div className="flex items-center gap-3 mb-2">
                    <div className={`w-4 h-4 rounded ${team.color}`} />
                    <span className="font-medium text-lg">{team.name}</span>
                  </div>
                  <div className="grid grid-cols-2 gap-4 text-sm text-muted-foreground">
                    <div>
                      <span className="font-semibold">人員：</span>
                      {teamData?.currentPlayerCount || 0} / {team.maxPlayers || teamData?.maxPlayers || 20} 人
                    </div>
                    <div>
                      <span className="font-semibold">場地：</span>
                      {teamData?.currentCourtCount || 0} / {team.courtCount || teamData?.courtCount || 2} 個
                    </div>
                  </div>
                </div>
                <Button
                  size="sm"
                  variant="ghost"
                  onClick={() => handleDeleteTeam(team.id)}
                  disabled={deletingIds.has(team.id)}
                  className="text-destructive hover:bg-destructive/10 hover:text-destructive"
                >
                  <Trash2 className="w-4 h-4" />
                </Button>
              </div>
            )
          })}
          {teams.length === 0 && <p className="text-center text-muted-foreground py-8">未新增任何球隊</p>}
        </div>
      </CardContent>
    </Card>
  )
}
