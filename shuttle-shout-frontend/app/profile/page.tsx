"use client"

import { useEffect } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/contexts/AuthContext"
import { ProfileForm } from "@/components/profile-form"
import { SidebarNavigation } from "@/components/sidebar-navigation"
import { SidebarProvider, SidebarInset, SidebarTrigger } from "@/components/ui/sidebar"
import { Loader2 } from "lucide-react"

export default function ProfilePage() {
  const { user, isLoading } = useAuth()
  const router = useRouter()

  useEffect(() => {
    // 如果用户未登录，跳转到登录页
    if (!isLoading && !user) {
      router.replace("/login")
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
            <h1 className="text-xl font-semibold">個人資料</h1>
          </div>
        </header>

        {/* Main Content */}
        <main className="flex-1 p-6">
          <ProfileForm />
        </main>
      </SidebarInset>
    </SidebarProvider>
  )
}

