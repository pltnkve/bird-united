// lib/with-auth.tsx
'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import {isAuthenticated} from '@/auth/auth';

export function withAuth<P extends object>(
	Component: React.ComponentType<P>
) {
	return function AuthenticatedComponent(props: P) {
		const router = useRouter()

		useEffect(() => {
			if (!isAuthenticated()) {
				router.push('/login')
			}
		}, [router])

		if (!isAuthenticated()) {
			return <div className="flex min-h-screen items-center justify-center">Загрузка...</div>
		}

		return <Component {...props} />
	}
}
