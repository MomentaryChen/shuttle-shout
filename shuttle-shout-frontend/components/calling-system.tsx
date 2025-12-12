"use client"

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { queueApi } from "@/lib/api"
import { toast } from "sonner"

interface CallSystemProps {
  teams: Array<{ id: string; name: string; color: string }>
  courts: Array<{ id: string; name: string; teamId: string; status: "available" | "occupied" }>
  players: Array<{ id: string; name: string; teamId: string; courtId?: string; status: "waiting" | "playing" | "rest" }>
  queue: string[]
  onQueueUpdate: (queue: string[]) => void
  onPlayerStatusChange: (id: string, status: "waiting" | "playing" | "rest") => void
  onPlayerCourtChange: (id: string, courtId: string | undefined) => void
  onCourtStatusChange: (id: string, status: "available" | "occupied") => void
}

export function CallingSystem({
  teams,
  courts,
  players,
  queue,
  onQueueUpdate,
  onPlayerStatusChange,
  onPlayerCourtChange,
  onCourtStatusChange,
}: CallSystemProps) {
  const getPlayerById = (id: string) => players.find((p) => p.id === id)
  const getTeamById = (id: string) => teams.find((t) => t.id === id)
  const getCourtById = (id: string) => courts.find((c) => c.id === id)

  const handleCallPlayer = async (courtId: string) => {
    if (queue.length === 0) return

    const court = getCourtById(courtId)
    if (!court) return

    // 找到隊列中第一個匹配場地隊伍的玩家
    let playerId: string | null = null
    let playerIndex = -1

    for (let i = 0; i < queue.length; i++) {
      const id = queue[i]
      const player = getPlayerById(id)
      if (player && player.teamId === court.teamId) {
        playerId = id
        playerIndex = i
        break
      }
    }

    // 如果沒有找到匹配的玩家，則不進行叫號
    if (!playerId || playerIndex === -1) {
      toast.warning("隊列中沒有匹配此場地隊伍的玩家")
      return
    }

    try {
      const playerIdNum = parseInt(playerId)
      const courtIdNum = parseInt(courtId)
      
      // 先獲取所有等待中的隊列，找到對應的隊列ID
      const waitingQueues = await queueApi.getWaiting()
      const targetQueue = waitingQueues.find((q) => q.playerId === playerIdNum)
      
      if (!targetQueue) {
        toast.error("找不到對應的隊列")
        return
      }
      
      // 調用API叫號
      await queueApi.call(targetQueue.id, courtIdNum)
      
      // 更新本地狀態
      onPlayerStatusChange(playerId, "playing")
      onPlayerCourtChange(playerId, courtId)
      onCourtStatusChange(courtId, "occupied")
      
      // 刷新隊列
      await onQueueUpdate([])
      
      toast.success("叫號成功")
    } catch (error) {
      console.error("叫號失敗:", error)
      toast.error("叫號失敗，請重試")
    }
  }

  const handleFinishGame = async (courtId: string) => {
    const court = getCourtById(courtId)
    if (!court) return

    // 查找此場地正在進行的玩家
    const playingPlayers = players.filter((p) => p.courtId === courtId && p.status === "playing")

    try {
      // 獲取所有隊列，找到正在進行的隊列
      const allQueues = await queueApi.getAll()
      const courtIdNum = parseInt(courtId)

      // 完成所有正在進行的隊列
      for (const player of playingPlayers) {
        const playerIdNum = parseInt(player.id)
        // 查找該玩家在此場地的隊列
        const playerQueue = allQueues.find(
          (q) => q.playerId === playerIdNum && q.courtId === courtIdNum && q.status === "CALLED"
        )

        if (playerQueue) {
          try {
            // 完成服務
            await queueApi.serve(playerQueue.id)
          } catch (error) {
            console.error(`完成玩家 ${player.id} 的服務失敗:`, error)
          }
        }

        // 更新本地狀態
        onPlayerStatusChange(player.id, "rest")
        onPlayerCourtChange(player.id, undefined)
      }

      // 更新場地狀態
      await onCourtStatusChange(courtId, "available")

      // 刷新隊列
      await onQueueUpdate([])

      toast.success("比賽結束")

      // 自動補號：如果隊列中還有等待的人，自動叫下一個號
      if (queue.length > 0) {
        setTimeout(() => {
          handleCallPlayer(courtId)
        }, 500)
      }
    } catch (error) {
      console.error("結束比賽失敗:", error)
      toast.error("結束比賽失敗，請重試")
    }
  }

  const getAvailableCourts = () => courts.filter((c) => c.status === "available")
  const getQueuePlayers = () => queue.map((id) => getPlayerById(id)).filter(Boolean)

  // 檢查隊列中是否有匹配場地隊伍的玩家
  const hasMatchingPlayer = (courtId: string) => {
    const court = getCourtById(courtId)
    if (!court) return false

    return queue.some((id) => {
      const player = getPlayerById(id)
      return player && player.teamId === court.teamId
    })
  }

  return (
    <div className="space-y-6">
      {/* Court Status Overview */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {courts.map((court) => {
          const team = getTeamById(court.teamId)
          const playingPlayers = players.filter((p) => p.courtId === court.id && p.status === "playing")

          return (
            <Card key={court.id} className="bg-card border-border">
              <CardHeader className="pb-3">
                <div className="flex items-center justify-between">
                  <CardTitle className="text-lg">{court.name}</CardTitle>
                  <Badge
                    className={`${
                      court.status === "available" ? "bg-green-500/20 text-green-300" : "bg-red-500/20 text-red-300"
                    }`}
                  >
                    {court.status === "available" ? "可用" : "使用中"}
                  </Badge>
                </div>
                <p className="text-sm text-muted-foreground">{team?.name}</p>
              </CardHeader>
              <CardContent className="space-y-3">
                {court.status === "occupied" && playingPlayers.length > 0 && (
                  <div>
                    <p className="text-xs text-muted-foreground mb-2">正在進行</p>
                    <div className="space-y-1">
                      {playingPlayers.map((p) => (
                        <p key={p.id} className="text-sm font-medium text-primary">
                          {p.name}
                        </p>
                      ))}
                    </div>
                  </div>
                )}
                <div className="flex gap-2 pt-2">
                  {court.status === "available" ? (
                    <Button
                      size="sm"
                      className="flex-1 bg-primary text-primary-foreground hover:bg-primary/90"
                      onClick={() => handleCallPlayer(court.id)}
                      disabled={!hasMatchingPlayer(court.id)}
                    >
                      叫號
                    </Button>
                  ) : (
                    <Button
                      size="sm"
                      variant="outline"
                      className="flex-1 bg-transparent"
                      onClick={() => handleFinishGame(court.id)}
                    >
                      結束
                    </Button>
                  )}
                </div>
              </CardContent>
            </Card>
          )
        })}
      </div>

      {/* Queue Display */}
      <Card className="bg-card border-border">
        <CardHeader>
          <CardTitle className="text-xl">等待隊列</CardTitle>
        </CardHeader>
        <CardContent>
          {getQueuePlayers().length === 0 ? (
            <p className="text-muted-foreground text-center py-8">目前沒有等待中的人員</p>
          ) : (
            <div className="space-y-2">
              {getQueuePlayers().map((player, index) => {
                const team = getTeamById(player!.teamId)
                return (
                  <div
                    key={player!.id}
                    className={`flex items-center gap-4 p-3 rounded-lg ${
                      index === 0 ? "bg-primary/20 border-2 border-primary" : "bg-secondary border border-border"
                    }`}
                  >
                    <div className="w-8 h-8 rounded-full bg-primary/30 flex items-center justify-center text-sm font-bold">
                      {index + 1}
                    </div>
                    <div className="flex-1">
                      <p className="font-medium">{player!.name}</p>
                      <p className="text-sm text-muted-foreground">{team?.name}</p>
                    </div>
                    {index === 0 && <Badge className="bg-green-500/20 text-green-300 animate-pulse">下一個叫號</Badge>}
                  </div>
                )
              })}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
