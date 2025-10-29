import { useEffect } from 'react';
import { useRouter } from 'next/router';
import { useAuthStore } from '@/store/auth-store';

export default function AuthCallback() {
  const router = useRouter();
  const { setUser } = useAuthStore();

  useEffect(() => {
    const { token } = router.query;

    if (token && typeof token === 'string') {
      localStorage.setItem('accessToken', token);

      // Decode JWT to get user info (basic implementation)
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        setUser({
          username: payload.sub,
          email: payload.email || '',
          roles: payload.roles || [],
          mfaEnabled: false,
        });
        router.push('/dashboard');
      } catch (error) {
        console.error('Failed to parse token', error);
        router.push('/login');
      }
    }
  }, [router.query, setUser, router]);

  return (
    <div className="flex items-center justify-center min-h-screen">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
        <p className="text-gray-600">Completing authentication...</p>
      </div>
    </div>
  );
}