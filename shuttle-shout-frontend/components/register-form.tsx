"use client"

import { useState } from "react"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import * as z from "zod"
import { userApi, authApi } from "@/lib/api"
import { useAuth } from "@/contexts/AuthContext"
import { LoginResponse } from "@/types/api"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Loader2, UserPlus, Mail, Phone, User } from "lucide-react"
import { toast } from "sonner"

const registerSchema = z.object({
  username: z.string().min(3, "用戶名至少3個字符").max(50, "用戶名最多50個字符"),
  password: z.string().min(6, "密碼至少6個字符"),
  confirmPassword: z.string().min(6, "請確認密碼"),
  email: z.string().email("郵箱格式不正確").optional().or(z.literal("")),
  phoneNumber: z.string().optional(),
  realName: z.string().optional(),
}).refine((data) => data.password === data.confirmPassword, {
  message: "兩次輸入的密碼不一致",
  path: ["confirmPassword"],
})

type RegisterFormValues = z.infer<typeof registerSchema>

interface RegisterFormProps {
  onSuccess?: () => void
  onCancel?: () => void
}

export function RegisterForm({ onSuccess, onCancel }: RegisterFormProps) {
  const { setLoginState } = useAuth()
  const [error, setError] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
  })

  const onSubmit = async (data: RegisterFormValues) => {
    setError(null)
    setIsLoading(true)

    try {
      // 註冊用戶（註冊API返回UserDto，不包含token）
      const { confirmPassword, ...userData } = data
      const registeredUser = await userApi.register({
        username: userData.username,
        password: userData.password,
        email: userData.email || undefined,
        phoneNumber: userData.phoneNumber || undefined,
        realName: userData.realName || undefined,
      })

      // 註冊成功後，使用註冊時的用戶名和密碼自動登錄
      if (registeredUser && registeredUser.id) {
        try {
          const loginResponse: LoginResponse = await authApi.login({
            username: userData.username,
            password: userData.password,
          })

          // 登錄成功後更新AuthContext狀態
          if (loginResponse.token && loginResponse.user) {
            await setLoginState({
              token: loginResponse.token,
              user: loginResponse.user,
            })
            toast.success("註冊成功！已自動登錄")
            onSuccess?.()
          } else {
            throw new Error("登錄響應數據不完整")
          }
        } catch (loginErr: any) {
          // 如果自動登錄失敗，只顯示註冊成功，提示用戶手動登錄
          console.error("自動登錄失敗:", loginErr)
          toast.success("註冊成功！請手動登錄")
          onSuccess?.()
        }
      } else {
        throw new Error("註冊響應數據不完整")
      }
    } catch (err: any) {
      console.error("註冊失敗:", err)
      const errorMessage = err.message || "註冊失敗，請檢查輸入信息"
      setError(errorMessage)
      toast.error(errorMessage)
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="w-full">
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          {error && (
            <Alert variant="destructive">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          <div className="space-y-2">
            <Label htmlFor="username" className="flex items-center gap-2">
              <User className="h-4 w-4" />
              用戶名 <span className="text-destructive">*</span>
            </Label>
            <Input
              id="username"
              type="text"
              placeholder="請輸入用戶名（3-50個字符）"
              {...register("username")}
              disabled={isLoading}
              autoComplete="username"
            />
            {errors.username && (
              <p className="text-sm text-destructive">{errors.username.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="password">密碼 <span className="text-destructive">*</span></Label>
            <Input
              id="password"
              type="password"
              placeholder="請輸入密碼（至少6個字符）"
              {...register("password")}
              disabled={isLoading}
              autoComplete="new-password"
            />
            {errors.password && (
              <p className="text-sm text-destructive">{errors.password.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="confirmPassword">確認密碼 <span className="text-destructive">*</span></Label>
            <Input
              id="confirmPassword"
              type="password"
              placeholder="請再次輸入密碼"
              {...register("confirmPassword")}
              disabled={isLoading}
              autoComplete="new-password"
            />
            {errors.confirmPassword && (
              <p className="text-sm text-destructive">{errors.confirmPassword.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="realName" className="flex items-center gap-2">
              <User className="h-4 w-4" />
              真實姓名
            </Label>
            <Input
              id="realName"
              type="text"
              placeholder="請輸入真實姓名（可選）"
              {...register("realName")}
              disabled={isLoading}
            />
            {errors.realName && (
              <p className="text-sm text-destructive">{errors.realName.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="email" className="flex items-center gap-2">
              <Mail className="h-4 w-4" />
              郵箱
            </Label>
            <Input
              id="email"
              type="email"
              placeholder="請輸入郵箱（可選）"
              {...register("email")}
              disabled={isLoading}
              autoComplete="email"
            />
            {errors.email && (
              <p className="text-sm text-destructive">{errors.email.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="phoneNumber" className="flex items-center gap-2">
              <Phone className="h-4 w-4" />
              手機號碼
            </Label>
            <Input
              id="phoneNumber"
              type="tel"
              placeholder="請輸入手機號碼（可選）"
              {...register("phoneNumber")}
              disabled={isLoading}
              autoComplete="tel"
            />
            {errors.phoneNumber && (
              <p className="text-sm text-destructive">{errors.phoneNumber.message}</p>
            )}
          </div>

          <div className="flex gap-2">
            <Button type="submit" className="flex-1" disabled={isLoading}>
              {isLoading ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  註冊中...
                </>
              ) : (
                <>
                  <UserPlus className="mr-2 h-4 w-4" />
                  註冊
                </>
              )}
            </Button>
            {onCancel && (
              <Button type="button" variant="outline" onClick={onCancel} disabled={isLoading}>
                取消
              </Button>
            )}
          </div>
        </form>
        <p className="text-sm text-muted-foreground text-center mt-4">
          註冊即表示您同意使用本系統
        </p>
    </div>
  )
}

