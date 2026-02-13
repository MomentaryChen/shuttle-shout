"use client"

import { useState, useEffect, useRef } from "react"
import { useSearchParams } from "next/navigation"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { Spinner } from "@/components/ui/spinner"
import { Alert, AlertDescription } from "@/components/ui/alert"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { teamApi, userTeamApi, userApi } from "@/lib/api"
import { TeamDto, UserDto, UserTeamDto } from "@/types/api"
import { useAuth } from "@/contexts/AuthContext"
import { toast } from "sonner"
import { Users, Play, Wifi, WifiOff, AlertCircle, Square } from "lucide-react"

interface CourtPlayer {
  userId: number
  userName: string
  userRealName?: string
  position: number // 1-4
}

interface Court {
  id: number
  name: string
  players: CourtPlayer[]
  isActive: boolean
  isPending?: boolean // 是否為待確認狀態
  matchStartedAt?: string // 比賽開始時間
}

interface TeamCallingSystemProps {
  onClearQueueRef?: React.MutableRefObject<(() => void) | null>
  /** 點擊「結束」後執行，用於導向離開叫號頁（如 router.back） */
  onEndSession?: () => void
}

export function TeamCallingSystem({ onClearQueueRef, onEndSession }: TeamCallingSystemProps) {
  const searchParams = useSearchParams()
  const urlTeamId = searchParams?.get("teamId") || ""
  const { user, isAuthenticated } = useAuth()
  
  const [teamId, setTeamId] = useState<string>(urlTeamId)
  const [team, setTeam] = useState<TeamDto | null>(null)
  const [teamMembers, setTeamMembers] = useState<UserTeamDto[]>([])
  const [allUsers, setAllUsers] = useState<UserDto[]>([])
  const [courts, setCourts] = useState<Court[]>([])
  const [waitingQueue, setWaitingQueue] = useState<UserTeamDto[]>([])
  const [loading, setLoading] = useState(false)
  const [wsConnected, setWsConnected] = useState(false)
  const [wsConnecting, setWsConnecting] = useState(false) // WebSocket 連接中狀態
  const [isAuthorized, setIsAuthorized] = useState<boolean | null>(null) // null = 未检查, true = 已授权, false = 未授权
  const [pendingCourtId, setPendingCourtId] = useState<number | null>(null) // 待確認的場地 ID
  const [showGameStateDialog, setShowGameStateDialog] = useState(false) // 是否顯示遊戲狀態選擇對話框
  const [gameStateData, setGameStateData] = useState<{hasOngoingMatches: boolean, ongoingCourtsCount: number} | null>(null) // 遊戲狀態數據
  const [isEndingSession, setIsEndingSession] = useState(false) // 防止「結束」重複點擊
  const wsRef = useRef<WebSocket | null>(null)
  // 自動重連計時器（30 秒重試一次）
  const reconnectTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  // 如果 URL 中有 teamId，自動加載
  useEffect(() => {
    if (urlTeamId && urlTeamId !== teamId) {
      setTeamId(urlTeamId)
    }
  }, [urlTeamId])

  // 當 teamId 從 URL 參數設置時，自動加載團隊信息並連接 WebSocket
  useEffect(() => {
    if (urlTeamId && urlTeamId === teamId && teamId !== "" && !isNaN(Number(teamId)) && !team && !loading && isAuthenticated) {
      console.log("自動加載團隊並準備重新連接 WebSocket, teamId:", teamId)
      loadTeam()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [urlTeamId, teamId, isAuthenticated])

  // 實時更新等待時間和場地時間（每秒更新一次）
  useEffect(() => {
    const interval = setInterval(() => {
      // 觸發重新渲染以更新時間顯示
      setWaitingQueue((prev) => [...prev])
      setCourts((prev) => [...prev])
    }, 1000) // 每秒更新一次

    return () => clearInterval(interval)
  }, [])

  // 初始化場地
  const initializeCourts = (courtCount: number) => {
    const newCourts: Court[] = []
    for (let i = 1; i <= courtCount; i++) {
      newCourts.push({
        id: i,
        name: `場地 ${i}`,
        players: [],
        isActive: false,
      })
    }
    setCourts(newCourts)
  }

  // 加載團隊信息
  const loadTeam = async () => {
    if (!teamId || isNaN(Number(teamId))) {
      toast.error("請輸入有效的團隊 ID")
      return
    }

    // 檢查是否已登錄
    if (!isAuthenticated || !user) {
      toast.error("請先登錄以使用叫號系統")
      setIsAuthorized(false)
      return
    }

    try {
      setLoading(true)
      const teamData = await teamApi.getById(Number(teamId))
      
      // 檢查當前用戶是否為團隊所有者（teamData.userId 就是 owner）
      if (teamData.userId !== user.id) {
        toast.error("只有團隊所有者才能使用叫號系統")
        setIsAuthorized(false)
        setTeam(null)
        setTeamMembers([])
        setCourts([])
        setWaitingQueue([])
        return
      }

      // 權限檢查通過
      setIsAuthorized(true)
      setTeam(teamData)

      // 初始化場地
      initializeCourts(teamData.courtCount || 0)

      // 加載團隊成員
      const membersData = await userTeamApi.getTeamMembers(teamData.id)
      setTeamMembers(membersData)

      // 加載所有用戶信息（用於顯示詳細信息）
      const usersData = await userApi.getAll()
      setAllUsers(usersData)

      // 等待隊列將由後端通過 WebSocket 發送
      // 不再在前端初始化，等待後端發送 QUEUE_UPDATE 消息

      toast.success("團隊信息加載成功")
      
      console.log("團隊加載成功，準備自動連接 WebSocket")
      // 自動連接WebSocket - 直接傳遞 teamData 避免閉包問題
      setTimeout(() => {
        console.log("開始自動重新連接...")
        connectWebSocket(teamData)
      }, 500) // 延遲500ms確保狀態已更新
    } catch (error: any) {
      console.error("加載團隊失敗:", error)
      toast.error(error.message || "加載團隊失敗，請檢查團隊 ID")
      setIsAuthorized(false)
      setTeam(null)
      setTeamMembers([])
      setCourts([])
      setWaitingQueue([])
    } finally {
      setLoading(false)
    }
  }

  // WebSocket 連接
  const connectWebSocket = (teamData?: TeamDto) => {
    // 使用傳入的 teamData 或狀態中的 team
    const currentTeam = teamData || team
    console.log("connectWebSocket 被調用, currentTeam:", currentTeam)
    
    if (!currentTeam) {
      console.error("無法重新連接 WebSocket: 團隊信息未加載")
      toast.error("請先加載團隊信息")
      return
    }

    if (wsRef.current?.readyState === WebSocket.OPEN) {
      console.log("WebSocket 已經處於連接狀態")
      toast.info("WebSocket 已連接")
      return
    }

    // 如果正在連接中，不重複連接
    if (wsConnecting) {
      console.log("WebSocket 正在連接中，請稍候...")
      return
    }

    try {
      // 每次主動連線前，清掉舊的自動重連計時器，避免重複排程
      if (reconnectTimerRef.current) {
        clearTimeout(reconnectTimerRef.current)
        reconnectTimerRef.current = null
      }

      // 設置連接中狀態
      setWsConnecting(true)
      
      // WebSocket URL - 根據實際後端配置調整
      const wsUrl = process.env.NEXT_PUBLIC_WS_URL || "ws://localhost:18080/api/ws"
      const fullWsUrl = `${wsUrl}?teamId=${currentTeam.id}`
      console.log("正在重新連接 WebSocket:", fullWsUrl)
      
      const ws = new WebSocket(fullWsUrl)

      ws.onopen = () => {
        setWsConnected(true)
        setWsConnecting(false)
        // 連線成功後，清除自動重連計時器
        if (reconnectTimerRef.current) {
          clearTimeout(reconnectTimerRef.current)
          reconnectTimerRef.current = null
        }
        toast.success("WebSocket 連接成功")
        console.log("WebSocket 連接已建立, teamId:", currentTeam.id)
      }

      ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          console.log("WebSocket 收到消息:", data)
          
          // 處理不同類型的消息
          if (data.type === "CONNECTED") {
            // WebSocket 連接確認
            console.log("WebSocket 連接已確認, teamId:", data.teamId)
          } else if (data.type === "QUEUE_UPDATE") {
            // 更新隊列
            handleQueueUpdate(data.queue)
          } else if (data.type === "COURT_UPDATE") {
            // 更新場地狀態
            handleCourtUpdate(data.court)
          } else if (data.type === "PLAYER_ASSIGNED" || data.type === "ASSIGN_PLAYER") {
            // 玩家被分配到場地
            handlePlayerAssigned(data.playerId, data.courtId, data.position)
          } else if (data.type === "PLAYER_REMOVED" || data.type === "REMOVE_PLAYER") {
            // 玩家從場地移除
            handlePlayerRemoved(data.playerId, data.courtId)
          } else if (data.type === "AUTO_ASSIGN_SUCCESS") {
            // 自動分配成功 - 執行實際的分配邏輯
            console.log("收到 AUTO_ASSIGN_SUCCESS 消息:", data)
            console.log("完整消息數據:", JSON.stringify(data, null, 2))
            
            // 確保類型正確（可能從 JSON 解析後是字符串或其他類型）
            const courtId = typeof data.courtId === 'number' ? data.courtId : parseInt(data.courtId)
            const assignments = data.assignments || []
            const isPending = data.isPending === true // 標記為待確認狀態
            
            console.log(`courtId: ${courtId} (type: ${typeof courtId})`)
            console.log(`assignments:`, assignments)
            console.log(`assignments length: ${assignments.length}`)
            console.log(`isPending: ${isPending}`)
            
            if (Array.isArray(assignments) && assignments.length > 0) {
              // 獲取當前場地狀態
              const currentCourt = courts.find((c) => c.id === courtId)
              const wasEmpty = currentCourt?.players.length === 0
              
              // 構建新的球員列表
              const newPlayers: CourtPlayer[] = []
              
              assignments.forEach((assignment: any) => {
                // 確保數據類型正確
                const userId = typeof assignment.userId === 'number' ? assignment.userId : parseInt(assignment.userId)
                const position = typeof assignment.position === 'number' ? assignment.position : parseInt(assignment.position)
                
                // 查找成員信息
                let member = teamMembers.find((m) => m.userId === userId)
                
                // 如果找不到，使用 assignment 中的信息
                if (!member && assignment.userName) {
                  member = {
                    userId: userId,
                    userName: assignment.userName || `用戶${userId}`,
                    userRealName: assignment.userRealName,
                  } as UserTeamDto
                }
                
                if (member) {
                  newPlayers.push({
                    userId: member.userId,
                    userName: member.userName || `用戶${member.userId}`,
                    userRealName: member.userRealName || assignment.userRealName,
                    position: position,
                  })
                }
              })
              
              // 更新場地狀態
              setCourts((prev) =>
                prev.map((c) => {
                  if (c.id === courtId) {
                    // 自動分配後總是設置為待確認狀態（除非後端明確標記為非待確認）
                    return {
                      ...c,
                      players: newPlayers,
                      isActive: !isPending, // 如果是待確認，則不標記為進行中
                      isPending: isPending,
                      matchStartedAt: data.matchStartedAt || c.matchStartedAt, // 更新比賽開始時間（如果有的話）
                    }
                  }
                  return c
                })
              )
              
              // 如果設置為待確認，記錄 courtId
              if (isPending) {
                setPendingCourtId(courtId)
              }
              
              // 等待隊列將由後端自動更新，無需前端手動管理
              
              toast.success(`已為場地分配 ${newPlayers.length} 位人員${isPending ? '，請確認後開始比賽' : ''}`)
            } else {
              console.warn("assignments 為空或不是數組", typeof assignments, assignments)
              toast.info("沒有可分配的人員")
            }
          } else if (data.type === "CONFIRM_START_MATCH_SUCCESS") {
            // 確認開始比賽成功
            console.log("收到 CONFIRM_START_MATCH_SUCCESS 消息:", data)
            
            const courtId = typeof data.courtId === 'number' ? data.courtId : parseInt(data.courtId)
            const assignments = data.assignments || []
            const matchStartedAt = data.matchStartedAt
            
            // 更新場地狀態為正式開始
            setCourts((prev) =>
              prev.map((c) => {
                if (c.id === courtId) {
                  // 如果有新的分配信息，更新球員列表
                  let updatedPlayers = c.players
                  if (Array.isArray(assignments) && assignments.length > 0) {
                    updatedPlayers = assignments.map((assignment: any) => {
                      const userId = typeof assignment.userId === 'number' ? assignment.userId : parseInt(assignment.userId)
                      const position = typeof assignment.position === 'number' ? assignment.position : parseInt(assignment.position)
                      const member = teamMembers.find((m) => m.userId === userId)
                      
                      return {
                        userId: member?.userId || userId,
                        userName: member?.userName || assignment.userName || `用戶${userId}`,
                        userRealName: member?.userRealName || assignment.userRealName,
                        position: position,
                      } as CourtPlayer
                    })
                  }
                  
                  return {
                    ...c,
                    players: updatedPlayers,
                    isActive: true,
                    isPending: false,
                    matchStartedAt: matchStartedAt || c.matchStartedAt,
                  }
                }
                return c
              })
            )
            
            // 清除待確認狀態
            setPendingCourtId(null)
            
            const court = courts.find((c) => c.id === courtId)
            toast.success(`場地 ${court?.name || courtId} 比賽已開始！`)
          } else if (data.type === "GAME_STATE_CHECK") {
            // 遊戲狀態檢查，顯示選擇對話框
            console.log("收到遊戲狀態檢查:", data)
            setGameStateData({
              hasOngoingMatches: data.hasOngoingMatches || false,
              ongoingCourtsCount: data.ongoingCourtsCount || 0,
            })
            setShowGameStateDialog(true)
          } else if (data.type === "RESTORE_ONGOING_MATCHES") {
            // 恢復正在進行的比賽
            console.log("收到恢復正在進行比賽消息:", data)
            handleRestoreOngoingMatches(data)
            setShowGameStateDialog(false)
          } else if (data.type === "START_NEW_GAME_SUCCESS") {
            // 開始新局成功
            console.log("開始新局成功:", data)
            toast.success(data.message || "已清空所有場地，可以開始新的一局")
            // 清空所有場地的狀態
            setCourts((prev) =>
              prev.map((court) => ({
                ...court,
                players: [],
                isActive: false,
                isPending: false,
              }))
            )
            setWaitingQueue([])
            setShowGameStateDialog(false)
          } else if (data.type === "CLEAR_QUEUE_SUCCESS") {
            // 清除隊列成功
            console.log("清除隊列成功:", data)
            setWaitingQueue([])
          } else if (data.type === "MATCH_FINISHED") {
            // 比賽已結束
            console.log("收到比賽結束消息:", data)
            handleMatchFinished(data)
          } else if (data.type === "CANCEL_PENDING_ASSIGNMENT_SUCCESS") {
            // 取消待確認分配成功
            console.log("收到取消待確認分配成功消息:", data)
            const courtId = typeof data.courtId === 'number' ? data.courtId : parseInt(data.courtId)
            
            // 清空場地
            setCourts((prev) =>
              prev.map((c) => 
                c.id === courtId 
                  ? { ...c, players: [], isPending: false, isActive: false }
                  : c
              )
            )
            
            // 清除待確認狀態
            setPendingCourtId(null)
            
            toast.success(data.message || "已取消分配，場地已清空")
          } else if (data.type === "ERROR") {
            // 錯誤消息
            console.error("收到錯誤消息:", data)
            toast.error(data.message || "操作失敗")
          }
        } catch (error) {
          console.error("解析 WebSocket 消息失敗:", error)
        }
      }

      ws.onerror = (error) => {
        console.error("WebSocket 錯誤:", error)
        toast.error("WebSocket 連接錯誤")
        setWsConnected(false)
        setWsConnecting(false)
      }

      ws.onclose = (event) => {
        setWsConnected(false)
        setWsConnecting(false)
        console.log("WebSocket 連接已關閉, code:", event.code, "reason:", event.reason)

        if (event.code === 1000) { // 1000 是正常關閉
          // 正常關閉時不自動重連
          return
        }

        toast.info("WebSocket 連接已關閉，30 秒後自動嘗試重新連線")

        // 異常關閉時啟動 30 秒自動重連機制（如果尚未排程）
        if (!reconnectTimerRef.current) {
          reconnectTimerRef.current = setTimeout(() => {
            reconnectTimerRef.current = null
            console.log("嘗試自動重新連接 WebSocket...")
            connectWebSocket(currentTeam)
          }, 30_000)
        }
      }

      wsRef.current = ws
      console.log("WebSocket 實例已創建並保存到 wsRef")
    } catch (error) {
      console.error("WebSocket 連接失敗:", error)
      toast.error("WebSocket 連接失敗")
      setWsConnecting(false)
    }
  }

  // 断开 WebSocket
  const disconnectWebSocket = () => {
    if (wsRef.current) {
      wsRef.current.close(1000) // 1000 是正常關閉代碼
      wsRef.current = null
    }
    // 主動斷開時也要清除自動重連計時器
    if (reconnectTimerRef.current) {
      clearTimeout(reconnectTimerRef.current)
      reconnectTimerRef.current = null
    }
    setWsConnected(false)
    setWsConnecting(false)
    toast.info("WebSocket 已斷開")
  }

  // 結束叫號：先送 CLEAR_QUEUE（若已連線）、斷線、再導向離開
  const handleEndSession = () => {
    if (isEndingSession) return
    setIsEndingSession(true)
    if (wsRef.current?.readyState === WebSocket.OPEN && team) {
      wsRef.current.send(JSON.stringify({ type: "CLEAR_QUEUE", teamId: team.id }))
    }
    disconnectWebSocket()
    onEndSession?.()
  }

  // 處理隊列更新（由後端發送）
  const handleQueueUpdate = (queue: any[]) => {
    if (!Array.isArray(queue)) {
      console.warn("收到無效的隊列數據:", queue)
      return
    }
    
    // 將後端發送的隊列數據轉換為 UserTeamDto 格式
    // 注意：後端發送 userId, userName, userRealName, userEmail, queueCreatedAt
    // 我們需要從 teamMembers 中查找完整信息，或使用部分信息
    const queueMembers: (UserTeamDto & { queueCreatedAt?: string })[] = queue.map((item: any) => {
      const userId = typeof item.userId === 'number' ? item.userId : parseInt(item.userId)
      // 嘗試從 teamMembers 中查找完整信息
      const fullMember = teamMembers.find(m => m.userId === userId)
      if (fullMember) {
        return {
          ...fullMember,
          queueCreatedAt: item.queueCreatedAt, // 添加隊列創建時間
        }
      }
      // 如果找不到，創建部分信息對象（僅用於顯示）
      return {
        id: userId, // 臨時使用 userId 作為 id
        userId: userId,
        teamId: team?.id || 0,
        userName: item.userName || `用戶${userId}`,
        userRealName: item.userRealName,
        userEmail: item.userEmail,
        queueCreatedAt: item.queueCreatedAt, // 添加隊列創建時間
      } as UserTeamDto & { queueCreatedAt?: string }
    })
    
    console.log("更新等待隊列，共", queueMembers.length, "人")
    setWaitingQueue(queueMembers)
  }

  // 處理場地更新
  const handleCourtUpdate = (courtData: any) => {
    setCourts((prev) =>
      prev.map((court) => {
        if (court.id === courtData.id) {
          return {
            ...courtData,
            matchStartedAt: courtData.matchStartedAt || court.matchStartedAt, // 保留比賽開始時間
          } as Court
        }
        return court
      })
    )
  }

  // 處理玩家分配到場地
  const handlePlayerAssigned = (playerId: number, courtId: number, position: number) => {
    console.log(`handlePlayerAssigned 被調用: playerId=${playerId}, courtId=${courtId}, position=${position}`)
    console.log(`teamMembers 列表:`, teamMembers)
    
    const member = teamMembers.find((m) => m.userId === playerId)
    if (!member) {
      console.error(`找不到 userId=${playerId} 的成員`)
      return
    }
    
    console.log(`找到成員:`, member)

    setCourts((prev) => {
      const updated = prev.map((court) => {
        if (court.id === courtId) {
          const newPlayers = [...court.players]
          // 如果該位置已有玩家，先移除
          const existingIndex = newPlayers.findIndex((p) => p.position === position)
          if (existingIndex >= 0) {
            newPlayers.splice(existingIndex, 1)
          }
          // 添加新玩家
          newPlayers.push({
            userId: member.userId,
            userName: member.userName || `用戶${member.userId}`,
            userRealName: member.userRealName,
            position,
          })
          console.log(`更新場地 ${court.id}, 新玩家列表:`, newPlayers)
          return {
            ...court,
            players: newPlayers,
            isActive: newPlayers.length > 0,
          }
        }
        return court
      })
      console.log(`場地狀態已更新:`, updated)
      return updated
    })

    // 等待隊列將由後端自動更新，無需前端手動管理
  }

  // 處理開始新局
  const handleStartNewGame = () => {
    if (!team || !wsRef.current || wsRef.current.readyState !== WebSocket.OPEN) {
      toast.error("WebSocket 未連接")
      return
    }

    const message = {
      type: "START_NEW_GAME",
      teamId: team.id,
    }

    wsRef.current.send(JSON.stringify(message))
    console.log("發送開始新局請求:", message)
  }

  // 處理恢復狀態
  const handleRestoreState = () => {
    if (!team || !wsRef.current || wsRef.current.readyState !== WebSocket.OPEN) {
      toast.error("WebSocket 未連接")
      return
    }

    // 發送恢復狀態請求
    const message = {
      type: "RESTORE_STATE",
      teamId: team.id,
    }

    wsRef.current.send(JSON.stringify(message))
    console.log("發送恢復狀態請求:", message)
    
    // 後端會自動調用 loadAndSendOngoingMatches
    // 我們只需要等待 RESTORE_ONGOING_MATCHES 消息
  }

  // 將清除隊列函數暴露給父組件
  useEffect(() => {
    const handleClearQueue = () => {
      if (!team || !wsRef.current || wsRef.current.readyState !== WebSocket.OPEN) {
        console.warn("WebSocket 未連接，無法清除隊列")
        return
      }

      // 發送清除隊列請求
      const message = {
        type: "CLEAR_QUEUE",
        teamId: team.id,
      }

      wsRef.current.send(JSON.stringify(message))
      console.log("發送清除隊列請求:", message)
    }

    if (onClearQueueRef) {
      onClearQueueRef.current = handleClearQueue
    }
    return () => {
      if (onClearQueueRef) {
        onClearQueueRef.current = null
      }
    }
  }, [onClearQueueRef, team, wsConnected])

  // 處理玩家從場地移除
  const handlePlayerRemoved = (playerId: number, courtId: number) => {
    const member = teamMembers.find((m) => m.userId === playerId)
    if (!member) return

    setCourts((prev) =>
      prev.map((court) => {
        if (court.id === courtId) {
          const newPlayers = court.players.filter((p) => p.userId !== playerId)
          return {
            ...court,
            players: newPlayers,
            isActive: newPlayers.length > 0,
          }
        }
        return court
      })
    )

    // 等待隊列將由後端自動更新，無需前端手動管理
  }

  // 處理恢復正在進行的比賽
  const handleRestoreOngoingMatches = (data: any) => {
    console.log("處理恢復正在進行的比賽:", data)
    
    const courtsData = data.courts || []
    if (!Array.isArray(courtsData) || courtsData.length === 0) {
      console.log("沒有需要恢復的比賽")
      return
    }

    // 收集所有被分配的用戶ID，用於從等待隊列中移除
    const assignedUserIds = new Set<number>()

    // 更新每個場地的狀態
    setCourts((prev) =>
      prev.map((court) => {
        const courtData = courtsData.find((c: any) => {
          const cId = typeof c.courtId === 'number' ? c.courtId : parseInt(c.courtId)
          return cId === court.id
        })

        if (courtData) {
          const assignments = courtData.assignments || []
          const restoredPlayers: CourtPlayer[] = []

          assignments.forEach((assignment: any) => {
            const userId = typeof assignment.userId === 'number' ? assignment.userId : parseInt(assignment.userId)
            const position = typeof assignment.position === 'number' ? assignment.position : parseInt(assignment.position)

            // 查找成員信息
            let member = teamMembers.find((m) => m.userId === userId)
            
            // 如果找不到，嘗試使用 assignment 中的信息
            if (!member && assignment.userName) {
              // 創建臨時成員對象
              member = {
                userId: userId,
                userName: assignment.userName || `用戶${userId}`,
                userRealName: assignment.userRealName,
              } as UserTeamDto
            }

            if (member) {
              restoredPlayers.push({
                userId: member.userId,
                userName: member.userName || `用戶${member.userId}`,
                userRealName: member.userRealName || assignment.userRealName,
                position: position,
              })
              assignedUserIds.add(userId)
            }
          })

          console.log(`恢復場地 ${court.id} 的比賽，共 ${restoredPlayers.length} 位球員`)
          
          return {
            ...court,
            players: restoredPlayers,
            isActive: restoredPlayers.length > 0,
            isPending: false, // 比賽已開始，不是待確認狀態
            matchStartedAt: courtData.matchStartedAt || court.matchStartedAt, // 恢復比賽開始時間
          }
        }
        return court
      })
    )

    // 等待隊列將由後端自動更新，無需前端手動管理

    toast.success(data.message || `已恢復 ${courtsData.length} 個場地的進行中比賽`)
  }

  // 處理比賽結束（後端廣播的消息，用於同步其他客戶端）
  const handleMatchFinished = (data: any) => {
    console.log("處理比賽結束消息:", data)
    
    const courtId = typeof data.courtId === 'number' ? data.courtId : parseInt(data.courtId)
    const court = courts.find((c) => c.id === courtId)
    
    if (!court) {
      console.warn(`找不到場地: ${courtId}`)
      return
    }

    // 如果場地已經被清空（可能是當前客戶端已經處理過），則跳過
    if (court.players.length === 0 && !court.isActive) {
      console.log(`場地 ${courtId} 已經被清空，跳過處理`)
      return
    }

    // 將場地上的球員放回等待隊列
    const playersToReturn = court.players.map(p => {
      const member = teamMembers.find(m => m.userId === p.userId)
      return member
    }).filter(m => m !== undefined) as UserTeamDto[]

    // 清空場地
    setCourts((prev) =>
      prev.map((c) => 
        c.id === courtId 
          ? { ...c, players: [], isActive: false, isPending: false }
          : c
      )
    )

    // 等待隊列將由後端自動更新，無需前端手動管理

    // 只在不是當前客戶端發起的操作時顯示提示（避免重複提示）
    console.log(`場地 ${court.name} 的比賽已結束（來自其他客戶端或後端確認）`)
  }

  // 手動分配玩家到場地
  const assignPlayerToCourt = (member: UserTeamDto, courtId: number) => {
    const court = courts.find((c) => c.id === courtId)
    if (!court) return

    // 進行中的比賽不允許更改成員
    if (court.isActive && !court.isPending) {
      toast.warning("比賽進行中，無法更改成員")
      return
    }

    // 待確認狀態下允許本地添加（不需要 WebSocket 連接）
    // 非待確認狀態下需要 WebSocket 連接
    if (!court.isPending && !wsConnected) {
      toast.warning("請先重新連線 以使用此功能")
      return
    }

    // 檢查場地是否已滿（4個人）
    if (court.players.length >= 4) {
      toast.warning("場地已滿（最多4人）")
      return
    }

    // 檢查玩家是否已在當前場地
    const isInCurrentCourt = court.players.some((p) => p.userId === member.userId)
    if (isInCurrentCourt) {
      toast.warning("該玩家已在當前場地")
      return
    }

    // 檢查玩家是否已在其他場地
    const isInOtherCourt = courts.some(
      (c) => c.id !== courtId && c.players.some((p) => p.userId === member.userId)
    )
    if (isInOtherCourt) {
      toast.warning("該玩家已在其他場地")
      return
    }

    // 找到下一個可用位置（1-4）
    const usedPositions = court.players.map((p) => p.position)
    let position = 1
    for (let i = 1; i <= 4; i++) {
      if (!usedPositions.includes(i)) {
        position = i
        break
      }
    }

    // 更新本地狀態
    handlePlayerAssigned(member.userId, courtId, position)

    // 如果不在待確認狀態，發送 WebSocket 消息同步到後端
    // 待確認狀態下，人員調整只存在本地，確認開始比賽時會一併發送給後端
    if (!court.isPending && wsRef.current?.readyState === WebSocket.OPEN) {
      wsRef.current.send(
        JSON.stringify({
          type: "ASSIGN_PLAYER",
          playerId: member.userId,
          courtId,
          position,
        })
      )
    } else if (court.isPending) {
      // 待確認狀態下，只更新本地 UI，不發送 WebSocket 消息
      toast.info(`已添加 ${getUserDisplayName(member)} 到場地，確認開始比賽時會同步到後端`)
    }
  }

  // 從場地移除玩家
  const removePlayerFromCourt = (playerId: number, courtId: number) => {
    const court = courts.find((c) => c.id === courtId)
    if (!court) return

    // 進行中的比賽不允許移除成員
    if (court.isActive && !court.isPending) {
      toast.warning("比賽進行中，無法移除成員")
      return
    }

    // 檢查 WebSocket 連接狀態（待確認狀態下允許移除）
    if (!court.isPending && !wsConnected) {
      toast.warning("請先重新連線 以使用此功能")
      return
    }

    handlePlayerRemoved(playerId, courtId)

    // 發送 WebSocket 消息（如果已連接）
    if (wsRef.current?.readyState === WebSocket.OPEN) {
      wsRef.current.send(
        JSON.stringify({
          type: "REMOVE_PLAYER",
          playerId,
          courtId,
        })
      )
    }
  }

  // 自動分配隊列中的玩家到場地（由後端選擇成員）
  const autoAssignToCourt = (courtId: number) => {
    // 檢查 WebSocket 連接狀態
    if (!wsConnected) {
      toast.warning("請先重新連線 以使用此功能")
      return
    }

    const court = courts.find((c) => c.id === courtId)
    if (!court) return

    // 檢查場地是否已滿
    if (court.players.length >= 4) {
      toast.warning("場地已滿（最多4人）")
      return
    }

    // 發送 WebSocket 消息，只傳遞 courtId 和 teamId，由後端選擇成員
    if (wsRef.current?.readyState === WebSocket.OPEN) {
      const message = {
        type: "AUTO_ASSIGN",
        courtId,
        teamId: team?.id,
      }
      console.log(`發送自動分配請求:`, message)
      wsRef.current.send(JSON.stringify(message))
      toast.info(`正在為場地 ${court.name} 自動分配人員...`)
    } else {
      toast.error("WebSocket 未連接")
    }
  }

  // 確認開始比賽
  const confirmStartMatch = (courtId: number) => {
    // 檢查 WebSocket 連接狀態
    if (!wsConnected) {
      toast.warning("請先重新連線 以開始比賽")
      return
    }

    const court = courts.find((c) => c.id === courtId)
    if (!court || !court.isPending) return

    // 檢查是否有足夠的球員（至少4人）
    if (court.players.length < 4) {
      toast.warning("場地需要至少4位球員才能開始比賽")
      return
    }

    // 發送 WebSocket 消息，確認開始比賽，並攜帶最終確認的球員名單
    if (wsRef.current?.readyState === WebSocket.OPEN) {
      const playersPayload = court.players.map((p) => ({
        userId: p.userId,
        position: p.position,
      }))

      const message = {
        type: "CONFIRM_START_MATCH",
        courtId,
        teamId: team?.id,
        players: playersPayload,
      }
      console.log(`發送確認開始比賽請求:`, message)
      wsRef.current.send(JSON.stringify(message))
      
      toast.info(`正在確認開始比賽...`)
    } else {
      toast.error("WebSocket 未連接")
    }
  }

  // 結束比賽
  const finishMatch = (courtId: number) => {
    // 檢查 WebSocket 連接狀態
    if (!wsConnected) {
      toast.warning("請先重新連線 以結束比賽")
      return
    }

    const court = courts.find((c) => c.id === courtId)
    if (!court || !court.isActive) {
      toast.warning("該場地沒有正在進行的比賽")
      return
    }

    // 立即更新UI（樂觀更新）
    const playersToReturn = court.players.map(p => {
      const member = teamMembers.find(m => m.userId === p.userId)
      return member
    }).filter(m => m !== undefined) as UserTeamDto[]

    // 清空場地
    setCourts((prev) =>
      prev.map((c) => 
        c.id === courtId 
          ? { ...c, players: [], isActive: false, isPending: false }
          : c
      )
    )

    // 等待隊列將由後端自動更新，無需前端手動管理

    // 發送 WebSocket 消息到後端
    if (wsRef.current?.readyState === WebSocket.OPEN) {
      const message = {
        type: "FINISH_MATCH",
        courtId,
        teamId: team?.id,
      }
      console.log(`發送結束比賽請求:`, message)
      wsRef.current.send(JSON.stringify(message))
      toast.success(`場地 ${court.name} 的比賽已結束，球員已放回等待隊列`)
    } else {
      toast.error("WebSocket 未連接")
    }
  }

  // 取消待確認的分配（恢復等待隊列）
  const cancelPendingAssignment = (courtId: number) => {
    const court = courts.find((c) => c.id === courtId)
    if (!court || !court.isPending) return

    // 檢查 WebSocket 連接狀態
    if (!wsConnected) {
      toast.warning("請先重新連線 以取消分配")
      return
    }

    // 發送 WebSocket 消息到後端，清空場地
    if (wsRef.current?.readyState === WebSocket.OPEN) {
      const message = {
        type: "CANCEL_PENDING_ASSIGNMENT",
        courtId,
        teamId: team?.id,
      }
      console.log(`發送取消待確認分配請求:`, message)
      wsRef.current.send(JSON.stringify(message))
      
      toast.info("正在取消分配...")
    } else {
      toast.error("WebSocket 未連接")
    }
  }

  // 從待確認的場地移除玩家
  const removePlayerFromPendingCourt = (playerId: number, courtId: number) => {
    const court = courts.find((c) => c.id === courtId)
    if (!court || !court.isPending) return

    const member = teamMembers.find((m) => m.userId === playerId)
    if (!member) return

    // 從場地移除
    setCourts((prev) =>
      prev.map((c) => {
        if (c.id === courtId) {
          const newPlayers = c.players.filter((p) => p.userId !== playerId)
          return {
            ...c,
            players: newPlayers,
          }
        }
        return c
      })
    )

    // 等待隊列將由後端自動更新，無需前端手動管理

    toast.info("已移除人員，可重新分配")
  }

  // 清理 WebSocket 連接
  useEffect(() => {
    return () => {
      if (wsRef.current) {
        wsRef.current.close()
      }
      // 組件卸載時清除自動重連計時器
      if (reconnectTimerRef.current) {
        clearTimeout(reconnectTimerRef.current)
        reconnectTimerRef.current = null
      }
    }
  }, [])

  // 獲取用戶顯示名稱
  const getUserDisplayName = (member: UserTeamDto) => {
    return member.userRealName || member.userName || `用戶 ${member.userId}`
  }

  // 如果未登錄，顯示提示信息
  if (!isAuthenticated && !loading && teamId) {
    return (
      <div className="space-y-6">
        <Card className="border-yellow-500/50 bg-yellow-50/50 dark:bg-yellow-950/20">
          <CardHeader>
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-full bg-yellow-500/10">
                <AlertCircle className="h-6 w-6 text-yellow-600 dark:text-yellow-400" />
              </div>
              <div>
                <CardTitle className="text-yellow-900 dark:text-yellow-100">需要登錄</CardTitle>
                <p className="text-sm text-muted-foreground mt-1">
                  請先登錄以使用叫號系統
                </p>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <p className="text-sm text-muted-foreground">
              只有團隊創建者登錄後才能使用叫號系統功能。
            </p>
          </CardContent>
        </Card>
      </div>
    )
  }

  // 如果未授權，顯示錯誤信息
  if (isAuthorized === false && !loading) {
    return (
      <div className="space-y-6">
        <Card className="border-destructive">
          <CardHeader>
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-full bg-destructive/10">
                <AlertCircle className="h-6 w-6 text-destructive" />
              </div>
              <div>
                <CardTitle className="text-destructive">訪問被拒絕</CardTitle>
                <p className="text-sm text-muted-foreground mt-1">
                  只有團隊創建者才能使用叫號系統
                </p>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <p className="text-sm text-muted-foreground">
              如果您是團隊成員，請聯繫團隊創建者來管理叫號系統。
            </p>
          </CardContent>
        </Card>
      </div>
    )
  }

  // WebSocket 連接中或未連接時的 Loading 狀態
  if (team && isAuthorized && !wsConnected && (wsConnecting || !wsRef.current)) {
    return (
      <div className="space-y-6">
        {/* 团队信息显示区域 */}
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center gap-4">
              <div>
                <p className="text-sm text-muted-foreground">團隊名稱</p>
                <p className="font-semibold">{team.name}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">場地數量</p>
                <p className="font-semibold">{team.courtCount || 0}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">團隊成員</p>
                <p className="font-semibold">{teamMembers.length} 人</p>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* WebSocket 連接中提示 */}
        <Card className="border-blue-500/50 bg-blue-50/50 dark:bg-blue-950/20">
          <CardContent className="p-12">
            <div className="flex flex-col items-center justify-center gap-4">
              <Spinner className="w-12 h-12 text-blue-600 dark:text-blue-400" />
              <div className="text-center">
                <h3 className="text-lg font-semibold text-blue-900 dark:text-blue-100 mb-2">
                  {wsConnecting ? "正在重新連接..." : "等待重新連接"}
                </h3>
                <p className="text-sm text-muted-foreground">
                  {wsConnecting 
                    ? "系統正在建立實時連接，請稍候..." 
                    : "請點擊下方按鈕重新連線 使用叫號系統"}
                </p>
              </div>
              {!wsConnecting && (
                <Button 
                  variant="default" 
                  size="lg"
                  onClick={() => connectWebSocket()}
                  className="mt-4"
                >
                  <Wifi className="w-4 h-4 mr-2" />
                  重新連線
                </Button>
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* 团队信息显示区域 */}
      {team && (
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center gap-4">
              <div>
                <p className="text-sm text-muted-foreground">團隊名稱</p>
                <p className="font-semibold">{team.name}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">場地數量</p>
                <p className="font-semibold">{team.courtCount || 0}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">團隊成員</p>
                <p className="font-semibold">{teamMembers.length} 人</p>
              </div>
              <div className="flex-1" />
              <div className="flex gap-2">
                {wsConnected ? (
                  <>
                    <Badge className="bg-green-500/20 text-green-300 flex items-center gap-1">
                      <Wifi className="w-3 h-3" />
                      已連接
                    </Badge>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={handleEndSession}
                      disabled={isEndingSession}
                    >
                      <WifiOff className="w-4 h-4 mr-2" />
                      {isEndingSession ? "離開中..." : "結束"}
                    </Button>
                  </>
                ) : wsConnecting ? (
                  <Badge className="bg-blue-500/20 text-blue-300 flex items-center gap-1">
                    <Spinner className="w-3 h-3" />
                    連接中...
                  </Badge>
                ) : (
                  <Button variant="outline" size="sm" onClick={() => connectWebSocket()}>
                    <Wifi className="w-4 h-4 mr-2" />
                    重新連線
                  </Button>
                )}
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {/* 场地显示区域 */}
      {team && courts.length > 0 && (
        <div>
          <h2 className="text-xl font-semibold mb-4">場地狀態</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {courts.map((court) => {
              // 获取4个位置的玩家
              const player1 = court.players.find((p) => p.position === 1)
              const player2 = court.players.find((p) => p.position === 2)
              const player3 = court.players.find((p) => p.position === 3)
              const player4 = court.players.find((p) => p.position === 4)

              // 定义同队颜色：位置1和3为左边队伍（蓝色），位置2和4为右边队伍（红色）
              const teamLeftColor = {
                bg: "bg-blue-500/20",
                border: "border-blue-500",
                bgDark: "dark:bg-blue-500/30",
                borderDark: "dark:border-blue-400",
                avatarBorder: "border-blue-500 dark:border-blue-400",
                avatarBg: "bg-blue-500"
              }
              const teamRightColor = {
                bg: "bg-red-500/20",
                border: "border-red-500",
                bgDark: "dark:bg-red-500/30",
                borderDark: "dark:border-red-400",
                avatarBorder: "border-red-500 dark:border-red-400",
                avatarBg: "bg-red-500"
              }

              return (
                <Card
                  key={court.id}
                  className={`overflow-hidden transition-all duration-300 ${
                    court.isActive
                      ? "border-2 border-green-500 shadow-lg shadow-green-500/20"
                      : "border border-border"
                  }`}
                >
                  <CardHeader className="pb-2 pt-3">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <CardTitle className="text-base font-bold">{court.name}</CardTitle>
                        {court.isActive && court.matchStartedAt && (
                          <Badge variant="outline" className="text-xs">
                            {(() => {
                              const startTime = new Date(court.matchStartedAt)
                              const now = new Date()
                              const diffMs = now.getTime() - startTime.getTime()
                              const totalSeconds = Math.floor(diffMs / 1000)
                              const minutes = Math.floor(totalSeconds / 60)
                              const seconds = totalSeconds % 60
                              return `${minutes}:${String(seconds).padStart(2, '0')}`
                            })()}
                          </Badge>
                        )}
                      </div>
                      <Badge
                        className={
                          court.isActive
                            ? "bg-green-500/20 text-green-600 dark:text-green-400 border-green-500/30"
                            : "bg-gray-500/20 text-gray-600 dark:text-gray-400 border-gray-500/30"
                        }
                      >
                        {court.isActive ? "進行中" : "空閒"}
                      </Badge>
                    </div>
                  </CardHeader>
                  <CardContent className="space-y-2 pb-3">
                    {/* 羽毛球场样式 */}
                    <div className="relative w-full aspect-[4/3] bg-gradient-to-b from-green-50 to-green-100 dark:from-green-950/30 dark:to-green-900/20 rounded-lg border-2 border-green-300 dark:border-green-800 overflow-hidden shadow-inner">
                      {/* 場地背景網格 */}
                      <div className="absolute inset-0 opacity-20">
                        <div className="absolute inset-0" style={{
                          backgroundImage: `
                            repeating-linear-gradient(0deg, transparent, transparent 19px, rgba(34, 197, 94, 0.1) 19px, rgba(34, 197, 94, 0.1) 20px),
                            repeating-linear-gradient(90deg, transparent, transparent 19px, rgba(34, 197, 94, 0.1) 19px, rgba(34, 197, 94, 0.1) 20px)
                          `
                        }} />
                      </div>

                      {/* 中线（网子）- 增强视觉效果 */}
                      <div className="absolute left-1/2 top-0 bottom-0 w-1 bg-white dark:bg-white/90 transform -translate-x-1/2 z-20 shadow-[0_0_8px_rgba(0,0,0,0.2)]">
                        {/* 网子纹理效果 */}
                        <div className="absolute inset-0 opacity-30" style={{
                          backgroundImage: `repeating-linear-gradient(
                            90deg,
                            transparent,
                            transparent 2px,
                            rgba(0,0,0,0.1) 2px,
                            rgba(0,0,0,0.1) 4px
                          )`
                        }}></div>
                        {/* 网子顶部和底部的支撑柱效果 */}
                        <div className="absolute top-0 left-1/2 transform -translate-x-1/2 -translate-y-1 w-3 h-2 bg-white dark:bg-white/90 rounded-t-sm shadow-md"></div>
                        <div className="absolute bottom-0 left-1/2 transform -translate-x-1/2 translate-y-1 w-3 h-2 bg-white dark:bg-white/90 rounded-b-sm shadow-md"></div>
                        {/* 网子中间的装饰圆点 */}
                        <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-10 h-10 bg-white dark:bg-white/90 rounded-full border-2 border-green-500 dark:border-green-400 flex items-center justify-center shadow-lg z-30">
                          <div className="w-5 h-5 bg-green-500 dark:bg-green-400 rounded-full"></div>
                        </div>
                      </div>

                      {/* 发球区线 */}
                      <div className="absolute left-1/4 top-0 bottom-0 w-0.5 bg-green-300 dark:bg-green-700 opacity-50 transform -translate-x-1/2"></div>
                      <div className="absolute right-1/4 top-0 bottom-0 w-0.5 bg-green-300 dark:bg-green-700 opacity-50 transform translate-x-1/2"></div>
                      <div className="absolute top-1/3 left-0 right-0 h-0.5 bg-green-300 dark:bg-green-700 opacity-50"></div>
                      <div className="absolute bottom-1/3 left-0 right-0 h-0.5 bg-green-300 dark:bg-green-700 opacity-50"></div>

                      {/* 位置1 - 左上（左边队伍） */}
                      <div className="absolute top-1 left-1 right-1/2 bottom-1/2 flex items-center justify-center p-1">
                        <div
                          className={`w-full h-full rounded-lg border-2 flex flex-col items-center justify-center p-1 transition-all duration-200 ${
                            player1
                              ? court.isPending
                                ? `${teamLeftColor.bg} ${teamLeftColor.bgDark} border-2 border-dashed border-amber-400 dark:border-amber-500 shadow-md cursor-pointer hover:opacity-80 active:scale-95 animate-pulse`
                                : `${teamLeftColor.bg} ${teamLeftColor.border} ${teamLeftColor.bgDark} ${teamLeftColor.borderDark} shadow-md cursor-pointer hover:opacity-80 active:scale-95`
                              : "bg-white/30 border-green-300 dark:bg-green-900/20 dark:border-green-700 border-dashed"
                          }`}
                          onClick={player1 ? () => {
                            // 進行中的比賽不允許移除
                            if (court.isActive && !court.isPending) {
                              toast.warning("比賽進行中，無法移除成員")
                              return
                            }
                            court.isPending ? removePlayerFromPendingCourt(player1.userId, court.id) : removePlayerFromCourt(player1.userId, court.id)
                          } : undefined}
                        >
                          {player1 ? (
                            <>
                              <Avatar className={`h-6 w-6 mb-0.5 border-2 ${teamLeftColor.avatarBorder}`}>
                                <AvatarFallback className={`${teamLeftColor.avatarBg} text-white text-[9px]`}>
                                  {(player1.userRealName || player1.userName).charAt(0).toUpperCase()}
                                </AvatarFallback>
                              </Avatar>
                              <p className="text-[9px] leading-tight font-semibold text-center break-words w-full px-0.5">
                                {player1.userRealName || player1.userName}
                              </p>
                            </>
                          ) : (
                            <div className="text-center">
                              <div className="w-6 h-6 rounded-full bg-green-200 dark:bg-green-800 flex items-center justify-center mx-auto mb-0.5">
                                <span className="text-[10px] font-bold text-green-600 dark:text-green-400">1</span>
                              </div>
                              <p className="text-[9px] text-muted-foreground">空位</p>
                            </div>
                          )}
                        </div>
                      </div>

                      {/* 位置2 - 右上（右边队伍） */}
                      <div className="absolute top-1 right-1 left-1/2 bottom-1/2 flex items-center justify-center p-1">
                        <div
                          className={`w-full h-full rounded-lg border-2 flex flex-col items-center justify-center p-1 transition-all duration-200 ${
                            player2
                              ? court.isPending
                                ? `${teamRightColor.bg} ${teamRightColor.bgDark} border-2 border-dashed border-amber-400 dark:border-amber-500 shadow-md cursor-pointer hover:opacity-80 active:scale-95 animate-pulse`
                                : court.isActive
                                ? `${teamRightColor.bg} ${teamRightColor.border} ${teamRightColor.bgDark} ${teamRightColor.borderDark} shadow-md`
                                : `${teamRightColor.bg} ${teamRightColor.border} ${teamRightColor.bgDark} ${teamRightColor.borderDark} shadow-md cursor-pointer hover:opacity-80 active:scale-95`
                              : "bg-white/30 border-green-300 dark:bg-green-900/20 dark:border-green-700 border-dashed"
                          }`}
                          onClick={player2 ? () => {
                            // 進行中的比賽不允許移除
                            if (court.isActive && !court.isPending) {
                              toast.warning("比賽進行中，無法移除成員")
                              return
                            }
                            court.isPending ? removePlayerFromPendingCourt(player2.userId, court.id) : removePlayerFromCourt(player2.userId, court.id)
                          } : undefined}
                        >
                          {player2 ? (
                            <>
                              <Avatar className={`h-6 w-6 mb-0.5 border-2 ${teamRightColor.avatarBorder}`}>
                                <AvatarFallback className={`${teamRightColor.avatarBg} text-white text-[9px]`}>
                                  {(player2.userRealName || player2.userName).charAt(0).toUpperCase()}
                                </AvatarFallback>
                              </Avatar>
                              <p className="text-[9px] leading-tight font-semibold text-center break-words w-full px-0.5">
                                {player2.userRealName || player2.userName}
                              </p>
                            </>
                          ) : (
                            <div className="text-center">
                              <div className="w-8 h-8 rounded-full bg-green-200 dark:bg-green-800 flex items-center justify-center mx-auto mb-1">
                                <span className="text-xs font-bold text-green-600 dark:text-green-400">2</span>
                              </div>
                              <p className="text-xs text-muted-foreground">空位</p>
                            </div>
                          )}
                        </div>
                      </div>

                      {/* 位置3 - 左下（左边队伍） */}
                      <div className="absolute bottom-1 left-1 right-1/2 top-1/2 flex items-center justify-center p-1">
                        <div
                          className={`w-full h-full rounded-lg border-2 flex flex-col items-center justify-center p-1 transition-all duration-200 ${
                            player3
                              ? court.isPending
                                ? `${teamLeftColor.bg} ${teamLeftColor.bgDark} border-2 border-dashed border-amber-400 dark:border-amber-500 shadow-md cursor-pointer hover:opacity-80 active:scale-95 animate-pulse`
                                : court.isActive
                                ? `${teamLeftColor.bg} ${teamLeftColor.border} ${teamLeftColor.bgDark} ${teamLeftColor.borderDark} shadow-md`
                                : `${teamLeftColor.bg} ${teamLeftColor.border} ${teamLeftColor.bgDark} ${teamLeftColor.borderDark} shadow-md cursor-pointer hover:opacity-80 active:scale-95`
                              : "bg-white/30 border-green-300 dark:bg-green-900/20 dark:border-green-700 border-dashed"
                          }`}
                          onClick={player3 ? () => {
                            // 進行中的比賽不允許移除
                            if (court.isActive && !court.isPending) {
                              toast.warning("比賽進行中，無法移除成員")
                              return
                            }
                            court.isPending ? removePlayerFromPendingCourt(player3.userId, court.id) : removePlayerFromCourt(player3.userId, court.id)
                          } : undefined}
                        >
                          {player3 ? (
                            <>
                              <Avatar className={`h-6 w-6 mb-0.5 border-2 ${teamLeftColor.avatarBorder}`}>
                                <AvatarFallback className={`${teamLeftColor.avatarBg} text-white text-[9px]`}>
                                  {(player3.userRealName || player3.userName).charAt(0).toUpperCase()}
                                </AvatarFallback>
                              </Avatar>
                              <p className="text-[9px] leading-tight font-semibold text-center break-words w-full px-0.5">
                                {player3.userRealName || player3.userName}
                              </p>
                            </>
                          ) : (
                            <div className="text-center">
                              <div className="w-8 h-8 rounded-full bg-green-200 dark:bg-green-800 flex items-center justify-center mx-auto mb-1">
                                <span className="text-xs font-bold text-green-600 dark:text-green-400">3</span>
                              </div>
                              <p className="text-xs text-muted-foreground">空位</p>
                            </div>
                          )}
                        </div>
                      </div>

                      {/* 位置4 - 右下（右边队伍） */}
                      <div className="absolute bottom-1 right-1 left-1/2 top-1/2 flex items-center justify-center p-1">
                        <div
                          className={`w-full h-full rounded-lg border-2 flex flex-col items-center justify-center p-1 transition-all duration-200 ${
                            player4
                              ? court.isPending
                                ? `${teamRightColor.bg} ${teamRightColor.bgDark} border-2 border-dashed border-amber-400 dark:border-amber-500 shadow-md cursor-pointer hover:opacity-80 active:scale-95 animate-pulse`
                                : court.isActive
                                ? `${teamRightColor.bg} ${teamRightColor.border} ${teamRightColor.bgDark} ${teamRightColor.borderDark} shadow-md`
                                : `${teamRightColor.bg} ${teamRightColor.border} ${teamRightColor.bgDark} ${teamRightColor.borderDark} shadow-md cursor-pointer hover:opacity-80 active:scale-95`
                              : "bg-white/30 border-green-300 dark:bg-green-900/20 dark:border-green-700 border-dashed"
                          }`}
                          onClick={player4 ? () => {
                            // 進行中的比賽不允許移除
                            if (court.isActive && !court.isPending) {
                              toast.warning("比賽進行中，無法移除成員")
                              return
                            }
                            court.isPending ? removePlayerFromPendingCourt(player4.userId, court.id) : removePlayerFromCourt(player4.userId, court.id)
                          } : undefined}
                        >
                          {player4 ? (
                            <>
                              <Avatar className={`h-6 w-6 mb-0.5 border-2 ${teamRightColor.avatarBorder}`}>
                                <AvatarFallback className={`${teamRightColor.avatarBg} text-white text-[9px]`}>
                                  {(player4.userRealName || player4.userName).charAt(0).toUpperCase()}
                                </AvatarFallback>
                              </Avatar>
                              <p className="text-[9px] leading-tight font-semibold text-center break-words w-full px-0.5">
                                {player4.userRealName || player4.userName}
                              </p>
                            </>
                          ) : (
                            <div className="text-center">
                              <div className="w-8 h-8 rounded-full bg-green-200 dark:bg-green-800 flex items-center justify-center mx-auto mb-1">
                                <span className="text-xs font-bold text-green-600 dark:text-green-400">4</span>
                              </div>
                              <p className="text-xs text-muted-foreground">空位</p>
                            </div>
                          )}
                        </div>
                      </div>
                    </div>

                    {/* 待確認狀態 - 確認開始比賽 */}
                    {court.isPending && (
                      <div className="pt-2 border-t border-amber-500/50 bg-gradient-to-b from-amber-50/50 to-amber-100/50 dark:from-amber-950/20 dark:to-amber-900/30 -mx-6 px-4 pb-3 mt-2 rounded-b-lg">
                        <div className="space-y-2">
                          <div className="flex items-center justify-center gap-2 py-1">
                            <div className="p-1.5 rounded-full bg-amber-500/20 animate-pulse">
                              <AlertCircle className="w-4 h-4 text-amber-600 dark:text-amber-400" />
                            </div>
                            <div className="text-center">
                              <p className="font-bold text-sm text-amber-900 dark:text-amber-100">
                                人員已分配完成
                              </p>
                              <p className="text-[10px] text-amber-700 dark:text-amber-300">
                                確認後開始比賽，或點擊人員調整位置
                              </p>
                            </div>
                          </div>

                          <div className="flex gap-2">
                            <Button
                              variant="default"
                              size="sm"
                              className="flex-1 bg-green-600 hover:bg-green-700 text-sm font-bold"
                              onClick={() => confirmStartMatch(court.id)}
                            >
                              <Play className="w-4 h-4 mr-1.5" />
                              確認開始比賽
                            </Button>
                            <Button
                              variant="outline"
                              size="sm"
                              className="border-2 border-amber-400 hover:bg-amber-100 dark:border-amber-600 dark:hover:bg-amber-950/50"
                              onClick={() => cancelPendingAssignment(court.id)}
                            >
                              取消
                            </Button>
                          </div>
                        </div>
                      </div>
                    )}

                    {/* 空场地快速自动分配 */}
                    {!court.isPending && court.players.length === 0 && waitingQueue.length > 0 && (
                      <div className="pt-2">
                        <Button
                          variant="default"
                          size="sm"
                          className="w-full"
                          onClick={() => autoAssignToCourt(court.id)}
                        >
                          <Users className="w-3 h-3 mr-1.5" />
                          自動分配 4 位人員開始比賽
                        </Button>
                      </div>
                    )}

                    {/* 比賽進行中 - 結束比賽按鈕 */}
                    {!court.isPending && court.isActive && court.players.length === 4 && (
                      <div className="pt-2 border-t border-red-500/30">
                        <Button
                          variant="destructive"
                          size="sm"
                          className="w-full bg-red-600 hover:bg-red-700 text-sm font-bold"
                          onClick={() => finishMatch(court.id)}
                        >
                          <Square className="w-4 h-4 mr-1.5" />
                          結束比賽
                        </Button>
                      </div>
                    )}

                    {/* 从等待队列分配玩家 - 待確認狀態或非待確認狀態都可以選擇，但進行中的比賽不允許 */}
                    {!court.isActive && waitingQueue.length > 0 && court.players.length < 4 && (
                      <div className="pt-2 border-t">
                        <div className="mb-1.5">
                          <p className="text-[10px] text-muted-foreground font-medium">
                            {court.isPending ? "從等待隊列選擇人員（可調整）" : "從等待隊列分配"}
                          </p>
                        </div>
                        <div className="space-y-1 max-h-24 overflow-y-auto">
                          {waitingQueue
                            .filter((member) => {
                              // 過濾掉已經在場地上的球員
                              return !court.players.some((p) => p.userId === member.userId)
                            })
                            .slice(0, 5)
                            .map((member) => (
                              <Button
                                key={member.userId}
                                variant="outline"
                                size="sm"
                                className="w-full justify-start text-[10px] h-7"
                                onClick={() => assignPlayerToCourt(member, court.id)}
                              >
                                <Play className="w-2.5 h-2.5 mr-1.5" />
                                {getUserDisplayName(member)}
                              </Button>
                            ))}
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

      {/* 等待队列显示区域 */}
      {team && waitingQueue.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Users className="w-5 h-5" />
              排隊等待 ({waitingQueue.length} 人)
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
              {waitingQueue.map((member, index) => {
                // 計算等待時間（秒）
                let waitingSeconds = 0
                let waitingTimeText = ""
                const memberWithQueue = member as UserTeamDto & { queueCreatedAt?: string }
                if (memberWithQueue.queueCreatedAt) {
                  const createdAt = new Date(memberWithQueue.queueCreatedAt)
                  const now = new Date()
                  const diffMs = now.getTime() - createdAt.getTime()
                  waitingSeconds = Math.floor(diffMs / 1000)
                  const hours = Math.floor(waitingSeconds / 3600)
                  const minutes = Math.floor((waitingSeconds % 3600) / 60)
                  const seconds = waitingSeconds % 60
                  if (hours > 0) {
                    waitingTimeText = `${hours}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
                  } else if (minutes > 0) {
                    waitingTimeText = `${minutes}:${String(seconds).padStart(2, '0')}`
                  } else {
                    waitingTimeText = `${seconds}秒`
                  }
                }
                
                // 超過3分鐘（180秒）顯示警告顏色
                const isLongWaiting = waitingSeconds > 180
                
                return (
                  <div
                    key={member.userId}
                    className={`flex items-center gap-3 p-3 rounded-lg border ${
                      isLongWaiting
                        ? "bg-orange-500/20 border-orange-500 border-2"
                        : "bg-secondary border-border"
                    }`}
                  >
                    <Avatar className="h-10 w-10">
                      <AvatarFallback>
                        {getUserDisplayName(member)
                          .charAt(0)
                          .toUpperCase()}
                      </AvatarFallback>
                    </Avatar>
                    <div className="flex-1">
                      <p className="font-medium">{getUserDisplayName(member)}</p>
                      <p className="text-xs text-muted-foreground">{member.userName}</p>
                      {waitingTimeText && (
                        <p className={`text-xs mt-0.5 ${isLongWaiting ? "text-orange-600 dark:text-orange-400 font-semibold" : "text-muted-foreground"}`}>
                          等待: {waitingTimeText}
                        </p>
                      )}
                    </div>
                    {index === 0 ? (
                      <Badge className="bg-green-500/20 text-green-600 dark:text-green-400 animate-pulse">
                        下一個
                      </Badge>
                    ) : (
                      <Badge variant="outline" className="text-xs">
                        #{index + 1}
                      </Badge>
                    )}
                  </div>
                )
              })}
            </div>
          </CardContent>
        </Card>
      )}

      {/* 遊戲狀態選擇對話框 */}
      <Dialog open={showGameStateDialog} onOpenChange={setShowGameStateDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>請選擇</DialogTitle>
            <DialogDescription>
              {gameStateData?.hasOngoingMatches
                ? `檢測到 ${gameStateData.ongoingCourtsCount} 個場地有進行中的比賽，請選擇：`
                : "沒有進行中的比賽，可以開始新的一局"}
            </DialogDescription>
          </DialogHeader>
          <DialogFooter className="flex-col sm:flex-row gap-2">
            {gameStateData?.hasOngoingMatches && (
              <Button
                variant="outline"
                onClick={() => {
                  handleRestoreState()
                }}
                className="w-full sm:w-auto"
              >
                恢復上次狀態
              </Button>
            )}
            <Button
              onClick={() => {
                handleStartNewGame()
              }}
              className="w-full sm:w-auto"
            >
              開始新的一局
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* 空状态 */}
      {team && waitingQueue.length === 0 && (
        <Card>
          <CardContent className="py-12 text-center">
            <Users className="w-12 h-12 mx-auto mb-4 text-muted-foreground" />
            <p className="text-muted-foreground">目前沒有等待中的人員</p>
          </CardContent>
        </Card>
      )}
    </div>
  )
}
