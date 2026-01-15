// lib/ums-api.ts
import { fetchWithAuth, ApiResponse } from './auth';

const UMS_API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:9000';

export interface User {
	id: string;
	name: string;
	email: string;
	created: number;
	roles: Array<{ role: string; description: string }>;
}

// Получить текущего пользователя
export async function getCurrentUser(): Promise<User> {
	const response = await fetchWithAuth(`${UMS_API_URL}/users/me`);
	const data: ApiResponse<User> = await response.json();

	if (data.code === '200') {
		return data.data;
	}

	throw new Error(data.message);
}

// Получить всех пользователей
export async function getAllUsers(): Promise<User[]> {
	const response = await fetchWithAuth(`${UMS_API_URL}/users`);
	const data: ApiResponse<User[]> = await response.json();

	if (data.code === '200') {
		return data.data;
	}

	throw new Error(data.message);
}

// Получить пользователя по ID
export async function getUserById(userId: string): Promise<User> {
	const response = await fetchWithAuth(`${UMS_API_URL}/users/user/${userId}`);
	const data: ApiResponse<User> = await response.json();

	if (data.code === '200') {
		return data.data;
	}

	throw new Error(data.message);
}
