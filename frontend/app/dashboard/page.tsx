// app/dashboard/page.tsx
'use client'

import { useEffect, useState } from 'react'
import { withAuth } from '@/auth/with-auth'
import { logout } from '@/auth/auth'
import { getCurrentUser } from '@/auth/ums-api'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { CreateMessage } from '@/components/create-message'
import { MessageFeed } from '@/components/message-feed'
import { MyMessages } from '@/components/my-messages'
import type { User } from '@/auth/ums-api'

function DashboardPage() {
	const [user, setUser] = useState<User | null>(null)
	const [loading, setLoading] = useState(true)
	const [refreshTrigger, setRefreshTrigger] = useState(0)

	useEffect(() => {
		getCurrentUser()
			.then(setUser)
			.catch(() => logout())
			.finally(() => setLoading(false))
	}, [])

	const handleMessageCreated = () => {
		setRefreshTrigger(prev => prev + 1)
	}

	const hasRole = (role: string) => {
		return user?.roles.some(r => r.role === role) || false
	}

	if (loading) {
		return <div className="flex min-h-screen items-center justify-center">Загрузка...</div>
	}

	if (!user) {
		return null
	}

	return (
		<div className="min-h-screen bg-gray-50 dark:bg-gray-900">
			<header className="bg-white dark:bg-gray-800 border-b sticky top-0 z-10">
				<div className="container mx-auto px-4 py-4 flex justify-between items-center">
					<h1 className="text-2xl font-bold">Twitter Clone</h1>
					<div className="flex items-center gap-4">
            <span className="text-sm text-gray-600 dark:text-gray-400">
              {user.name} ({user.roles.map(r => r.role).join(', ')})
            </span>
						<Button onClick={logout} variant="outline">Выйти</Button>
					</div>
				</div>
			</header>

			<div className="container mx-auto px-4 py-6">
				<div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
					{/* Основной контент */}
					<div className="lg:col-span-2 space-y-6">
						{hasRole('PRODUCER') && (
							<CreateMessage userId={user.id} onMessageCreated={handleMessageCreated} />
						)}

						<Tabs defaultValue="feed" className="w-full">
							<TabsList className="grid w-full grid-cols-2">
								<TabsTrigger value="feed">Лента</TabsTrigger>
								<TabsTrigger value="my-messages">Мои сообщения</TabsTrigger>
							</TabsList>

							<TabsContent value="feed" className="mt-6">
								{hasRole('SUBSCRIBER') ? (
									<MessageFeed userId={user.id} refreshTrigger={refreshTrigger} />
								) : (
									<Card>
										<CardContent className="pt-6">
											<p className="text-center text-gray-500">
												Роль SUBSCRIBER требуется для просмотра ленты
											</p>
										</CardContent>
									</Card>
								)}
							</TabsContent>

							<TabsContent value="my-messages" className="mt-6">
								{hasRole('PRODUCER') ? (
									<MyMessages
										userId={user.id}
										userName={user.name}
										refreshTrigger={refreshTrigger}
									/>
								) : (
									<Card>
										<CardContent className="pt-6">
											<p className="text-center text-gray-500">
												Роль PRODUCER требуется для создания сообщений
											</p>
										</CardContent>
									</Card>
								)}
							</TabsContent>
						</Tabs>
					</div>

					{/* Сайдбар */}
					<div className="space-y-6">
						<Card>
							<CardHeader>
								<CardTitle>Профиль</CardTitle>
							</CardHeader>
							<CardContent className="space-y-2">
								<div>
									<p className="text-sm text-gray-500">Имя</p>
									<p className="font-medium">{user.name}</p>
								</div>
								<div>
									<p className="text-sm text-gray-500">Email</p>
									<p className="font-medium">{user.email}</p>
								</div>
								<div>
									<p className="text-sm text-gray-500">Роли</p>
									<div className="flex flex-wrap gap-2 mt-1">
										{user.roles.map(role => (
											<span
												key={role.role}
												className="px-2 py-1 bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-200 text-xs rounded-full"
											>
                        {role.role}
                      </span>
										))}
									</div>
								</div>
							</CardContent>
						</Card>

						{hasRole('SUBSCRIBER') && (
							<Card>
								<CardHeader>
									<CardTitle>Подписки</CardTitle>
								</CardHeader>
								<CardContent>
									<p className="text-sm text-gray-500">
										Функция управления подписками в разработке
									</p>
								</CardContent>
							</Card>
						)}
					</div>
				</div>
			</div>
		</div>
	)
}

export default withAuth(DashboardPage)
