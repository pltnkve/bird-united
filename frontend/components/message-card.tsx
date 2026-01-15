// components/message-card.tsx
'use client'

import { useState } from 'react'
import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Trash2 } from 'lucide-react'
import { useToast } from '@/hooks/use-toast'
import { deleteMessage } from '@/auth/twitter-api'
import type { Message } from '@/auth/twitter-api'

interface MessageCardProps {
	message: Message;
	currentUserId: string;
	authorName?: string;
	canDelete: boolean;
	onDeleted: () => void;
}

export function MessageCard({ message, currentUserId, authorName, canDelete, onDeleted }: MessageCardProps) {
	const { toast } = useToast()
	const [isDeleting, setIsDeleting] = useState(false)

	const handleDelete = async () => {
		if (!confirm('Удалить это сообщение?')) return

		setIsDeleting(true)

		try {
			await deleteMessage(message.id)

			toast({
				title: 'Успешно',
				description: 'Сообщение удалено',
			})

			onDeleted()
		} catch (error: any) {
			toast({
				variant: 'destructive',
				title: 'Ошибка',
				description: error.message || 'Не удалось удалить сообщение',
			})
		} finally {
			setIsDeleting(false)
		}
	}

	const formatDate = (timestamp: number) => {
		const date = new Date(timestamp * 1000)
		const now = new Date()
		const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000)

		if (diffInSeconds < 60) return `${diffInSeconds}с назад`
		if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)}м назад`
		if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)}ч назад`

		return date.toLocaleDateString('ru-RU', {
			day: 'numeric',
			month: 'short',
			year: date.getFullYear() !== now.getFullYear() ? 'numeric' : undefined,
		})
	}

	return (
		<Card>
			<CardContent className="pt-6">
				<div className="flex justify-between items-start">
					<div className="flex-1">
						<div className="flex items-center gap-2 mb-2">
							<span className="font-semibold">{authorName || 'Пользователь'}</span>
							<span className="text-sm text-gray-500">·</span>
							<span className="text-sm text-gray-500">{formatDate(message.timestamp)}</span>
						</div>
						<p className="text-gray-900 dark:text-gray-100 whitespace-pre-wrap">
							{message.content}
						</p>
					</div>

					{canDelete && (
						<Button
							variant="ghost"
							size="icon"
							onClick={handleDelete}
							disabled={isDeleting}
							className="text-red-500 hover:text-red-700 hover:bg-red-50"
						>
							<Trash2 className="h-4 w-4" />
						</Button>
					)}
				</div>
			</CardContent>
		</Card>
	)
}
