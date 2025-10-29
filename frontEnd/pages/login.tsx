// pages/login.tsx
import React, { useState } from 'react';
import { useRouter } from 'next/router';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useAuthStore } from '@/store/auth-store';
import { SCREEN_IDS } from '@/types';
import { toast, ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

const loginSchema = z.object({
  username: z.string().min(3, 'Username must be at least 3 characters'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
});

type LoginFormData = z.infer<typeof loginSchema>;

export default function LoginPage() {
  const router = useRouter();
  const { login } = useAuthStore();
  const [showMfa, setShowMfa] = useState(false);
  const [mfaUsername, setMfaUsername] = useState('');

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginFormData) => {
    try {
      const response = await login(data.username, data.password);

      if (response.mfaRequired) {
        setMfaUsername(data.username);
        setShowMfa(true);
        toast.info('Please enter your MFA code');
      } else {
        toast.success('Login successful!');
        router.push('/dashboard');
      }
    } catch (error: any) {
      const errorCode = error.response?.data?.errorCode;
      const message = error.response?.data?.message || 'Login failed';

      toast.error(`${message} (${errorCode})`);
    }
  };

  if (showMfa) {
    return <MfaVerification username={mfaUsername} onBack={() => setShowMfa(false)} />;
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8" data-screen-id={SCREEN_IDS.LOGIN}>
      <ToastContainer position="top-right" autoClose={3000} />

      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
          Sign in to your account
        </h2>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
          <form className="space-y-6" onSubmit={handleSubmit(onSubmit)}>
            <div>
              <label htmlFor="username" className="block text-sm font-medium text-gray-700">
                Username
              </label>
              <div className="mt-1">
                <input
                  {...register('username')}
                  type="text"
                  className="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                />
                {errors.username && (
                  <p className="mt-1 text-sm text-red-600">{errors.username.message}</p>
                )}
              </div>
            </div>

            <div>
              <label htmlFor="password" className="block text-sm font-medium text-gray-700">
                Password
              </label>
              <div className="mt-1">
                <input
                  {...register('password')}
                  type="password"
                  className="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                />
                {errors.password && (
                  <p className="mt-1 text-sm text-red-600">{errors.password.message}</p>
                )}
              </div>
            </div>

            <div>
              <button
                type="submit"
                disabled={isSubmitting}
                className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
              >
                {isSubmitting ? 'Signing in...' : 'Sign in'}
              </button>
            </div>
          </form>

          <div className="mt-6">
            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-300" />
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-2 bg-white text-gray-500">Or continue with</span>
              </div>
            </div>

            <div className="mt-6 grid grid-cols-2 gap-3">
              <a
                href={`${process.env.NEXT_PUBLIC_API_URL}/oauth2/authorization/google`}
                className="w-full inline-flex justify-center py-2 px-4 border border-gray-300 rounded-md shadow-sm bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
              >
                Google
              </a>
              <a
                href={`${process.env.NEXT_PUBLIC_API_URL}/oauth2/authorization/github`}
                className="w-full inline-flex justify-center py-2 px-4 border border-gray-300 rounded-md shadow-sm bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
              >
                GitHub
              </a>
            </div>
          </div>

          <div className="mt-6 text-center">
            <a
              href="/register"
              className="font-medium text-blue-600 hover:text-blue-500"
            >
              Don't have an account? Sign up
            </a>
          </div>
        </div>
      </div>
    </div>
  );
}

// MFA Verification Component
function MfaVerification({ username, onBack }: { username: string; onBack: () => void }) {
  const router = useRouter();
  const { verifyMfa } = useAuthStore();
  const [code, setCode] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleVerify = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      await verifyMfa(username, code);
      toast.success('MFA verification successful!');
      router.push('/dashboard');
    } catch (error: any) {
      const errorCode = error.response?.data?.errorCode;
      const message = error.response?.data?.message || 'MFA verification failed';
      toast.error(`${message} (${errorCode})`);
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
          Enter MFA Code
        </h2>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
          <form onSubmit={handleVerify} className="space-y-6">
            <div>
              <label htmlFor="code" className="block text-sm font-medium text-gray-700">
                6-Digit Code
              </label>
              <input
                type="text"
                value={code}
                onChange={(e) => setCode(e.target.value)}
                maxLength={6}
                className="mt-1 appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 text-center text-2xl tracking-widest"
                placeholder="000000"
              />
            </div>

            <button
              type="submit"
              disabled={isSubmitting || code.length !== 6}
              className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none disabled:opacity-50"
            >
              {isSubmitting ? 'Verifying...' : 'Verify'}
            </button>

            <button
              type="button"
              onClick={onBack}
              className="w-full flex justify-center py-2 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
            >
              Back to Login
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}