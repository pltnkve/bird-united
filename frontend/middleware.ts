// middleware.ts
import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

export function middleware(request: NextRequest) {
	const accessToken = request.cookies.get('accessToken')?.value
	const isAuthPage = request.nextUrl.pathname.startsWith('/login') ||
		request.nextUrl.pathname.startsWith('/register')
	const isProtectedPage = request.nextUrl.pathname.startsWith('/dashboard') ||
		request.nextUrl.pathname.startsWith('/messages')

	// Если пользователь авторизован и пытается зайти на /login
	if (isAuthPage && accessToken) {
		return NextResponse.redirect(new URL('/dashboard', request.url))
	}

	// Если пользователь НЕ авторизован и пытается зайти на защищённые страницы
	if (isProtectedPage && !accessToken) {
		return NextResponse.redirect(new URL('/login', request.url))
	}

	return NextResponse.next()
}

export const config = {
	matcher: ['/dashboard/:path*', '/messages/:path*', '/login', '/register'],
}
