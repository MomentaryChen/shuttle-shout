"use client"

import { useState, useEffect } from "react"
import { useAuth } from "@/contexts/AuthContext"
import { userApi } from "@/lib/api"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Spinner } from "@/components/ui/spinner"
import { toast } from "sonner"
import { User, Mail, Phone, Lock, Save, X } from "lucide-react"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"

export function ProfileForm() {
  const { user, refreshUser } = useAuth()
  const [loading, setLoading] = useState(false)
  const [formData, setFormData] = useState({
    email: "",
    phoneNumber: "",
    realName: "",
    password: "",
    confirmPassword: "",
  })

  useEffect(() => {
    if (user) {
      setFormData({
        email: user.email || "",
        phoneNumber: user.phoneNumber || "",
        realName: user.realName || "",
        password: "",
        confirmPassword: "",
      })
    }
  }, [user])

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    // 如果填写了密码，验证密码确认
    if (formData.password && formData.password !== formData.confirmPassword) {
      toast.error("密碼與確認密碼不一致")
      return
    }

    // 如果填写了密码，验证密码长度
    if (formData.password && formData.password.length < 6) {
      toast.error("密碼長度不能少於6個字符")
      return
    }

    try {
      setLoading(true)

      // 准备更新数据，只包含有值的字段
      const updateData: {
        email?: string
        phoneNumber?: string
        realName?: string
        password?: string
      } = {}

      if (formData.email && formData.email !== user?.email) {
        updateData.email = formData.email
      }
      if (formData.phoneNumber !== user?.phoneNumber) {
        updateData.phoneNumber = formData.phoneNumber || undefined
      }
      if (formData.realName !== user?.realName) {
        updateData.realName = formData.realName || undefined
      }
      if (formData.password) {
        updateData.password = formData.password
      }

      // 如果没有要更新的字段，提示用户
      if (Object.keys(updateData).length === 0) {
        toast.info("沒有需要更新的信息")
        return
      }

      await userApi.updateMe(updateData)
      toast.success("個人資料更新成功")
      
      // 刷新用户信息
      await refreshUser()

      // 清空密码字段
      setFormData((prev) => ({
        ...prev,
        password: "",
        confirmPassword: "",
      }))
    } catch (error: any) {
      console.error("更新個人資料失敗:", error)
      toast.error(error.message || "更新個人資料失敗，請稍後重試")
    } finally {
      setLoading(false)
    }
  }

  const handleReset = () => {
    if (user) {
      setFormData({
        email: user.email || "",
        phoneNumber: user.phoneNumber || "",
        realName: user.realName || "",
        password: "",
        confirmPassword: "",
      })
    }
  }

  if (!user) {
    return (
      <div className="flex items-center justify-center py-12">
        <Spinner className="h-8 w-8" />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* 用户头像和信息卡片 */}
      <Card>
        <CardHeader>
          <CardTitle>個人資料</CardTitle>
          <CardDescription>查看和管理您的個人信息</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex items-center gap-4 mb-6">
            <Avatar className="h-20 w-20">
              <AvatarFallback className="text-2xl bg-primary text-primary-foreground">
                {user.realName?.charAt(0) || user.username?.charAt(0) || "U"}
              </AvatarFallback>
            </Avatar>
            <div>
              <h3 className="text-lg font-semibold">{user.realName || user.username}</h3>
              <p className="text-sm text-muted-foreground">{user.username}</p>
              {user.email && <p className="text-sm text-muted-foreground">{user.email}</p>}
            </div>
          </div>
        </CardContent>
      </Card>

      {/* 编辑表单 */}
      <Card>
        <CardHeader>
          <CardTitle>編輯個人資料</CardTitle>
          <CardDescription>更新您的個人信息</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            {/* 真实姓名 */}
            <div className="space-y-2">
              <Label htmlFor="realName" className="flex items-center gap-2">
                <User className="h-4 w-4" />
                真實姓名
              </Label>
              <Input
                id="realName"
                name="realName"
                type="text"
                value={formData.realName}
                onChange={handleChange}
                placeholder="請輸入真實姓名"
              />
            </div>

            {/* 邮箱 */}
            <div className="space-y-2">
              <Label htmlFor="email" className="flex items-center gap-2">
                <Mail className="h-4 w-4" />
                電子郵箱
              </Label>
              <Input
                id="email"
                name="email"
                type="email"
                value={formData.email}
                onChange={handleChange}
                placeholder="請輸入電子郵箱"
              />
            </div>

            {/* 电话号码 */}
            <div className="space-y-2">
              <Label htmlFor="phoneNumber" className="flex items-center gap-2">
                <Phone className="h-4 w-4" />
                電話號碼
              </Label>
              <Input
                id="phoneNumber"
                name="phoneNumber"
                type="tel"
                value={formData.phoneNumber}
                onChange={handleChange}
                placeholder="請輸入電話號碼"
              />
            </div>

            {/* 密码 */}
            <div className="space-y-2">
              <Label htmlFor="password" className="flex items-center gap-2">
                <Lock className="h-4 w-4" />
                新密碼（可選）
              </Label>
              <Input
                id="password"
                name="password"
                type="password"
                value={formData.password}
                onChange={handleChange}
                placeholder="留空則不修改密碼"
                autoComplete="new-password"
              />
            </div>

            {/* 确认密码 */}
            {formData.password && (
              <div className="space-y-2">
                <Label htmlFor="confirmPassword" className="flex items-center gap-2">
                  <Lock className="h-4 w-4" />
                  確認新密碼
                </Label>
                <Input
                  id="confirmPassword"
                  name="confirmPassword"
                  type="password"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  placeholder="請再次輸入新密碼"
                  autoComplete="new-password"
                />
              </div>
            )}

            {/* 按钮组 */}
            <div className="flex gap-2 pt-4">
              <Button type="submit" disabled={loading} className="flex items-center gap-2">
                {loading ? (
                  <>
                    <Spinner className="h-4 w-4" />
                    保存中...
                  </>
                ) : (
                  <>
                    <Save className="h-4 w-4" />
                    保存更改
                  </>
                )}
              </Button>
              <Button
                type="button"
                variant="outline"
                onClick={handleReset}
                disabled={loading}
                className="flex items-center gap-2"
              >
                <X className="h-4 w-4" />
                重置
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      {/* 账户信息 */}
      <Card>
        <CardHeader>
          <CardTitle>賬戶信息</CardTitle>
          <CardDescription>您的賬戶詳細信息</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            <div className="flex justify-between">
              <span className="text-sm text-muted-foreground">用戶名</span>
              <span className="text-sm font-medium">{user.username}</span>
            </div>
            {user.roleNames && user.roleNames.length > 0 && (
              <div className="flex justify-between">
                <span className="text-sm text-muted-foreground">角色</span>
                <div className="flex gap-1">
                  {user.roleNames.map((role) => (
                    <span key={role} className="text-sm font-medium">
                      {role}
                    </span>
                  ))}
                </div>
              </div>
            )}
            {user.createdAt && (
              <div className="flex justify-between">
                <span className="text-sm text-muted-foreground">註冊時間</span>
                <span className="text-sm font-medium">
                  {new Date(user.createdAt).toLocaleDateString("zh-TW")}
                </span>
              </div>
            )}
            {user.lastLoginAt && (
              <div className="flex justify-between">
                <span className="text-sm text-muted-foreground">最後登錄</span>
                <span className="text-sm font-medium">
                  {new Date(user.lastLoginAt).toLocaleDateString("zh-TW")}
                </span>
              </div>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  )
}

