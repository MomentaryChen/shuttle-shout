import { SystemSettings } from "@/components/system-settings"
import { SidebarNavigation } from "@/components/sidebar-navigation"
import { SidebarProvider, SidebarInset, SidebarTrigger } from "@/components/ui/sidebar"

export default function Page() {
  return (
    <SidebarProvider>
      <SidebarNavigation />
      <SidebarInset>
        {/* Header */}
        <header className="flex h-16 shrink-0 items-center gap-2 border-b px-4">
          <SidebarTrigger className="-ml-1" />
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-semibold">系統設置</h1>
          </div>
        </header>

        {/* Main Content */}
        <main className="flex-1">
          <div className="container mx-auto px-6 py-8">
            <SystemSettings />
          </div>
        </main>
      </SidebarInset>
    </SidebarProvider>
  )
}
