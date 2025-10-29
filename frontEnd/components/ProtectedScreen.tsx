// components/ProtectedScreen.tsx
import React, { useEffect } from 'react';
import { useRouter } from 'next/router';
import { useScreenAccess } from '@/hooks/useScreenAccess';
import { useAuthStore } from '@/store/auth-store';

interface ProtectedScreenProps {
  screenId: string;
  children: React.ReactNode;
  fallback?: React.ReactNode;
}

export const ProtectedScreen: React.FC<ProtectedScreenProps> = ({
  screenId,
  children,
  fallback,
}) => {
  const router = useRouter();
  const { isAuthenticated } = useAuthStore();
  const { hasAccess, isLoading, error } = useScreenAccess(screenId);

  useEffect(() => {
    if (!isAuthenticated) {
      router.push('/login');
    }
  }, [isAuthenticated, router]);

  if (!isAuthenticated) {
    return null;
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error || !hasAccess) {
    return (
      fallback || (
        <div className="flex flex-col items-center justify-center min-h-screen">
          <div className="bg-red-50 border border-red-200 rounded-lg p-6 max-w-md">
            <h2 className="text-xl font-bold text-red-600 mb-2">Access Denied</h2>
            <p className="text-gray-700 mb-4">
              You don't have permission to access this screen.
            </p>
            <button
              onClick={() => router.push('/dashboard')}
              className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
            >
              Go to Dashboard
            </button>
          </div>
        </div>
      )
    );
  }

  return <>{children}</>;
};

// Higher-Order Component for page protection
export const withScreenAccess = (
  Component: React.ComponentType,
  screenId: string
) => {
  return function ProtectedComponent(props: any) {
    return (
      <ProtectedScreen screenId={screenId}>
        <Component {...props} />
      </ProtectedScreen>
    );
  };
};