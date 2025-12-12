"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import * as z from "zod"
import { useAuth } from "@/contexts/AuthContext"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { RegisterForm } from "@/components/register-form"
import { Loader2, UserPlus } from "lucide-react"

const loginSchema = z.object({
  username: z.string().min(1, "請輸入用戶名"),
  password: z.string().min(1, "請輸入密碼"),
})

type LoginFormValues = z.infer<typeof loginSchema>

export function LoginForm() {
  const router = useRouter()
  const { login } = useAuth()
  const [error, setError] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [showRegisterDialog, setShowRegisterDialog] = useState(false)

  // 獲取返回頁面路徑（從 URL 參數或 sessionStorage）
  const getReturnPath = () => {
    if (typeof window === "undefined") return "/"
    
    // 優先從 URL 參數獲取
    const params = new URLSearchParams(window.location.search)
    const returnTo = params.get("returnTo")
    if (returnTo) return returnTo

    // 其次從 sessionStorage 獲取登錄返回路徑
    const loginReturnPath = sessionStorage.getItem("loginReturnPath")
    if (loginReturnPath) return loginReturnPath

    // 再次從 sessionStorage 獲取註冊返回路徑
    const registerReturnPath = sessionStorage.getItem("registerReturnPath")
    if (registerReturnPath) return registerReturnPath

    // 默認返回首頁
    return "/"
  }

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
  })

  const onSubmit = async (data: LoginFormValues) => {
    setError(null)
    setIsLoading(true)

    try {
      await login(data.username, data.password)
      // 等待狀態更新完成後再跳轉，避免白屏
      // 注意：不在這裡設置 setIsLoading(false)，讓頁面跳轉處理加載狀態
      const returnPath = getReturnPath()
      setTimeout(() => {
        // 清除保存的路徑
        if (typeof window !== "undefined") {
          sessionStorage.removeItem("loginReturnPath")
          sessionStorage.removeItem("registerReturnPath")
        }
        router.replace(returnPath)
        router.refresh()
      }, 150)
    } catch (err: any) {
      setError(err.message || "登錄失敗，請檢查用戶名和密碼")
      setIsLoading(false)
    }
  }

  return (
    <Card className="w-full max-w-md">
      <CardHeader className="space-y-1">
        <CardTitle className="text-2xl font-bold text-center">用戶登錄</CardTitle>
        <CardDescription className="text-center">
          請輸入您的用戶名和密碼以登錄系統
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          {error && (
            <Alert variant="destructive">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          <div className="space-y-2">
            <Label htmlFor="username">用戶名</Label>
            <Input
              id="username"
              type="text"
              placeholder="請輸入用戶名"
              {...register("username")}
              disabled={isLoading}
              autoComplete="username"
            />
            {errors.username && (
              <p className="text-sm text-destructive">{errors.username.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="password">密碼</Label>
            <Input
              id="password"
              type="password"
              placeholder="請輸入密碼"
              {...register("password")}
              disabled={isLoading}
              autoComplete="current-password"
            />
            {errors.password && (
              <p className="text-sm text-destructive">{errors.password.message}</p>
            )}
          </div>

          <Button type="submit" className="w-full" disabled={isLoading}>
            {isLoading ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                登錄中...
              </>
            ) : (
              "登錄"
            )}
          </Button>
        </form>
      </CardContent>
      <CardFooter className="flex flex-col space-y-3">
        <div className="relative w-full">
          <div className="absolute inset-0 flex items-center">
            <span className="w-full border-t" />
          </div>
          <div className="relative flex justify-center text-xs uppercase">
            <span className="bg-card px-2 text-muted-foreground">或</span>
          </div>
        </div>
        <Button
          type="button"
          variant="outline"
          className="w-full"
          onClick={() => {
            // 保存當前頁面路徑，以便註冊成功後返回
            if (typeof window !== "undefined") {
              const currentPath = window.location.pathname + window.location.search
              // 如果不是登錄頁面本身，保存路徑
              if (currentPath !== "/login") {
                sessionStorage.setItem("registerReturnPath", currentPath)
              } else {
                // 如果是登錄頁面，檢查是否有 returnTo 參數
                const params = new URLSearchParams(window.location.search)
                const returnTo = params.get("returnTo")
                if (returnTo) {
                  sessionStorage.setItem("registerReturnPath", returnTo)
                } else {
                  // 默認返回首頁
                  sessionStorage.setItem("registerReturnPath", "/")
                }
              }
            }
            setShowRegisterDialog(true)
          }}
          disabled={isLoading}
        >
          <UserPlus className="mr-2 h-4 w-4" />
          註冊新帳號
        </Button>
        <p className="text-sm text-muted-foreground text-center">
          羽球叫號系統 - ShuttleShout
        </p>
      </CardFooter>

      {/* 註冊對話框 */}
      <Dialog open={showRegisterDialog} onOpenChange={setShowRegisterDialog}>
        <DialogContent className="max-w-lg max-h-[90vh] overflow-y-auto sm:max-w-[600px]">
          <DialogHeader>
            <DialogTitle>註冊新帳號</DialogTitle>
            <DialogDescription>
              請填寫以下信息以創建新帳號
            </DialogDescription>
          </DialogHeader>
          <RegisterForm
            onSuccess={() => {
              setShowRegisterDialog(false)
              // 註冊成功後會自動登錄，延遲一下讓狀態更新完成
              const returnPath = getReturnPath()
              setTimeout(() => {
                // 清除保存的路徑
                if (typeof window !== "undefined") {
                  sessionStorage.removeItem("registerReturnPath")
                }
                router.replace(returnPath)
                router.refresh()
              }, 300)
            }}
            onCancel={() => setShowRegisterDialog(false)}
          />
        </DialogContent>
      </Dialog>
    </Card>
  )
}

