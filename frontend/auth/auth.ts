// lib/auth.ts
const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:9000';

export interface LoginCredentials {
	email: string;
	password: string;
}

export interface AuthTokens {
	accessToken: string;
	refreshToken: string;
	tokenType: string;
	expiresIn: number;
}

export interface ApiResponse<T> {
	code: string;
	message: string;
	data: T;
}

class AuthError extends Error {
	constructor(public code: string, message: string) {
		super(message);
		this.name = 'AuthError';
	}
}

export async function login(credentials: LoginCredentials): Promise<AuthTokens> {
	const response = await fetch(`${API_URL}/auth/login`, {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify(credentials),
	});

	const data: ApiResponse<AuthTokens> = await response.json();

	if (data.code !== '200') {
		throw new AuthError(data.code, data.message);
	}

	setAuthCookies(data.data);
	return data.data;
}

export async function refreshAccessToken(): Promise<string> {
	const refreshToken = typeof window !== 'undefined' ? localStorage.getItem('refreshToken') : null;

	if (!refreshToken) {
		throw new AuthError('401', 'No refresh token available');
	}

	const response = await fetch(`${API_URL}/auth/refresh`, {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify({ refreshToken }),
	});

	const data: ApiResponse<AuthTokens> = await response.json();

	if (data.code !== '200') {
		// Токен истёк, разлогиниваем
		logout();
		throw new AuthError(data.code, 'Session expired. Please login again.');
	}

	// Обновляем токены
	if (typeof window !== 'undefined') {
		localStorage.setItem('accessToken', data.data.accessToken);
		localStorage.setItem('refreshToken', data.data.refreshToken);
		localStorage.setItem('tokenExpiry', String(Date.now() + data.data.expiresIn * 1000));
	}

	return data.data.accessToken;
}

export function logout() {
	clearAuthCookies();
	window.location.href = '/login';
}

export function getAccessToken(): string | null {
	if (typeof window === 'undefined') return null;
	return localStorage.getItem('accessToken');
}

export function isAuthenticated(): boolean {
	if (typeof window === 'undefined') return false;

	const token = localStorage.getItem('accessToken');
	const expiry = localStorage.getItem('tokenExpiry');

	if (!token || !expiry) return false;

	// Проверяем что токен ещё валиден
	return Date.now() < parseInt(expiry);
}

// Универсальный fetch с автообновлением токена
export async function fetchWithAuth(url: string, options: RequestInit = {}): Promise<Response> {
	let accessToken = getAccessToken();

	// Проверяем нужно ли обновить токен
	const expiry = typeof window !== 'undefined' ? localStorage.getItem('tokenExpiry') : null;
	if (expiry && Date.now() > parseInt(expiry) - 60000) { // за минуту до истечения
		try {
			accessToken = await refreshAccessToken();
		} catch (error) {
			throw error;
		}
	}

	const response = await fetch(url, {
		...options,
		headers: {
			...options.headers,
			'Authorization': `Bearer ${accessToken}`,
		},
	});

	// Если 401, пробуем обновить токен один раз
	if (response.status === 401) {
		try {
			accessToken = await refreshAccessToken();

			// Повторяем запрос с новым токеном
			return fetch(url, {
				...options,
				headers: {
					...options.headers,
					'Authorization': `Bearer ${accessToken}`,
				},
			});
		} catch (error) {
			logout();
			throw error;
		}
	}

	return response;
}

export function setAuthCookies(tokens: AuthTokens) {
	if (typeof window === 'undefined') return;

	// Устанавливаем cookie для middleware
	document.cookie = `accessToken=${tokens.accessToken}; path=/; max-age=${tokens.expiresIn}; SameSite=Strict`;

	// Всё ещё храним в localStorage для клиентской логики
	localStorage.setItem('accessToken', tokens.accessToken);
	localStorage.setItem('refreshToken', tokens.refreshToken);
	localStorage.setItem('tokenExpiry', String(Date.now() + tokens.expiresIn * 1000));
}

export function clearAuthCookies() {
	if (typeof window === 'undefined') return;

	document.cookie = 'accessToken=; path=/; max-age=0';
	localStorage.removeItem('accessToken');
	localStorage.removeItem('refreshToken');
	localStorage.removeItem('tokenExpiry');
}
