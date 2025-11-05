// pages/dashboard.tsx
import React, { useEffect, useState } from "react";
import { useRouter } from "next/router";
import { ProtectedScreen } from "@/components/ProtectedScreen";
import { useAuthStore } from "@/store/auth-store";
import { apiClient } from "@/lib/api-client";
import { SCREEN_IDS, Screen, ApiResponse } from "@/types";
import { toast, ToastContainer } from "react-toastify";

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
      const response = await apiClient.get<ApiResponse<Screen[]>>("/api/screens/accessible");
      setAccessibleScreens(response.data.data || []);
    } catch (error) {
      toast.error("Failed to load accessible screens");
    } finally {
      setIsLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
    router.push("/login");
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100" data-screen-id={SCREEN_IDS.DASHBOARD}>
      <ToastContainer position="top-right" autoClose={3000} />

      {/* ✅ Top Navbar */}
      <nav className="backdrop-blur-md bg-white/80 shadow-sm sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-6 py-4 flex justify-between items-center">
          <h1 className="text-2xl font-bold text-indigo-600 tracking-tight">
            IAM Security Dashboard
          </h1>
          <div className="flex items-center gap-4">
            <span className="text-gray-700 font-medium">
              Hi, {user?.username}
            </span>
            <button
              onClick={handleLogout}
              className="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded-lg shadow-sm transition-all duration-200"
            >
              Logout
            </button>
          </div>
        </div>
      </nav>

      {/* ✅ Main Dashboard Content */}
      <main className="max-w-7xl mx-auto p-6 space-y-8">
        {/* User Info Card */}
        <div className="bg-white/70 backdrop-blur-md shadow-md rounded-2xl p-8 border border-gray-100">
          <h2 className="text-2xl font-semibold text-gray-900 mb-6">User Overview</h2>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <InfoField label="Username" value={user?.username} />
            <InfoField label="Email" value={user?.email} />
            <InfoField
              label="Roles"
              value={
                <div className="flex flex-wrap gap-2">
                  {user?.roles.map((role) => (
                    <span
                      key={role}
                      className="bg-indigo-100 text-indigo-800 px-2 py-1 rounded-md text-sm font-medium"
                    >
                      {role}
                    </span>
                  ))}
                </div>
              }
            />
            <InfoField
              label="MFA Status"
              value={
                user?.mfaEnabled ? (
                  <span className="text-green-600 font-semibold">Enabled ✓</span>
                ) : (
                  <span className="text-orange-500 font-semibold">Disabled</span>
                )
              }
            />
          </div>
        </div>

        {/* Quick Action Cards */}
        <section>
          <h3 className="text-xl font-semibold mb-4 text-gray-800">Quick Actions</h3>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            <ActionCard
              color="blue"
              icon="👤"
              title="Profile Settings"
              desc="Manage your personal account"
              onClick={() => router.push("/profile")}
            />
            <ActionCard
              color="green"
              icon="🔐"
              title="MFA Settings"
              desc="Configure two-factor authentication"
              onClick={() => router.push("/mfa-setup")}
            />
            {user?.roles.includes("ADMIN") && (
              <ActionCard
                color="purple"
                icon="⚙️"
                title="Admin Panel"
                desc="Manage users & permissions"
                onClick={() => router.push("/admin")}
              />
            )}
          </div>
        </section>

        {/* Accessible Screens Section */}
        <section className="bg-white/70 backdrop-blur-md shadow-md rounded-2xl p-8 border border-gray-100">
          <h3 className="text-xl font-semibold text-gray-900 mb-4">Accessible Screens</h3>

          {isLoading ? (
            <p className="text-gray-500">Loading available screens...</p>
          ) : accessibleScreens.length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {accessibleScreens.map((screen) => (
                <div
                  key={screen.id}
                  className="border border-gray-200 rounded-lg p-5 hover:border-indigo-400 transition-all cursor-pointer bg-white/60 backdrop-blur-md hover:shadow-lg"
                  onClick={() => router.push(screen.path)}
                >
                  <div className="flex justify-between mb-2">
                    <h4 className="text-gray-900 font-semibold">{screen.name}</h4>
                    <span className="text-xs text-gray-500">{screen.screenId}</span>
                  </div>
                  <p className="text-sm text-gray-600 mb-3">{screen.description}</p>
                  <span className="text-indigo-600 text-sm font-medium hover:underline">
                    Open →
                  </span>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-gray-500">No accessible screens found.</p>
          )}
        </section>
      </main>
    </div>
  );
}

/* ✅ Reusable Components */
function InfoField({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div>
      <p className="text-sm text-gray-500">{label}</p>
      <div className="text-lg font-medium text-gray-800 mt-1">{value}</div>
    </div>
  );
}

function ActionCard({
  color,
  icon,
  title,
  desc,
  onClick,
}: {
  color: string;
  icon: string;
  title: string;
  desc: string;
  onClick: () => void;
}) {
  const colors: Record<string, string> = {
    blue: "from-blue-500/10 to-blue-100/30 hover:from-blue-600/20",
    green: "from-green-500/10 to-green-100/30 hover:from-green-600/20",
    purple: "from-purple-500/10 to-purple-100/30 hover:from-purple-600/20",
  };

  return (
    <div
      onClick={onClick}
      className={`cursor-pointer bg-gradient-to-br ${colors[color]} shadow-md rounded-xl p-6 transition-all hover:shadow-lg hover:scale-[1.02]`}
    >
      <div className="text-3xl mb-3">{icon}</div>
      <h4 className="text-lg font-semibold text-gray-900 mb-1">{title}</h4>
      <p className="text-sm text-gray-600">{desc}</p>
    </div>
  );
}
