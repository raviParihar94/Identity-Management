// pages/dashboard.tsx
import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/router';
import { ProtectedScreen } from '@/components/ProtectedScreen';
import { useAuthStore } from '@/store/auth-store';
import { apiClient } from '@/lib/api-client';
import { SCREEN_IDS, Screen, ApiResponse } from '@/types';
import { toast, ToastContainer } from 'react-toastify';

export default function DashboardPage() {
  return (
    <ProtectedScreen screenId={SCREEN_IDS.DASHBOARD}>
      <DashboardContent />
    </ProtectedScreen>
  );
}

function DashboardContent() {
  const router = useRouter();
  const { user, logout } = useAuthStore();
  const [accessibleScreens, setAccessibleScreens] = useState<Screen[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    fetchAccessibleScreens();
  }, []);

  const fetchAccessibleScreens = async () => {
    try {
      const response = await apiClient.get<ApiResponse<Screen[]>>('/api/screens/accessible');
      setAccessibleScreens(response.data.data || []);
    } catch (error) {
      toast.error('Failed to load accessible screens');
    } finally {
      setIsLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
    router.push('/login');
  };

  return (
    <div className="min-h-screen bg-gray-100" data-screen-id={SCREEN_IDS.DASHBOARD}>
      <ToastContainer position="top-right" autoClose={3000} />

      {/* Navigation Bar */}
      <nav className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex items-center">
              <h1 className="text-xl font-bold text-gray-900">IAM Security System</h1>
            </div>
            <div className="flex items-center space-x-4">
              <span className="text-gray-700">Welcome, {user?.username}</span>
              <button
                onClick={handleLogout}
                className="bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700"
              >
                Logout
              </button>
            </div>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          {/* User Info Card */}
          <div className="bg-white shadow rounded-lg p-6 mb-6">
            <h2 className="text-2xl font-bold text-gray-900 mb-4">Dashboard</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <p className="text-sm text-gray-500">Username</p>
                <p className="text-lg font-medium">{user?.username}</p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Email</p>
                <p className="text-lg font-medium">{user?.email}</p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Roles</p>
                <div className="flex gap-2 mt-1">
                  {user?.roles.map((role) => (
                    <span
                      key={role}
                      className="px-2 py-1 bg-blue-100 text-blue-800 rounded text-sm"
                    >
                      {role}
                    </span>
                  ))}
                </div>
              </div>
              <div>
                <p className="text-sm text-gray-500">MFA Status</p>
                <p className="text-lg font-medium">
                  {user?.mfaEnabled ? (
                    <span className="text-green-600">Enabled ✓</span>
                  ) : (
                    <span className="text-orange-600">Disabled</span>
                  )}
                </p>
              </div>
            </div>
          </div>

          {/* Quick Actions */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
            <button
              onClick={() => router.push('/profile')}
              className="bg-white shadow rounded-lg p-6 hover:shadow-lg transition-shadow"
            >
              <div className="text-blue-600 text-3xl mb-2">👤</div>
              <h3 className="text-lg font-semibold">Profile Settings</h3>
              <p className="text-gray-600 text-sm">Manage your account details</p>
            </button>

            <button
              onClick={() => router.push('/mfa-setup')}
              className="bg-white shadow rounded-lg p-6 hover:shadow-lg transition-shadow"
            >
              <div className="text-green-600 text-3xl mb-2">🔐</div>
              <h3 className="text-lg font-semibold">MFA Settings</h3>
              <p className="text-gray-600 text-sm">Enable two-factor authentication</p>
            </button>

            {user?.roles.includes('ADMIN') && (
              <button
                onClick={() => router.push('/admin')}
                className="bg-white shadow rounded-lg p-6 hover:shadow-lg transition-shadow"
              >
                <div className="text-purple-600 text-3xl mb-2">⚙️</div>
                <h3 className="text-lg font-semibold">Admin Panel</h3>
                <p className="text-gray-600 text-sm">Manage users and permissions</p>
              </button>
            )}
          </div>

          {/* Accessible Screens */}
          <div className="bg-white shadow rounded-lg p-6">
            <h3 className="text-xl font-bold text-gray-900 mb-4">Accessible Screens</h3>
            {isLoading ? (
              <p className="text-gray-500">Loading...</p>
            ) : accessibleScreens.length > 0 ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {accessibleScreens.map((screen) => (
                  <div
                    key={screen.id}
                    className="border border-gray-200 rounded-lg p-4 hover:border-blue-500 transition-colors"
                  >
                    <div className="flex items-center justify-between mb-2">
                      <h4 className="font-semibold text-gray-900">{screen.name}</h4>
                      <span className="text-xs text-gray-500">{screen.screenId}</span>
                    </div>
                    <p className="text-sm text-gray-600 mb-2">{screen.description}</p>
                    <button
                      onClick={() => router.push(screen.path)}
                      className="text-blue-600 text-sm hover:text-blue-800"
                    >
                      Open →
                    </button>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-gray-500">No accessible screens found.</p>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}