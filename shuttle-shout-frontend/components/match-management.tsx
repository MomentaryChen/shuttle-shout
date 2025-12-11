"use client"

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Trophy, Calendar, Users, TrendingUp } from "lucide-react"

export function MatchManagement() {
  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* 页面标题 */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-blue-900 dark:text-blue-100">
            比赛管理系统
          </h2>
          <p className="text-sm text-muted-foreground mt-1">
            羽毛球比赛管理和结果记录
          </p>
        </div>
      </div>

      {/* 简要统计 */}
      <div className="grid grid-cols-4 gap-4">
        <div className="bg-blue-50 dark:bg-blue-950/20 rounded-lg p-4 border border-blue-100 dark:border-blue-900/30">
          <div className="flex items-center gap-3">
            <Trophy className="h-5 w-5 text-blue-600 dark:text-blue-400" />
            <div>
              <p className="text-2xl font-bold text-blue-900 dark:text-blue-100">0</p>
              <p className="text-sm text-blue-700 dark:text-blue-300">總比賽數</p>
            </div>
          </div>
        </div>

        <div className="bg-green-50 dark:bg-green-950/20 rounded-lg p-4 border border-green-100 dark:border-green-900/30">
          <div className="flex items-center gap-3">
            <Calendar className="h-5 w-5 text-green-600 dark:text-green-400" />
            <div>
              <p className="text-2xl font-bold text-green-900 dark:text-green-100">0</p>
              <p className="text-sm text-green-700 dark:text-green-300">進行中比賽</p>
            </div>
          </div>
        </div>

        <div className="bg-purple-50 dark:bg-purple-950/20 rounded-lg p-4 border border-purple-100 dark:border-purple-900/30">
          <div className="flex items-center gap-3">
            <Users className="h-5 w-5 text-purple-600 dark:text-purple-400" />
            <div>
              <p className="text-2xl font-bold text-purple-900 dark:text-purple-100">0</p>
              <p className="text-sm text-purple-700 dark:text-purple-300">參賽選手</p>
            </div>
          </div>
        </div>

        <div className="bg-orange-50 dark:bg-orange-950/20 rounded-lg p-4 border border-orange-100 dark:border-orange-900/30">
          <div className="flex items-center gap-3">
            <TrendingUp className="h-5 w-5 text-orange-600 dark:text-orange-400" />
            <div>
              <p className="text-2xl font-bold text-orange-900 dark:text-orange-100">0</p>
              <p className="text-sm text-orange-700 dark:text-orange-300">完成比賽</p>
            </div>
          </div>
        </div>
      </div>

      {/* 功能说明 */}
      <Card className="border-2 border-blue-100 dark:border-border/50 bg-gradient-to-br from-blue-50/50 to-white dark:from-card/30 dark:to-card/50">
        <CardContent className="py-16">
          <div className="text-center space-y-4">
            <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-blue-100 dark:bg-blue-900/20 mb-4">
              <Trophy className="h-8 w-8 text-blue-500 dark:text-blue-400" />
            </div>
            <div>
              <h3 className="text-xl font-semibold text-foreground mb-2">
                比赛管理系统
              </h3>
              <p className="text-muted-foreground mb-4">
                此功能正在開發中，將提供完整的羽毛球比赛管理功能
              </p>
              <Badge variant="outline" className="text-blue-600 border-blue-200">
                即將推出
              </Badge>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
