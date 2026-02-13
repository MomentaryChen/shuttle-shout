"use client"

import { useState, useEffect } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Button } from "@/components/ui/button"
import { Switch } from "@/components/ui/switch"
import { Badge } from "@/components/ui/badge"
import { courtApi, teamApi } from "@/lib/api"
import { CourtDto, TeamDto } from "@/types/api"
import { Spinner } from "@/components/ui/spinner"
import { toast } from "sonner"
import { MapPin, RefreshCw } from "lucide-react"

export function CourtManagement() {
  const [courts, setCourts] = useState<CourtDto[]>([])
  const [teams, setTeams] = useState<TeamDto[]>([])
  const [loading, setLoading] = useState(true)
  const [updatingId, setUpdatingId] = useState<number | null>(null)

  const loadData = async () => {
    try {
      setLoading(true)
      const [courtsData, teamsData] = await Promise.all([
        courtApi.getAll(),
        teamApi.getAll(),
      ])
      setCourts(courtsData)
      setTeams(teamsData)
    } catch (error) {
      console.error("載入球場數據失敗:", error)
      toast.error("載入球場數據失敗")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  const getTeamName = (teamId?: number) => {
    if (teamId == null) return "—"
    const team = teams.find((t) => t.id === teamId)
    return team?.name ?? `團隊 #${teamId}`
  }

  const onToggleActive = async (court: CourtDto, checked: boolean) => {
    if (!court.id) return
    try {
      setUpdatingId(court.id)
      await courtApi.update(court.id, { ...court, isActive: checked })
      setCourts((prev) =>
        prev.map((c) => (c.id === court.id ? { ...c, isActive: checked } : c))
      )
      toast.success(checked ? "已啟用場地" : "已停用場地")
    } catch (error) {
      console.error("更新場地狀態失敗:", error)
      toast.error("更新場地狀態失敗")
    } finally {
      setUpdatingId(null)
    }
  }

  if (loading) {
    return (
      <div className="w-full flex items-center justify-center py-12">
        <div className="flex flex-col items-center gap-4">
          <Spinner className="w-8 h-8 text-blue-600" />
          <p className="text-muted-foreground">載入球場數據中...</p>
        </div>
      </div>
    )
  }

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <div>
          <CardTitle className="flex items-center gap-2">
            <MapPin className="h-5 w-5" />
            球場管理
          </CardTitle>
          <CardDescription>檢視與啟用/停用各團隊的場地</CardDescription>
        </div>
        <Button variant="outline" size="sm" onClick={loadData}>
          <RefreshCw className="h-4 w-4 mr-2" />
          重新整理
        </Button>
      </CardHeader>
      <CardContent>
        {courts.length === 0 ? (
          <p className="text-muted-foreground text-center py-8">尚無球場資料，請先在球隊管理中建立團隊並初始化場地。</p>
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>場地名稱</TableHead>
                <TableHead>所屬團隊</TableHead>
                <TableHead>狀態</TableHead>
                <TableHead className="text-right">啟用</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {courts.map((court) => (
                <TableRow key={court.id}>
                  <TableCell className="font-medium">{court.name}</TableCell>
                  <TableCell>{getTeamName(court.teamId)}</TableCell>
                  <TableCell>
                    <Badge variant={court.isActive ? "default" : "secondary"}>
                      {court.isActive ? "啟用" : "停用"}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-right">
                    <Switch
                      checked={court.isActive ?? true}
                      disabled={updatingId === court.id}
                      onCheckedChange={(checked) => onToggleActive(court, checked)}
                    />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </CardContent>
    </Card>
  )
}
