// lib/twitter-api.ts
import { fetchWithAuth, ApiResponse } from './auth';

const TWITTER_API_URL = process.env.NEXT_PUBLIC_TWITTER_API_URL || 'http://localhost:9001';

export interface Message {
	id: string;
	author: string;
	content: string;
	timestamp: number;
}

export interface Subscription {
	subscriber: string;
	producers: string[];
}

// Получить ленту подписчика (сообщения от тех, на кого подписан)
export async function getSubscriberFeed(subscriberId: string): Promise<Message[]> {
	const response = await fetchWithAuth(`${TWITTER_API_URL}/messages/subscriber/${subscriberId}`);
	const data: ApiResponse<Message[]> = await response.json();

	if (data.code === '200') {
		return data.data;
	}

	throw new Error(data.message);
}

// Получить сообщения конкретного продюсера
export async function getProducerMessages(producerId: string): Promise<Message[]> {
	const response = await fetchWithAuth(`${TWITTER_API_URL}/messages/producer/${producerId}`);
	const data: ApiResponse<Message[]> = await response.json();

	if (data.code === '200') {
		return data.data;
	}

	throw new Error(data.message);
}

// Создать сообщение
export async function createMessage(content: string, authorId: string): Promise<string> {
	const response = await fetchWithAuth(`${TWITTER_API_URL}/messages/message`, {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify({
			author: authorId,
			content: content,
		}),
	});

	const data: ApiResponse<string> = await response.json();

	if (data.code === '201' || data.code === '200') {
		return data.data;
	}

	throw new Error(data.message);
}

// Удалить сообщение
export async function deleteMessage(messageId: string): Promise<boolean> {
	const response = await fetchWithAuth(`${TWITTER_API_URL}/messages/message/${messageId}`, {
		method: 'DELETE',
	});

	const data: ApiResponse<boolean> = await response.json();

	if (data.code === '200') {
		return data.data;
	}

	throw new Error(data.message);
}

// Получить подписки
export async function getSubscriptions(subscriberId: string): Promise<Subscription> {
	const response = await fetchWithAuth(`${TWITTER_API_URL}/subscriptions/subscriber/${subscriberId}`);
	const data: ApiResponse<Subscription> = await response.json();

	if (data.code === '200' || data.code === '201') {
		return data.data;
	}

	throw new Error(data.message);
}

// Создать/обновить подписки
export async function updateSubscriptions(subscription: Subscription): Promise<boolean> {
	const response = await fetchWithAuth(`${TWITTER_API_URL}/subscriptions`, {
		method: 'PUT',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify(subscription),
	});

	const data: ApiResponse<boolean> = await response.json();

	if (data.code === '200') {
		return data.data;
	}

	throw new Error(data.message);
}
