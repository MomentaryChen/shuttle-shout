"use client"

import { useEffect } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/contexts/AuthContext"
import { UserTeamOverview } from "@/components/user-team-overview"
import { SidebarNavigation } from "@/components/sidebar-navigation"
import { SidebarProvider, SidebarInset, SidebarTrigger } from "@/components/ui/sidebar"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Loader2, Home, Users, MapPin, Activity, User } from "lucide-react"

export default function Page() {
  const { user, logout, isLoading, accessiblePages } = useAuth()
  const router = useRouter()

  useEffect(() => {
    // 如果用户未登录，跳转到首页（团队总览）而不是登录页
    // 因为未登录用户也可以查看团队总览
    if (!isLoading && !user) {
      router.replace('/')
    }
  }, [user, isLoading, router])

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    )
  }

  if (!user) {
    return null
  }

  return (
    <SidebarProvider>
      <SidebarNavigation />
      <SidebarInset>
        {/* Header */}
        <header className="flex h-16 shrink-0 items-center gap-2 border-b px-4">
          <SidebarTrigger className="-ml-1" />
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-semibold">儀表板</h1>
          </div>
        </header>

        {/* Main Content */}
        <main className="flex-1">
          <div className="container mx-auto px-6 py-8">
          {/* Welcome Section */}
          <div className="mb-8">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-2xl font-bold text-foreground mb-2">
                  歡迎回來，{user?.realName || user?.username}！
                </h2>
                <p className="text-muted-foreground">
                  以下是您當前的系統總覽
                </p>
              </div>
              <div className="flex gap-2">
                {accessiblePages.some(page => page.code === 'PERSONNEL_MANAGEMENT') && (
                  <Button
                    variant="outline"
                    onClick={() => router.push('/personnel-management')}
                    className="flex items-center gap-2"
                  >
                    <Users className="h-4 w-4" />
                    人員管理
                  </Button>
                )}
              </div>
            </div>
          </div>

          {/* Quick Stats Cards */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            <Card className="bg-gradient-to-br from-blue-50 to-blue-100/50 dark:from-blue-950/20 dark:to-blue-900/10 border-blue-200 dark:border-blue-800">
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">用戶狀態</CardTitle>
                <Activity className="h-4 w-4 text-blue-600" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-blue-900 dark:text-blue-100">
                  已登入
                </div>
                <p className="text-xs text-blue-700 dark:text-blue-300">
                  系統運行正常
                </p>
              </CardContent>
            </Card>

            <Card className="bg-gradient-to-br from-green-50 to-green-100/50 dark:from-green-950/20 dark:to-green-900/10 border-green-200 dark:border-green-800">
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">可訪問頁面</CardTitle>
                <Home className="h-4 w-4 text-green-600" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-green-900 dark:text-green-100">
                  {accessiblePages.length}
                </div>
                <p className="text-xs text-green-700 dark:text-green-300">
                  個功能頁面
                </p>
              </CardContent>
            </Card>

            <Card className="bg-gradient-to-br from-purple-50 to-purple-100/50 dark:from-purple-950/20 dark:to-purple-900/10 border-purple-200 dark:border-purple-800">
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">系統角色</CardTitle>
                <User className="h-4 w-4 text-purple-600" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-purple-900 dark:text-purple-100">
                  {user?.roles?.length || 0}
                </div>
                <p className="text-xs text-purple-700 dark:text-purple-300">
                  個系統角色
                </p>
              </CardContent>
            </Card>
          </div>

          {/* Quick Access */}
          <div className="mb-8">
            <h3 className="text-lg font-semibold mb-4">快速訪問</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {accessiblePages.map((page) => (
                <Card key={page.id} className="hover:shadow-md transition-shadow cursor-pointer" onClick={() => {
                  // Map page codes to routes
                  const routeMap: { [key: string]: string } = {
                    'PERSONNEL_MANAGEMENT': '/personnel-management',
                    'TEAM_MANAGEMENT': '/team-management',
                    'COURT_MANAGEMENT': '/court-management',
                  }
                  const route = routeMap[page.code]
                  if (route) router.push(route)
                }}>
                  <CardHeader className="pb-3">
                    <div className="flex items-center justify-between">
                      <CardTitle className="text-base">{page.name}</CardTitle>
                      <Badge variant="secondary" className="text-xs">
                        {page.code}
                      </Badge>
                    </div>
                    <CardDescription className="text-sm">
                      {page.description || '無描述'}
                    </CardDescription>
                  </CardHeader>
                </Card>
              ))}
            </div>
          </div>

          {/* Team Overview */}
          <UserTeamOverview />
          </div>
        </main>
      </SidebarInset>
    </SidebarProvider>
  )
}