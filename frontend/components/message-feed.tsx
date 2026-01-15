// components/message-feed.tsx
'use client'

import { useEffect, useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { MessageCard } from './message-card'
import { getSubscriberFeed } from '@/auth/twitter-api'
import { getUserById } from '@/auth/ums-api'
import type { Message } from '@/auth/twitter-api'

interface MessageFeedProps {
	userId: string;
	refreshTrigger: number;
}

export function MessageFeed({ userId, refreshTrigger }: MessageFeedProps) {
	const [messages, setMessages] = useState<Message[]>([])
	const [authors, setAuthors] = useState<Record<string, string>>({})
	const [loading, setLoading] = useState(true)
	const [error, setError] = useState<string | null>(null)

	useEffect(() => {
		loadFeed()
	}, [userId, refreshTrigger])

	const loadFeed = async () => {
		setLoading(true)
		setError(null)

		try {
			const feedMessages = await getSubscriberFeed(userId)
			setMessages(feedMessages.sort((a, b) => b.timestamp - a.timestamp))

			// Загружаем имена авторов
			const uniqueAuthorIds = [...new Set(feedMessages.map(m => m.author))]
			const authorNames: Record<string, string> = {}

			await Promise.all(
				uniqueAuthorIds.map(async (authorId) => {
					try {
						const author = await getUserById(authorId)
						authorNames[authorId] = author.name
					} catch {
						authorNames[authorId] = 'Неизвестный пользователь'
					}
				})
			)

			setAuthors(authorNames)
		} catch (err: any) {
			setError(err.message || 'Не удалось загрузить ленту')
		} finally {
			setLoading(false)
		}
	}

	if (loading) {
		return (
			<Card>
				<CardHeader>
					<CardTitle>Лента</CardTitle>
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
					<CardTitle>Лента</CardTitle>
				</CardHeader>
				<CardContent>
					<p className="text-red-500">{error}</p>
				</CardContent>
			</Card>
		)
	}

	return (
		<div className="space-y-4">
			<h2 className="text-2xl font-bold">Лента</h2>
			{messages.length === 0 ? (
				<Card>
					<CardContent className="pt-6">
						<p className="text-gray-500 text-center">
							Нет сообщений. Подпишитесь на пользователей, чтобы видеть их посты.
						</p>
					</CardContent>
				</Card>
			) : (
				messages.map((message) => (
					<MessageCard
						key={message.id}
						message={message}
						currentUserId={userId}
						authorName={authors[message.author]}
						canDelete={false}
						onDeleted={loadFeed}
					/>
				))
			)}
		</div>
	)
}
