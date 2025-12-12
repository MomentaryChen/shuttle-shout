"use client"

import { useEffect } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import { useAuth } from "@/contexts/AuthContext"
import { LoginForm } from "@/components/login-form"
import { Loader2, ArrowLeft, Home } from "lucide-react"
import { Button } from "@/components/ui/button"

export default function Page() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const { isAuthenticated, isLoading } = useAuth()

  // 获取返回页面路径
  const getReturnPath = () => {
    if (typeof window === "undefined") return "/"
    
    // 优先从 URL 参数获取
    const returnTo = searchParams?.get("returnTo")
    if (returnTo) return returnTo

    // 其次从 sessionStorage 获取
    const storedPath = sessionStorage.getItem("loginReturnPath")
    if (storedPath) return storedPath

    // 默认返回首页
    return "/"
  }

  // 页面加载时，如果有 returnTo 参数，保存到 sessionStorage
  useEffect(() => {
    if (typeof window !== "undefined") {
      const returnTo = searchParams?.get("returnTo")
      if (returnTo) {
        sessionStorage.setItem("loginReturnPath", returnTo)
      } else {
        // 如果没有 returnTo 参数，检查是否有 referrer
        const referrer = document.referrer
        if (referrer && !referrer.includes("/login")) {
          try {
            const referrerUrl = new URL(referrer)
            const referrerPath = referrerUrl.pathname + referrerUrl.search
            if (referrerPath !== "/login") {
              sessionStorage.setItem("loginReturnPath", referrerPath)
            }
          } catch (e) {
            // 忽略 URL 解析错误
          }
        }
      }
    }
  }, [searchParams])

  useEffect(() => {
    if (!isLoading && isAuthenticated) {
      // 使用 replace 避免瀏覽器歷史記錄問題
      const returnPath = getReturnPath()
      // 清除保存的路徑
      if (typeof window !== "undefined") {
        sessionStorage.removeItem("loginReturnPath")
      }
      router.replace(returnPath)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAuthenticated, isLoading, router])

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    )
  }

  // 如果已登录，显示加载状态并等待跳转，而不是返回 null
  if (isAuthenticated) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background">
        <div className="flex flex-col items-center gap-4">
          <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
          <p className="text-sm text-muted-foreground">正在跳转...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-background to-muted/20 p-4">
      <div className="w-full max-w-md relative">
        {/* 返回首頁按鈕 - 放在頁面頂部 */}
        <Button
          type="button"
          variant="ghost"
          size="sm"
          className="absolute -top-12 left-0 text-muted-foreground hover:text-foreground hover:bg-muted/50 transition-all"
          onClick={() => {
            // 清除保存的路徑，直接返回首頁
            if (typeof window !== "undefined") {
              sessionStorage.removeItem("loginReturnPath")
              sessionStorage.removeItem("registerReturnPath")
            }
            router.push("/")
          }}
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          <span className="font-medium">返回首頁</span>
        </Button>
        
        <div className="mb-8 text-center">
          <h1 className="text-4xl font-bold mb-2">羽球叫號系統</h1>
          <p className="text-muted-foreground">ShuttleShout</p>
        </div>
        <LoginForm />
      </div>
    </div>
  )
}

