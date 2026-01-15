// components/my-messages.tsx
'use client'

import { useEffect, useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { MessageCard } from './message-card'
import { getProducerMessages } from '@/auth/twitter-api'
import type { Message } from '@/auth/twitter-api'

interface MyMessagesProps {
	userId: string;
	userName: string;
	refreshTrigger: number;
}

export function MyMessages({ userId, userName, refreshTrigger }: MyMessagesProps) {
	const [messages, setMessages] = useState<Message[]>([])
	const [loading, setLoading] = useState(true)
	const [error, setError] = useState<string | null>(null)

	useEffect(() => {
		loadMessages()
	}, [userId, refreshTrigger])

	const loadMessages = async () => {
		setLoading(true)
		setError(null)

		try {
			const userMessages = await getProducerMessages(userId)
			setMessages(userMessages.sort((a, b) => b.timestamp - a.timestamp))
		} catch (err: any) {
			setError(err.message || 'Не удалось загрузить сообщения')
		} finally {
			setLoading(false)
		}
	}

	if (loading) {
		return (
			<Card>
				<CardHeader>
					<CardTitle>Мои сообщения</CardTitle>
				</CardHeader>
				<CardContent>
					<p className="text-gray-500">Загрузка...</p>
				</CardContent>
			</Card>
		)
	}

	if (error) {
		return (
			<Card>
				<CardHeader>
					<CardTitle>Мои сообщения</CardTitle>
				</CardHeader>
				<CardContent>
					<p className="text-red-500">{error}</p>
				</CardContent>
			</Card>
		)
	}

	return (
		<div className="space-y-4">
			<h2 className="text-2xl font-bold">Мои сообщения</h2>
			{messages.length === 0 ? (
				<Card>
					<CardContent className="pt-6">
						<p className="text-gray-500 text-center">
							Вы ещё не опубликовали ни одного сообщения.
						</p>
					</CardContent>
				</Card>
			) : (
				messages.map((message) => (
					<MessageCard
						key={message.id}
						message={message}
						currentUserId={userId}
						authorName={userName}
						canDelete={true}
						onDeleted={loadMessages}
					/>
				))
			)}
		</div>
	)
}
