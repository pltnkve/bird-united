// app/dashboard/page.tsx
'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { fetchWithAuth, logout, isAuthenticated } from '@/auth/auth'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'

interface User {
	id: string;
	name: string;
	email: string;
	roles: Array<{ role: string }>;
}

export default function DashboardPage() {
	const router = useRouter()
	const [user, setUser] = useState<User | null>(null)
	const [loading, setLoading] = useState(true)

	useEffect(() => {
		if (!isAuthenticated()) {
			router.push('/login')
			return
		}

		// Загружаем данные пользователя
		fetchWithAuth('http://localhost:9000/users')
			.then(res => res.json())
			.then(data => {
				if (data.code === '200' && data.data.length > 0) {
					setUser(data.data[0]) // временно берём первого юзера
				}
			})
			.catch(() => {
				logout()
			})
			.finally(() => setLoading(false))
	}, [router])

	if (loading) {
		return <div className="flex min-h-screen items-center justify-center">Загрузка...</div>
	}

	return (
		<div className="container mx-auto p-6">
			<div className="flex justify-between items-center mb-6">
				<h1 className="text-3xl font-bold">Dashboard</h1>
				<Button onClick={logout} variant="outline">Выйти</Button>
			</div>

			<Card>
				<CardHeader>
					<CardTitle>Информация о пользователе</CardTitle>
				</CardHeader>
				<CardContent>
					{user ? (
						<div className="space-y-2">
							<p><strong>Имя:</strong> {user.name}</p>
							<p><strong>Email:</strong> {user.email}</p>
							<p><strong>Роли:</strong> {user.roles.map(r => r.role).join(', ')}</p>
						</div>
					) : (
						<p>Не удалось загрузить данные</p>
					)}
				</CardContent>
			</Card>
		</div>
	)
}
