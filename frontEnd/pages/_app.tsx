import '@/styles/globals.css';
import type { AppProps } from 'next/app';
import { useEffect } from 'react';
import { useAuthStore } from '@/store/auth-store';

export default function App({ Component, pageProps }: AppProps) {
  const { checkAuth } = useAuthStore();

  useEffect(() => {
    checkAuth();
  }, [checkAuth]);

  return <Component {...pageProps} />;
}
