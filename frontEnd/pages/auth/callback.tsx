import { useEffect } from 'react';
import { useRouter } from 'next/router';
import { jwtDecode } from "jwt-decode";
import { useAuthStore } from '@/store/auth-store';

interface JwtPayload {
  sub: string;
  email?: string;
  roles?: string[];
  exp?: number;
}

export default function AuthCallback() {
  const router = useRouter();
  const { setUser, logout } = useAuthStore();

  useEffect(() => {
    const handleAuthCallback = async () => {
      const tokenParam = router.query.token;

        if (!tokenParam || typeof tokenParam !== 'string') {
          router.replace('/login');
          return;
        }


      try {
        // ✅ Decode JWT payload
        const payload: JwtPayload = jwtDecode(tokenParam);

        // ✅ Check token expiration
        if (payload.exp && payload.exp * 1000 < Date.now()) {
          console.warn('Token expired, requesting new token...');
          logout();
          router.replace('/login');
          return;
        }

        // ✅ Persist securely
        localStorage.setItem('accessToken', tokenParam);

        // ✅ Save user state
        setUser({
          username: payload.sub,
          email: payload.email ?? '',
          roles: payload.roles ?? [],
          mfaEnabled: false,
        });

        // ✅ Navigate safely
        await router.replace('/dashboard');
      } catch (err) {
        console.error('Invalid token:', err);
        logout();
        router.replace('/login');
      }
    };

    if (router.isReady) handleAuthCallback();
  }, [router, setUser, logout]);

  return (
    <div className="flex items-center justify-center min-h-screen bg-gray-50">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
        <p className="text-gray-600 text-sm">Completing authentication...</p>
      </div>
    </div>
  );
}
