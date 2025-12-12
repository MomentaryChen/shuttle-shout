"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { UserTeamOverview } from "@/components/user-team-overview"
import { SidebarNavigation } from "@/components/sidebar-navigation"
import { SidebarProvider, SidebarInset, SidebarTrigger } from "@/components/ui/sidebar"
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { RegisterForm } from "@/components/register-form"
import { useAuth } from "@/contexts/AuthContext"
import { Loader2, UserPlus, LogIn } from "lucide-react"

export default function Page() {
  const { isLoading, isAuthenticated } = useAuth()
  const router = useRouter()
  const [showRegisterDialog, setShowRegisterDialog] = useState(false)

  // 移除自动跳转逻辑，允许已登录和未登录用户都可以查看团队总览

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    )
  }

  const handleRegisterSuccess = () => {
    setShowRegisterDialog(false)
    // 延遲一下讓AuthContext狀態更新完成
    // 註冊成功後返回當前頁面（首頁）
    setTimeout(() => {
      router.refresh()
    }, 300)
  }

  return (
    <SidebarProvider>
      <SidebarNavigation />
      <SidebarInset>
        {/* Header */}
        <header className="flex h-16 shrink-0 items-center gap-2 border-b px-4">
          <SidebarTrigger className="-ml-1" />
          <div className="flex items-center gap-2 flex-1">
            <h1 className="text-xl font-semibold">團隊總覽</h1>
            {!isAuthenticated && (
              <div className="ml-auto flex items-center gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => {
                    // 保存當前頁面路徑，以便登錄成功後返回
                    if (typeof window !== "undefined") {
                      const currentPath = window.location.pathname + window.location.search
                      router.push(`/login?returnTo=${encodeURIComponent(currentPath)}`)
                    } else {
                      router.push("/login")
                    }
                  }}
                  className="flex items-center gap-2"
                >
                  <LogIn className="h-4 w-4" />
                  登錄
                </Button>
                <Button
                  size="sm"
                  onClick={() => setShowRegisterDialog(true)}
                  className="flex items-center gap-2 bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-800"
                >
                  <UserPlus className="h-4 w-4" />
                  加入團隊
                </Button>
              </div>
            )}
          </div>
        </header>

        {/* Main Content */}
        <main className="flex-1">
          <div className="container mx-auto px-6 py-8">
            {!isAuthenticated && (
              <div className="mb-6 p-4 bg-gradient-to-r from-blue-50 to-blue-100/50 dark:from-blue-950/20 dark:to-blue-900/10 border border-blue-200 dark:border-blue-800 rounded-lg">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-lg font-semibold text-blue-900 dark:text-blue-100 mb-1">
                      歡迎來到羽球叫號系統！
                    </h3>
                    <p className="text-sm text-blue-700 dark:text-blue-300">
                      註冊帳號以加入團隊並開始使用系統功能
                    </p>
                  </div>
                  <Button
                    onClick={() => setShowRegisterDialog(true)}
                    className="flex items-center gap-2 bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-800"
                  >
                    <UserPlus className="h-4 w-4" />
                    立即註冊
                  </Button>
                </div>
              </div>
            )}
            <UserTeamOverview />
          </div>
        </main>
      </SidebarInset>

      {/* 注册对话框 */}
      <Dialog open={showRegisterDialog} onOpenChange={setShowRegisterDialog}>
        <DialogContent className="max-w-lg max-h-[90vh] overflow-y-auto sm:max-w-[600px]">
          <DialogHeader>
            <DialogTitle>加入團隊</DialogTitle>
            <DialogDescription>
              請註冊帳號以加入團隊並使用系統功能
            </DialogDescription>
          </DialogHeader>
          <RegisterForm
            onSuccess={handleRegisterSuccess}
            onCancel={() => setShowRegisterDialog(false)}
          />
        </DialogContent>
      </Dialog>
    </SidebarProvider>
  )
}
