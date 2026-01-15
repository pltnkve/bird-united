// components/create-message.tsx
'use client'

import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import * as z from 'zod'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import { useToast } from '@/hooks/use-toast'
import { createMessage } from '@/auth/twitter-api'

const messageSchema = z.object({
	content: z.string().min(1, 'Сообщение не может быть пустым').max(140, 'Максимум 140 символов'),
})

type MessageFormData = z.infer<typeof messageSchema>

interface CreateMessageProps {
	userId: string;
	onMessageCreated: () => void;
}

export function CreateMessage({ userId, onMessageCreated }: CreateMessageProps) {
	const { toast } = useToast()
	const [isSubmitting, setIsSubmitting] = useState(false)

	const {
		register,
		handleSubmit,
		reset,
		watch,
		formState: { errors },
	} = useForm<MessageFormData>({
		resolver: zodResolver(messageSchema),
	})

	const content = watch('content', '')

	const onSubmit = async (data: MessageFormData) => {
		setIsSubmitting(true)

		try {
			await createMessage(data.content, userId)

			toast({
				title: 'Успешно',
				description: 'Сообщение опубликовано',
			})

			reset()
			onMessageCreated()
		} catch (error: any) {
			toast({
				variant: 'destructive',
				title: 'Ошибка',
				description: error.message || 'Не удалось опубликовать сообщение',
			})
		} finally {
			setIsSubmitting(false)
		}
	}

	return (
		<Card>
			<CardHeader>
				<CardTitle>Новое сообщение</CardTitle>
			</CardHeader>
			<CardContent>
				<form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
					<div>
						<Textarea
							placeholder="Что происходит?"
							className="resize-none"
							rows={3}
							{...register('content')}
							disabled={isSubmitting}
						/>
						<div className="flex justify-between items-center mt-2">
              <span className={`text-sm ${content.length > 140 ? 'text-red-500' : 'text-gray-500'}`}>
                {content.length} / 140
              </span>
							{errors.content && (
								<p className="text-sm text-red-500">{errors.content.message}</p>
							)}
						</div>
					</div>
					<Button type="submit" disabled={isSubmitting || content.length > 140}>
						{isSubmitting ? 'Публикация...' : 'Опубликовать'}
					</Button>
				</form>
			</CardContent>
		</Card>
	)
}
