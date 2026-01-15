// app/dashboard/page.tsx
'use client'

import { useEffect, useState } from 'react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import {fetchWithAuth, logout} from '@/auth/auth';
import {withAuth} from '@/auth/with-auth';

interface User {
	id: string;
	name: string;
	email: string;
	roles: Array<{ role: string }>;
}

function DashboardPage() {
	const [user, setUser] = useState<User | null>(null)
	const [loading, setLoading] = useState(true)

	useEffect(() => {
		fetchWithAuth('http://localhost:9000/users/me')
			.then(res => res.json())
			.then(data => {
				if (data.code === '200') {
					setUser(data.data)
				}
			})
			.catch(() => {
				logout()
			})
			.finally(() => setLoading(false))
	}, [])

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

export default withAuth(DashboardPage)
