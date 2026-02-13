import { CourtManagement } from "@/components/court-management"
import { SidebarNavigation } from "@/components/sidebar-navigation"
import { SidebarProvider, SidebarInset, SidebarTrigger } from "@/components/ui/sidebar"

export default function Page() {
  return (
    <SidebarProvider>
      <SidebarNavigation />
      <SidebarInset>
        <header className="flex h-16 shrink-0 items-center gap-2 border-b px-4">
          <SidebarTrigger className="-ml-1" />
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-semibold">球場管理</h1>
          </div>
        </header>
        <main className="flex-1">
          <div className="container mx-auto px-6 py-8">
            <CourtManagement />
          </div>
        </main>
      </SidebarInset>
    </SidebarProvider>
  )
}
