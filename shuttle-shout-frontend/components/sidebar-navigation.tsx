"use client"

import { useEffect, useState } from "react"
import { useRouter, usePathname } from "next/navigation"
import { useAuth } from "@/contexts/AuthContext"
import { ResourcePageDto } from "@/types/api"
import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarTrigger,
} from "@/components/ui/sidebar"
import { Badge } from "@/components/ui/badge"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"
import { Separator } from "@/components/ui/separator"
import { Spinner } from "@/components/ui/spinner"
import {
  Users,
  MapPin,
  Clock,
  Trophy,
  BarChart,
  Settings,
  Home,
  LogOut,
  User,
  ChevronDown,
} from "lucide-react"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"

// 图标映射 - 将字符串映射到 Lucide React 图标组件
const iconMap: Record<string, React.ComponentType<{ className?: string }>> = {
  Users,
  MapPin,
  Clock,
  Trophy,
  BarChart,
  Settings,
  Home,
}

export function SidebarNavigation() {
  const { user, logout, isLoading: authLoading, accessiblePages, isAuthenticated } = useAuth()
  const router = useRouter()
  const pathname = usePathname()

  const handleLogout = async () => {
    try {
      await logout()
      // 使用 replace 而不是 push，避免浏览器历史记录问题
      // 由于 logout 函数会同步更新状态，isAuthenticated 会立即变为 false
      router.replace("/")
    } catch (error) {
      console.error("登出失败:", error)
      // 即使登出API失败，也清除本地状态并跳转
      router.replace("/")
    }
  }

  const handlePageClick = (page: ResourcePageDto) => {
    router.push(page.path)
  }

  // 只在未登录时添加默认的团队总览页面
  // 已登录用户应该从角色权限中获取团队总览页面
  const defaultPages: ResourcePageDto[] = !isAuthenticated ? [
    {
      id: 0,
      name: "團隊總覽",
      code: "TEAM_OVERVIEW",
      path: "/",
      description: "查看所有團隊的總覽資訊",
      icon: "Home",
      sortOrder: 0,
    },
  ] : []

  // 合并默认页面和用户可访问的页面，并去重（基于 code）
  const allPagesMap = new Map<string, ResourcePageDto>()
  
  // 先添加默认页面（仅未登录用户）
  defaultPages.forEach(page => {
    allPagesMap.set(page.code, page)
  })
  
  // 再添加用户可访问的页面（会覆盖默认页面，避免重复）
  accessiblePages.forEach(page => {
    allPagesMap.set(page.code, page)
  })
  
  // 转换为数组并排序
  const allPages = Array.from(allPagesMap.values()).sort((a, b) => {
    // 团队总览排在第一位
    if (a.code === "TEAM_OVERVIEW") return -1
    if (b.code === "TEAM_OVERVIEW") return 1
    // 其他页面按 sortOrder 排序
    return (a.sortOrder || 0) - (b.sortOrder || 0)
  })

  // 获取页面对应的图标组件
  const getIconComponent = (iconName?: string) => {
    if (!iconName) return Home
    return iconMap[iconName] || Home
  }

  return (
    <Sidebar>
      <SidebarHeader className="border-b border-sidebar-border">
        <div className="flex items-center gap-2 px-2 py-2">
          <div className="flex aspect-square size-8 items-center justify-center rounded-lg bg-sidebar-primary text-sidebar-primary-foreground">
            <Home className="size-4" />
          </div>
          <div className="grid flex-1 text-left text-sm leading-tight">
            <span className="truncate font-semibold">羽球叫號系統</span>
            <span className="truncate text-xs text-sidebar-foreground/70">
              專業場地管理系統
            </span>
          </div>
        </div>
      </SidebarHeader>

      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupLabel>導航菜單</SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              {allPages.map((page) => {
                const IconComponent = getIconComponent(page.icon)
                const isActive = pathname === page.path

                return (
                  <SidebarMenuItem key={page.id}>
                    <SidebarMenuButton
                      onClick={() => handlePageClick(page)}
                      isActive={isActive}
                      tooltip={page.name}
                    >
                      <IconComponent className="size-4" />
                      <span>{page.name}</span>
                    </SidebarMenuButton>
                  </SidebarMenuItem>
                )
              })}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>

        {/* 用户信息区域或登入按钮 */}
        {user ? (
          <>
            <Separator />
            <SidebarGroup>
              <SidebarGroupContent>
                <SidebarMenu>
                  <SidebarMenuItem>
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <SidebarMenuButton
                          size="lg"
                          className="data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground"
                        >
                          <Avatar className="h-8 w-8 rounded-lg">
                            <AvatarFallback className="rounded-lg bg-sidebar-primary text-sidebar-primary-foreground">
                              {user.realName?.charAt(0) || user.username?.charAt(0) || "U"}
                            </AvatarFallback>
                          </Avatar>
                          <div className="grid flex-1 text-left text-sm leading-tight">
                            <span className="truncate font-semibold">
                              {user.realName || user.username}
                            </span>
                            <span className="truncate text-xs text-sidebar-foreground/70">
                              {user.email || user.username}
                            </span>
                          </div>
                          <ChevronDown className="ml-auto size-4" />
                        </SidebarMenuButton>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent
                        className="w-[--radix-dropdown-menu-trigger-width] min-w-56 rounded-lg"
                        side="bottom"
                        align="end"
                        sideOffset={4}
                      >
                        <DropdownMenuLabel className="p-0 font-normal">
                          <div className="flex items-center gap-2 px-1 py-1.5 text-left text-sm">
                            <Avatar className="h-8 w-8 rounded-lg">
                              <AvatarFallback className="rounded-lg bg-sidebar-primary text-sidebar-primary-foreground">
                                {user.realName?.charAt(0) || user.username?.charAt(0) || "U"}
                              </AvatarFallback>
                            </Avatar>
                            <div className="grid flex-1 text-left text-sm leading-tight">
                              <span className="truncate font-semibold">
                                {user.realName || user.username}
                              </span>
                              <span className="truncate text-xs text-sidebar-foreground/70">
                                {user.email || user.username}
                              </span>
                            </div>
                          </div>
                        </DropdownMenuLabel>
                        <DropdownMenuSeparator />
                        <DropdownMenuItem onClick={() => router.push("/profile")}>
                          <User className="mr-2 h-4 w-4" />
                          個人資料
                        </DropdownMenuItem>
                        <DropdownMenuSeparator />
                        <DropdownMenuItem onClick={handleLogout}>
                          <LogOut className="mr-2 h-4 w-4" />
                          登出
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </SidebarMenuItem>
                </SidebarMenu>
              </SidebarGroupContent>
            </SidebarGroup>
          </>
        ) : (
          <>
            <Separator />
            <SidebarGroup>
              <SidebarGroupContent>
                <SidebarMenu>
                  <SidebarMenuItem>
                    <SidebarMenuButton
                      onClick={() => {
                        // 保存當前頁面路徑，以便登錄成功後返回
                        if (typeof window !== "undefined") {
                          const currentPath = window.location.pathname + window.location.search
                          router.push(`/login?returnTo=${encodeURIComponent(currentPath)}`)
                        } else {
                          router.push("/login")
                        }
                      }}
                      tooltip="登入系統"
                    >
                      <User className="size-4" />
                      <span>登入</span>
                    </SidebarMenuButton>
                  </SidebarMenuItem>
                </SidebarMenu>
              </SidebarGroupContent>
            </SidebarGroup>
          </>
        )}
      </SidebarContent>
    </Sidebar>
  )
}
