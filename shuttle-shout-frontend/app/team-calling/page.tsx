"use client"

import { Suspense } from "react"
import { useRouter } from "next/navigation"
import { TeamCallingSystem } from "@/components/team-calling-system"
import { SidebarNavigation } from "@/components/sidebar-navigation"
import { SidebarProvider, SidebarInset, SidebarTrigger } from "@/components/ui/sidebar"
import { Spinner } from "@/components/ui/spinner"
import { Button } from "@/components/ui/button"
import { ArrowLeft } from "lucide-react"

function TeamCallingContent() {
  return <TeamCallingSystem />
}

export default function Page() {
  const router = useRouter()

  return (
    <SidebarProvider>
      <SidebarNavigation />
      <SidebarInset>
        {/* Header */}
        <header className="flex h-16 shrink-0 items-center gap-2 border-b px-4">
          <SidebarTrigger className="-ml-1" />
          <Button
            variant="ghost"
            size="sm"
            onClick={() => router.back()}
            className="mr-2"
          >
            <ArrowLeft className="h-4 w-4 mr-2" />
            返回
          </Button>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-semibold">團隊叫號系統</h1>
          </div>
        </header>

        {/* Main Content */}
        <main className="flex-1">
          <div className="container mx-auto px-6 py-8">
            <Suspense
              fallback={
                <div className="flex items-center justify-center py-12">
                  <div className="flex flex-col items-center gap-4">
                    <Spinner className="w-8 h-8" />
                    <p className="text-muted-foreground">載入中...</p>
                  </div>
                </div>
              }
            >
              <TeamCallingContent />
            </Suspense>
          </div>
        </main>
      </SidebarInset>
    </SidebarProvider>
  )
}
