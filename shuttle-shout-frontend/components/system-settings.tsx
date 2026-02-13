"use client"

import { useEffect, useState } from "react"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Switch } from "@/components/ui/switch"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Badge } from "@/components/ui/badge"
import { authApi } from "@/lib/api"
import { useAuth } from "@/contexts/AuthContext"
import { UserDto } from "@/types/api"
import { Spinner } from "@/components/ui/spinner"
import { toast } from "sonner"
import {
  Settings,
  Shield,
  Bell,
  Database,
  Mail,
  Globe,
  Key,
  Save,
  RefreshCw,
  AlertTriangle,
  Activity,
  CheckCircle2,
  Clock,
  Server,
  Zap,
  TrendingUp,
  Users,
  Lock
} from "lucide-react"

const PAGE_CODE_SYSTEM_SETTINGS = "SYSTEM_SETTINGS"

export function SystemSettings() {
  const { user: currentUser, isLoading: authLoading, hasPageAccess } = useAuth()
  const hasPermission = hasPageAccess(PAGE_CODE_SYSTEM_SETTINGS)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [activeTab, setActiveTab] = useState("general")

  // 系统设置状态
  const [settings, setSettings] = useState({
    // 通用设置
    systemName: "Shuttle Shout 羽毛球管理系统",
    systemDescription: "专业的羽毛球场馆管理系统",
    contactEmail: "admin@shuttleshout.com",
    timezone: "Asia/Shanghai",

    // 安全设置
    sessionTimeout: 30, // 分钟
    passwordMinLength: 8,
    enableTwoFactor: false,
    loginAttemptsLimit: 5,

    // 通知设置
    enableEmailNotifications: true,
    enablePushNotifications: false,
    maintenanceMode: false,
    maintenanceMessage: "",

    // API设置
    apiRateLimit: 1000, // 请求/分钟
    enableApiLogging: true,

    // 数据库设置
    dbBackupFrequency: "daily",
    enableAutoBackup: true
  })

  // 系统统计信息（模拟数据）
  const [stats] = useState({
    totalUsers: 156,
    activeSessions: 23,
    apiRequestsToday: 12450,
    systemUptime: "99.9%",
    lastBackup: "2小时前",
    dbSize: "2.4 GB"
  })

  useEffect(() => {
    if (hasPermission && !authLoading) {
      loadSettings().finally(() => setLoading(false))
    } else if (!authLoading) {
      setLoading(false)
    }
  }, [hasPermission, authLoading])

  const loadSettings = async () => {
    // 模拟API调用加载设置
    try {
      await new Promise(resolve => setTimeout(resolve, 800))
      // 这里应该从API获取真实设置数据
      // 暂时使用默认值
    } catch (error) {
      console.error("獲取系統設置失敗:", error)
      toast.error("獲取系統設置失敗")
    }
  }

  const saveSettings = async () => {
    try {
      setSaving(true)
      // 模拟保存设置
      await new Promise(resolve => setTimeout(resolve, 1500))

      toast.success("系統設置已保存")
    } catch (error) {
      console.error("保存系統設置失敗:", error)
      toast.error("保存系統設置失敗")
    } finally {
      setSaving(false)
    }
  }

  const handleSettingChange = (key: string, value: any) => {
    setSettings(prev => ({
      ...prev,
      [key]: value
    }))
  }

  // 權限與載入：依後端可存取頁面判斷（管理員已含全部頁面）
  if (authLoading || (loading && hasPermission)) {
    return (
      <div className="w-full min-h-[400px] flex items-center justify-center">
        <div className="flex flex-col items-center gap-4">
          <Spinner className="w-8 h-8 text-blue-600 dark:text-primary" />
          <p className="text-muted-foreground font-medium">正在載入...</p>
        </div>
      </div>
    )
  }
  if (!hasPermission) {
    return (
      <div className="w-full min-h-[400px] flex items-center justify-center">
        <Card className="w-full max-w-md">
          <CardContent className="py-16">
            <div className="text-center space-y-4">
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-red-100 dark:bg-red-900/20 mb-4">
                <Shield className="h-8 w-8 text-red-500 dark:text-red-400" />
              </div>
              <div>
                <h3 className="text-xl font-semibold text-foreground mb-2">
                  權限不足
                </h3>
                <p className="text-muted-foreground">
                  只有管理員才能訪問此頁面
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    )
  }

  return (
    <div className="space-y-6 animate-in fade-in duration-500">
      {/* 页面标题和操作栏 */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div className="space-y-1">
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-lg bg-gradient-to-br from-blue-500 to-blue-600 dark:from-blue-600 dark:to-blue-700 shadow-lg">
              <Settings className="h-6 w-6 text-white" />
            </div>
            <div>
              <h2 className="text-3xl font-bold bg-gradient-to-r from-blue-600 to-blue-800 dark:from-blue-400 dark:to-blue-600 bg-clip-text text-transparent">
                系統設置
              </h2>
              <p className="text-sm text-muted-foreground mt-1">
                配置系統參數和設置，管理系統運行狀態
              </p>
            </div>
          </div>
        </div>
        <div className="flex items-center gap-3">
          <Button
            onClick={() => checkPermissionAndLoadSettings()}
            variant="outline"
            size="sm"
            className="hover:bg-blue-50 dark:hover:bg-blue-950 transition-all duration-200"
          >
            <RefreshCw className="h-4 w-4 mr-2" />
            重新載入
          </Button>
          <Button
            onClick={saveSettings}
            disabled={saving}
            size="sm"
            className="bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-800 shadow-lg hover:shadow-xl transition-all duration-200"
          >
            {saving ? (
              <>
                <Spinner className="h-4 w-4 mr-2" />
                保存中...
              </>
            ) : (
              <>
                <Save className="h-4 w-4 mr-2" />
                保存設置
              </>
            )}
          </Button>
        </div>
      </div>

      {/* 系统状态统计卡片 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <Card className="relative overflow-hidden border-blue-200 dark:border-blue-800 bg-gradient-to-br from-blue-50 to-white dark:from-blue-950/50 dark:to-blue-900/20 hover:shadow-lg transition-all duration-300 group">
          <div className="absolute top-0 right-0 w-32 h-32 bg-blue-200 dark:bg-blue-800 rounded-full -mr-16 -mt-16 opacity-20 group-hover:opacity-30 transition-opacity"></div>
          <CardContent className="p-6 relative">
            <div className="flex items-center justify-between">
              <div className="space-y-2">
                <p className="text-sm font-medium text-muted-foreground">總用戶數</p>
                <p className="text-3xl font-bold text-blue-600 dark:text-blue-400">{stats.totalUsers}</p>
                <div className="flex items-center gap-1 text-xs text-green-600 dark:text-green-400">
                  <TrendingUp className="h-3 w-3" />
                  <span>較上月 +12%</span>
                </div>
              </div>
              <div className="p-3 rounded-full bg-blue-100 dark:bg-blue-900/50">
                <Users className="h-6 w-6 text-blue-600 dark:text-blue-400" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="relative overflow-hidden border-green-200 dark:border-green-800 bg-gradient-to-br from-green-50 to-white dark:from-green-950/50 dark:to-green-900/20 hover:shadow-lg transition-all duration-300 group">
          <div className="absolute top-0 right-0 w-32 h-32 bg-green-200 dark:bg-green-800 rounded-full -mr-16 -mt-16 opacity-20 group-hover:opacity-30 transition-opacity"></div>
          <CardContent className="p-6 relative">
            <div className="flex items-center justify-between">
              <div className="space-y-2">
                <p className="text-sm font-medium text-muted-foreground">活躍會話</p>
                <p className="text-3xl font-bold text-green-600 dark:text-green-400">{stats.activeSessions}</p>
                <div className="flex items-center gap-1 text-xs text-muted-foreground">
                  <Activity className="h-3 w-3" />
                  <span>實時監控</span>
                </div>
              </div>
              <div className="p-3 rounded-full bg-green-100 dark:bg-green-900/50">
                <Activity className="h-6 w-6 text-green-600 dark:text-green-400" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="relative overflow-hidden border-purple-200 dark:border-purple-800 bg-gradient-to-br from-purple-50 to-white dark:from-purple-950/50 dark:to-purple-900/20 hover:shadow-lg transition-all duration-300 group">
          <div className="absolute top-0 right-0 w-32 h-32 bg-purple-200 dark:bg-purple-800 rounded-full -mr-16 -mt-16 opacity-20 group-hover:opacity-30 transition-opacity"></div>
          <CardContent className="p-6 relative">
            <div className="flex items-center justify-between">
              <div className="space-y-2">
                <p className="text-sm font-medium text-muted-foreground">今日API請求</p>
                <p className="text-3xl font-bold text-purple-600 dark:text-purple-400">{stats.apiRequestsToday.toLocaleString()}</p>
                <div className="flex items-center gap-1 text-xs text-muted-foreground">
                  <Zap className="h-3 w-3" />
                  <span>平均 {Math.round(stats.apiRequestsToday / 24)} 次/小時</span>
                </div>
              </div>
              <div className="p-3 rounded-full bg-purple-100 dark:bg-purple-900/50">
                <Zap className="h-6 w-6 text-purple-600 dark:text-purple-400" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="relative overflow-hidden border-orange-200 dark:border-orange-800 bg-gradient-to-br from-orange-50 to-white dark:from-orange-950/50 dark:to-orange-900/20 hover:shadow-lg transition-all duration-300 group">
          <div className="absolute top-0 right-0 w-32 h-32 bg-orange-200 dark:bg-orange-800 rounded-full -mr-16 -mt-16 opacity-20 group-hover:opacity-30 transition-opacity"></div>
          <CardContent className="p-6 relative">
            <div className="flex items-center justify-between">
              <div className="space-y-2">
                <p className="text-sm font-medium text-muted-foreground">系統可用性</p>
                <p className="text-3xl font-bold text-orange-600 dark:text-orange-400">{stats.systemUptime}</p>
                <div className="flex items-center gap-1 text-xs text-green-600 dark:text-green-400">
                  <CheckCircle2 className="h-3 w-3" />
                  <span>運行正常</span>
                </div>
              </div>
              <div className="p-3 rounded-full bg-orange-100 dark:bg-orange-900/50">
                <Server className="h-6 w-6 text-orange-600 dark:text-orange-400" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="relative overflow-hidden border-cyan-200 dark:border-cyan-800 bg-gradient-to-br from-cyan-50 to-white dark:from-cyan-950/50 dark:to-cyan-900/20 hover:shadow-lg transition-all duration-300 group">
          <div className="absolute top-0 right-0 w-32 h-32 bg-cyan-200 dark:bg-cyan-800 rounded-full -mr-16 -mt-16 opacity-20 group-hover:opacity-30 transition-opacity"></div>
          <CardContent className="p-6 relative">
            <div className="flex items-center justify-between">
              <div className="space-y-2">
                <p className="text-sm font-medium text-muted-foreground">最後備份</p>
                <p className="text-2xl font-bold text-cyan-600 dark:text-cyan-400">{stats.lastBackup}</p>
                <div className="flex items-center gap-1 text-xs text-muted-foreground">
                  <Clock className="h-3 w-3" />
                  <span>自動備份已啟用</span>
                </div>
              </div>
              <div className="p-3 rounded-full bg-cyan-100 dark:bg-cyan-900/50">
                <Database className="h-6 w-6 text-cyan-600 dark:text-cyan-400" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="relative overflow-hidden border-indigo-200 dark:border-indigo-800 bg-gradient-to-br from-indigo-50 to-white dark:from-indigo-950/50 dark:to-indigo-900/20 hover:shadow-lg transition-all duration-300 group">
          <div className="absolute top-0 right-0 w-32 h-32 bg-indigo-200 dark:bg-indigo-800 rounded-full -mr-16 -mt-16 opacity-20 group-hover:opacity-30 transition-opacity"></div>
          <CardContent className="p-6 relative">
            <div className="flex items-center justify-between">
              <div className="space-y-2">
                <p className="text-sm font-medium text-muted-foreground">數據庫大小</p>
                <p className="text-3xl font-bold text-indigo-600 dark:text-indigo-400">{stats.dbSize}</p>
                <div className="flex items-center gap-1 text-xs text-muted-foreground">
                  <Database className="h-3 w-3" />
                  <span>健康狀態良好</span>
                </div>
              </div>
              <div className="p-3 rounded-full bg-indigo-100 dark:bg-indigo-900/50">
                <Database className="h-6 w-6 text-indigo-600 dark:text-indigo-400" />
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* 设置标签页 */}
      <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-6">
        <TabsList className="grid w-full grid-cols-2 md:grid-cols-5 gap-2 bg-muted/50 p-1.5 rounded-xl">
          <TabsTrigger 
            value="general" 
            className="flex items-center gap-2 data-[state=active]:bg-white dark:data-[state=active]:bg-gray-800 data-[state=active]:shadow-md transition-all duration-200"
          >
            <Settings className="h-4 w-4" />
            <span className="hidden sm:inline">通用設置</span>
            <span className="sm:hidden">通用</span>
          </TabsTrigger>
          <TabsTrigger 
            value="security" 
            className="flex items-center gap-2 data-[state=active]:bg-white dark:data-[state=active]:bg-gray-800 data-[state=active]:shadow-md transition-all duration-200"
          >
            <Shield className="h-4 w-4" />
            <span className="hidden sm:inline">安全設置</span>
            <span className="sm:hidden">安全</span>
          </TabsTrigger>
          <TabsTrigger 
            value="notifications" 
            className="flex items-center gap-2 data-[state=active]:bg-white dark:data-[state=active]:bg-gray-800 data-[state=active]:shadow-md transition-all duration-200"
          >
            <Bell className="h-4 w-4" />
            <span className="hidden sm:inline">通知設置</span>
            <span className="sm:hidden">通知</span>
          </TabsTrigger>
          <TabsTrigger 
            value="api" 
            className="flex items-center gap-2 data-[state=active]:bg-white dark:data-[state=active]:bg-gray-800 data-[state=active]:shadow-md transition-all duration-200"
          >
            <Key className="h-4 w-4" />
            <span className="hidden sm:inline">API設置</span>
            <span className="sm:hidden">API</span>
          </TabsTrigger>
          <TabsTrigger 
            value="database" 
            className="flex items-center gap-2 data-[state=active]:bg-white dark:data-[state=active]:bg-gray-800 data-[state=active]:shadow-md transition-all duration-200"
          >
            <Database className="h-4 w-4" />
            <span className="hidden sm:inline">數據庫</span>
            <span className="sm:hidden">數據</span>
          </TabsTrigger>
        </TabsList>

        {/* 通用设置 */}
        <TabsContent value="general" className="space-y-4 animate-in fade-in-50 slide-in-from-left-4 duration-300">
          <Card className="border-blue-200 dark:border-blue-800 shadow-lg hover:shadow-xl transition-all duration-300">
            <CardHeader className="pb-4">
              <div className="flex items-center gap-3">
                <div className="p-2 rounded-lg bg-blue-100 dark:bg-blue-900/50">
                  <Globe className="h-5 w-5 text-blue-600 dark:text-blue-400" />
                </div>
                <div>
                  <CardTitle className="text-xl">基本信息</CardTitle>
                  <CardDescription className="mt-1">配置系統的基本信息和顯示設置</CardDescription>
                </div>
              </div>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2 group">
                  <Label htmlFor="systemName" className="text-sm font-semibold flex items-center gap-2">
                    <span>系統名稱</span>
                    <Badge variant="outline" className="text-xs">必填</Badge>
                  </Label>
                  <Input
                    id="systemName"
                    value={settings.systemName}
                    onChange={(e) => handleSettingChange("systemName", e.target.value)}
                    className="transition-all duration-200 focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    placeholder="輸入系統名稱"
                  />
                </div>
                <div className="space-y-2 group">
                  <Label htmlFor="contactEmail" className="text-sm font-semibold flex items-center gap-2">
                    <Mail className="h-3 w-3" />
                    <span>聯繫郵箱</span>
                  </Label>
                  <Input
                    id="contactEmail"
                    type="email"
                    value={settings.contactEmail}
                    onChange={(e) => handleSettingChange("contactEmail", e.target.value)}
                    className="transition-all duration-200 focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    placeholder="admin@example.com"
                  />
                </div>
              </div>
              <div className="space-y-2">
                <Label htmlFor="systemDescription" className="text-sm font-semibold">系統描述</Label>
                <Textarea
                  id="systemDescription"
                  value={settings.systemDescription}
                  onChange={(e) => handleSettingChange("systemDescription", e.target.value)}
                  rows={4}
                  className="transition-all duration-200 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 resize-none"
                  placeholder="輸入系統描述信息..."
                />
                <p className="text-xs text-muted-foreground">此描述將顯示在登錄頁面和系統首頁</p>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2">
                  <Label htmlFor="timezone" className="text-sm font-semibold flex items-center gap-2">
                    <Clock className="h-3 w-3" />
                    <span>時區設置</span>
                  </Label>
                  <Select value={settings.timezone} onValueChange={(value) => handleSettingChange("timezone", value)}>
                    <SelectTrigger className="transition-all duration-200 focus:ring-2 focus:ring-blue-500">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="Asia/Shanghai">🌏 亞洲/上海 (UTC+8)</SelectItem>
                      <SelectItem value="Asia/Tokyo">🇯🇵 亞洲/東京 (UTC+9)</SelectItem>
                      <SelectItem value="America/New_York">🇺🇸 美洲/紐約 (UTC-5)</SelectItem>
                      <SelectItem value="Europe/London">🇬🇧 歐洲/倫敦 (UTC+0)</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* 安全设置 */}
        <TabsContent value="security" className="space-y-4 animate-in fade-in-50 slide-in-from-left-4 duration-300">
          <Card className="border-red-200 dark:border-red-800 shadow-lg hover:shadow-xl transition-all duration-300">
            <CardHeader className="pb-4">
              <div className="flex items-center gap-3">
                <div className="p-2 rounded-lg bg-red-100 dark:bg-red-900/50">
                  <Shield className="h-5 w-5 text-red-600 dark:text-red-400" />
                </div>
                <div>
                  <CardTitle className="text-xl">安全配置</CardTitle>
                  <CardDescription className="mt-1">保護系統安全的重要設置</CardDescription>
                </div>
              </div>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2">
                  <Label htmlFor="sessionTimeout" className="text-sm font-semibold flex items-center gap-2">
                    <Clock className="h-3 w-3" />
                    <span>會話超時時間</span>
                  </Label>
                  <div className="relative">
                    <Input
                      id="sessionTimeout"
                      type="number"
                      min="5"
                      max="480"
                      value={settings.sessionTimeout}
                      onChange={(e) => handleSettingChange("sessionTimeout", parseInt(e.target.value))}
                      className="transition-all duration-200 focus:ring-2 focus:ring-red-500 focus:border-red-500"
                    />
                    <span className="absolute right-3 top-1/2 -translate-y-1/2 text-sm text-muted-foreground">分鐘</span>
                  </div>
                  <p className="text-xs text-muted-foreground">用戶無操作後自動登出的時間</p>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="passwordMinLength" className="text-sm font-semibold flex items-center gap-2">
                    <Lock className="h-3 w-3" />
                    <span>密碼最小長度</span>
                  </Label>
                  <Input
                    id="passwordMinLength"
                    type="number"
                    min="6"
                    max="32"
                    value={settings.passwordMinLength}
                    onChange={(e) => handleSettingChange("passwordMinLength", parseInt(e.target.value))}
                    className="transition-all duration-200 focus:ring-2 focus:ring-red-500 focus:border-red-500"
                  />
                  <p className="text-xs text-muted-foreground">建議設置為 8 位或以上</p>
                </div>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2">
                  <Label htmlFor="loginAttemptsLimit" className="text-sm font-semibold flex items-center gap-2">
                    <AlertTriangle className="h-3 w-3" />
                    <span>登錄嘗試次數限制</span>
                  </Label>
                  <Input
                    id="loginAttemptsLimit"
                    type="number"
                    min="3"
                    max="20"
                    value={settings.loginAttemptsLimit}
                    onChange={(e) => handleSettingChange("loginAttemptsLimit", parseInt(e.target.value))}
                    className="transition-all duration-200 focus:ring-2 focus:ring-red-500 focus:border-red-500"
                  />
                  <p className="text-xs text-muted-foreground">超過此次數將暫時鎖定帳號</p>
                </div>
                <div className="flex items-center justify-between p-5 border-2 rounded-xl bg-gradient-to-br from-red-50 to-white dark:from-red-950/20 dark:to-transparent hover:border-red-300 dark:hover:border-red-700 transition-all duration-200">
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <Label className="text-sm font-semibold">啟用兩因素認證</Label>
                      {settings.enableTwoFactor && (
                        <Badge variant="default" className="bg-green-500 text-xs">已啟用</Badge>
                      )}
                    </div>
                    <p className="text-sm text-muted-foreground">為管理員帳號啟用額外的安全驗證</p>
                  </div>
                  <Switch
                    checked={settings.enableTwoFactor}
                    onCheckedChange={(checked) => handleSettingChange("enableTwoFactor", checked)}
                    className="data-[state=checked]:bg-red-600"
                  />
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* 通知设置 */}
        <TabsContent value="notifications" className="space-y-4 animate-in fade-in-50 slide-in-from-left-4 duration-300">
          <Card className="border-yellow-200 dark:border-yellow-800 shadow-lg hover:shadow-xl transition-all duration-300">
            <CardHeader className="pb-4">
              <div className="flex items-center gap-3">
                <div className="p-2 rounded-lg bg-yellow-100 dark:bg-yellow-900/50">
                  <Bell className="h-5 w-5 text-yellow-600 dark:text-yellow-400" />
                </div>
                <div>
                  <CardTitle className="text-xl">通知配置</CardTitle>
                  <CardDescription className="mt-1">管理系統通知和提醒設置</CardDescription>
                </div>
              </div>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between p-5 border-2 rounded-xl bg-gradient-to-br from-yellow-50 to-white dark:from-yellow-950/20 dark:to-transparent hover:border-yellow-300 dark:hover:border-yellow-700 transition-all duration-200">
                <div className="space-y-1 flex-1">
                  <div className="flex items-center gap-2">
                    <Mail className="h-4 w-4 text-yellow-600 dark:text-yellow-400" />
                    <Label className="text-sm font-semibold">啟用郵件通知</Label>
                    {settings.enableEmailNotifications && (
                      <Badge variant="default" className="bg-green-500 text-xs">已啟用</Badge>
                    )}
                  </div>
                  <p className="text-sm text-muted-foreground">發送系統通知和提醒郵件</p>
                </div>
                <Switch
                  checked={settings.enableEmailNotifications}
                  onCheckedChange={(checked) => handleSettingChange("enableEmailNotifications", checked)}
                  className="data-[state=checked]:bg-yellow-600"
                />
              </div>
              <div className="flex items-center justify-between p-5 border-2 rounded-xl bg-gradient-to-br from-yellow-50 to-white dark:from-yellow-950/20 dark:to-transparent hover:border-yellow-300 dark:hover:border-yellow-700 transition-all duration-200">
                <div className="space-y-1 flex-1">
                  <div className="flex items-center gap-2">
                    <Bell className="h-4 w-4 text-yellow-600 dark:text-yellow-400" />
                    <Label className="text-sm font-semibold">啟用推送通知</Label>
                    {settings.enablePushNotifications && (
                      <Badge variant="default" className="bg-green-500 text-xs">已啟用</Badge>
                    )}
                  </div>
                  <p className="text-sm text-muted-foreground">在瀏覽器中顯示推送通知</p>
                </div>
                <Switch
                  checked={settings.enablePushNotifications}
                  onCheckedChange={(checked) => handleSettingChange("enablePushNotifications", checked)}
                  className="data-[state=checked]:bg-yellow-600"
                />
              </div>
            </CardContent>
          </Card>

          <Card className="border-orange-200 dark:border-orange-800 shadow-lg hover:shadow-xl transition-all duration-300">
            <CardHeader className="pb-4">
              <div className="flex items-center gap-3">
                <div className="p-2 rounded-lg bg-orange-100 dark:bg-orange-900/50">
                  <AlertTriangle className="h-5 w-5 text-orange-600 dark:text-orange-400" />
                </div>
                <div>
                  <CardTitle className="text-xl">維護模式</CardTitle>
                  <CardDescription className="mt-1">臨時關閉系統進行維護</CardDescription>
                </div>
              </div>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between p-5 border-2 rounded-xl bg-gradient-to-br from-orange-50 to-white dark:from-orange-950/20 dark:to-transparent hover:border-orange-300 dark:hover:border-orange-700 transition-all duration-200">
                <div className="space-y-1 flex-1">
                  <div className="flex items-center gap-2">
                    <AlertTriangle className="h-4 w-4 text-orange-600 dark:text-orange-400" />
                    <Label className="text-sm font-semibold">啟用維護模式</Label>
                    {settings.maintenanceMode && (
                      <Badge variant="destructive" className="text-xs">維護中</Badge>
                    )}
                  </div>
                  <p className="text-sm text-muted-foreground">啟用後，普通用戶將無法訪問系統</p>
                </div>
                <Switch
                  checked={settings.maintenanceMode}
                  onCheckedChange={(checked) => handleSettingChange("maintenanceMode", checked)}
                  className="data-[state=checked]:bg-orange-600"
                />
              </div>
              {settings.maintenanceMode && (
                <div className="space-y-2 p-4 bg-orange-50 dark:bg-orange-950/30 rounded-xl border border-orange-200 dark:border-orange-800 animate-in fade-in-50 slide-in-from-top-2 duration-300">
                  <Label htmlFor="maintenanceMessage" className="text-sm font-semibold">維護通知消息</Label>
                  <Textarea
                    id="maintenanceMessage"
                    placeholder="請輸入維護期間顯示給用戶的消息..."
                    value={settings.maintenanceMessage}
                    onChange={(e) => handleSettingChange("maintenanceMessage", e.target.value)}
                    rows={4}
                    className="transition-all duration-200 focus:ring-2 focus:ring-orange-500 focus:border-orange-500 resize-none"
                  />
                  <p className="text-xs text-muted-foreground">此消息將顯示在維護頁面上</p>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        {/* API设置 */}
        <TabsContent value="api" className="space-y-4 animate-in fade-in-50 slide-in-from-left-4 duration-300">
          <Card className="border-purple-200 dark:border-purple-800 shadow-lg hover:shadow-xl transition-all duration-300">
            <CardHeader className="pb-4">
              <div className="flex items-center gap-3">
                <div className="p-2 rounded-lg bg-purple-100 dark:bg-purple-900/50">
                  <Key className="h-5 w-5 text-purple-600 dark:text-purple-400" />
                </div>
                <div>
                  <CardTitle className="text-xl">API 配置</CardTitle>
                  <CardDescription className="mt-1">管理 API 訪問限制和日誌記錄</CardDescription>
                </div>
              </div>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2">
                  <Label htmlFor="apiRateLimit" className="text-sm font-semibold flex items-center gap-2">
                    <Zap className="h-3 w-3" />
                    <span>API 請求限制</span>
                  </Label>
                  <div className="relative">
                    <Input
                      id="apiRateLimit"
                      type="number"
                      min="10"
                      max="10000"
                      value={settings.apiRateLimit}
                      onChange={(e) => handleSettingChange("apiRateLimit", parseInt(e.target.value))}
                      className="transition-all duration-200 focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
                    />
                    <span className="absolute right-3 top-1/2 -translate-y-1/2 text-sm text-muted-foreground">次/分鐘</span>
                  </div>
                  <p className="text-xs text-muted-foreground">單個 IP 地址每分鐘允許的最大請求數</p>
                </div>
                <div className="flex items-center justify-between p-5 border-2 rounded-xl bg-gradient-to-br from-purple-50 to-white dark:from-purple-950/20 dark:to-transparent hover:border-purple-300 dark:hover:border-purple-700 transition-all duration-200">
                  <div className="space-y-1 flex-1">
                    <div className="flex items-center gap-2">
                      <Activity className="h-4 w-4 text-purple-600 dark:text-purple-400" />
                      <Label className="text-sm font-semibold">啟用 API 日誌記錄</Label>
                      {settings.enableApiLogging && (
                        <Badge variant="default" className="bg-green-500 text-xs">已啟用</Badge>
                      )}
                    </div>
                    <p className="text-sm text-muted-foreground">記錄所有 API 請求和響應，用於調試和審計</p>
                  </div>
                  <Switch
                    checked={settings.enableApiLogging}
                    onCheckedChange={(checked) => handleSettingChange("enableApiLogging", checked)}
                    className="data-[state=checked]:bg-purple-600"
                  />
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* 数据库设置 */}
        <TabsContent value="database" className="space-y-4 animate-in fade-in-50 slide-in-from-left-4 duration-300">
          <Card className="border-green-200 dark:border-green-800 shadow-lg hover:shadow-xl transition-all duration-300">
            <CardHeader className="pb-4">
              <div className="flex items-center gap-3">
                <div className="p-2 rounded-lg bg-green-100 dark:bg-green-900/50">
                  <Database className="h-5 w-5 text-green-600 dark:text-green-400" />
                </div>
                <div>
                  <CardTitle className="text-xl">數據庫配置</CardTitle>
                  <CardDescription className="mt-1">管理數據庫備份和維護設置</CardDescription>
                </div>
              </div>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2">
                  <Label htmlFor="dbBackupFrequency" className="text-sm font-semibold flex items-center gap-2">
                    <Clock className="h-3 w-3" />
                    <span>備份頻率</span>
                  </Label>
                  <Select value={settings.dbBackupFrequency} onValueChange={(value) => handleSettingChange("dbBackupFrequency", value)}>
                    <SelectTrigger className="transition-all duration-200 focus:ring-2 focus:ring-green-500">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="hourly">⏰ 每小時</SelectItem>
                      <SelectItem value="daily">📅 每日</SelectItem>
                      <SelectItem value="weekly">📆 每週</SelectItem>
                      <SelectItem value="monthly">🗓️ 每月</SelectItem>
                    </SelectContent>
                  </Select>
                  <p className="text-xs text-muted-foreground">選擇數據庫自動備份的頻率</p>
                </div>
                <div className="flex items-center justify-between p-5 border-2 rounded-xl bg-gradient-to-br from-green-50 to-white dark:from-green-950/20 dark:to-transparent hover:border-green-300 dark:hover:border-green-700 transition-all duration-200">
                  <div className="space-y-1 flex-1">
                    <div className="flex items-center gap-2">
                      <Database className="h-4 w-4 text-green-600 dark:text-green-400" />
                      <Label className="text-sm font-semibold">啟用自動備份</Label>
                      {settings.enableAutoBackup && (
                        <Badge variant="default" className="bg-green-500 text-xs">已啟用</Badge>
                      )}
                    </div>
                    <p className="text-sm text-muted-foreground">自動備份數據庫數據，確保數據安全</p>
                  </div>
                  <Switch
                    checked={settings.enableAutoBackup}
                    onCheckedChange={(checked) => handleSettingChange("enableAutoBackup", checked)}
                    className="data-[state=checked]:bg-green-600"
                  />
                </div>
              </div>
              {settings.enableAutoBackup && (
                <div className="p-4 bg-green-50 dark:bg-green-950/30 rounded-xl border border-green-200 dark:border-green-800 animate-in fade-in-50 slide-in-from-top-2 duration-300">
                  <div className="flex items-start gap-3">
                    <CheckCircle2 className="h-5 w-5 text-green-600 dark:text-green-400 mt-0.5" />
                    <div className="space-y-1 flex-1">
                      <p className="text-sm font-semibold text-green-900 dark:text-green-100">自動備份已啟用</p>
                      <p className="text-xs text-muted-foreground">
                        系統將按照設定的頻率自動備份數據庫。最後一次備份時間：{stats.lastBackup}
                      </p>
                    </div>
                  </div>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}
