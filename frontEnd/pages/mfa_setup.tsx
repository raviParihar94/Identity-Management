import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/router';
import { QRCodeSVG } from 'qrcode.react';
import { ProtectedScreen } from '@/components/ProtectedScreen';
import { apiClient } from '@/lib/api-client';
import { useAuthStore } from '@/store/auth-store';
import { SCREEN_IDS, MfaSetupResponse, ApiResponse } from '@/types';
import { toast, ToastContainer } from 'react-toastify';

export default function MfaSetupPage() {
  return (
    <ProtectedScreen screenId={SCREEN_IDS.MFA_SETUP}>
      <MfaSetupContent />
    </ProtectedScreen>
  );
}

function MfaSetupContent() {
  const router = useRouter();
  const { user } = useAuthStore();
  const [mfaData, setMfaData] = useState<MfaSetupResponse | null>(null);
  const [verificationCode, setVerificationCode] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isVerifying, setIsVerifying] = useState(false);

  useEffect(() => {
    if (user?.mfaEnabled) {
      setIsLoading(false);
    } else {
      setupMfa();
    }
  }, [user]);

  const setupMfa = async () => {
    try {
      const response = await apiClient.post<ApiResponse<MfaSetupResponse>>('/api/mfa/setup');
      setMfaData(response.data.data!);
    } catch (error) {
      toast.error('Failed to setup MFA');
    } finally {
      setIsLoading(false);
    }
  };

  const handleEnableMfa = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsVerifying(true);

    try {
      await apiClient.post('/api/mfa/enable', null, {
        params: {
          secret: mfaData?.secret,
          code: verificationCode,
        },
      });
      toast.success('MFA enabled successfully!');
      router.push('/dashboard');
    } catch (error: any) {
      const errorCode = error.response?.data?.errorCode;
      const message = error.response?.data?.message || 'Failed to enable MFA';
      toast.error(`${message} (${errorCode})`);
      setIsVerifying(false);
    }
  };

  const handleDisableMfa = async () => {
    if (!confirm('Are you sure you want to disable MFA?')) return;

    try {
      await apiClient.post('/api/mfa/disable');
      toast.success('MFA disabled successfully!');
      router.push('/dashboard');
    } catch (error) {
      toast.error('Failed to disable MFA');
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100 py-12 px-4 sm:px-6 lg:px-8" data-screen-id={SCREEN_IDS.MFA_SETUP}>
      <ToastContainer position="top-right" autoClose={3000} />

      <div className="max-w-3xl mx-auto">
        <button
          onClick={() => router.back()}
          className="mb-6 text-blue-600 hover:text-blue-800"
        >
          ← Back
        </button>

        <div className="bg-white shadow rounded-lg p-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-6">
            Multi-Factor Authentication
          </h1>

          {user?.mfaEnabled ? (
            <div>
              <div className="bg-green-50 border border-green-200 rounded-lg p-4 mb-6">
                <p className="text-green-800 font-medium">
                  ✓ MFA is currently enabled for your account
                </p>
              </div>

              <p className="text-gray-700 mb-6">
                Your account is protected with two-factor authentication. You'll need to enter
                a code from your authenticator app each time you log in.
              </p>

              <button
                onClick={handleDisableMfa}
                className="bg-red-600 text-white px-6 py-2 rounded hover:bg-red-700"
              >
                Disable MFA
              </button>
            </div>
          ) : (
            <div>
              <p className="text-gray-700 mb-6">
                Enhance your account security by enabling Multi-Factor Authentication (MFA).
                You'll need an authenticator app like Google Authenticator or Authy.
              </p>

              {mfaData && (
                <div className="space-y-6">
                  <div>
                    <h2 className="text-xl font-semibold mb-4">Step 1: Scan QR Code</h2>
                    <div className="flex justify-center bg-gray-50 p-6 rounded-lg">
                      <QRCodeSVG value={mfaData.qrCodeUrl} size={200} />
                    </div>
                  </div>

                  <div>
                    <h2 className="text-xl font-semibold mb-2">
                      Or enter this key manually:
                    </h2>
                    <div className="bg-gray-100 p-4 rounded border border-gray-300">
                      <code className="text-sm font-mono break-all">{mfaData.manualEntryKey}</code>
                    </div>
                  </div>

                  <form onSubmit={handleEnableMfa}>
                    <h2 className="text-xl font-semibold mb-4">
                      Step 2: Verify with 6-digit code
                    </h2>
                    <div className="flex gap-4">
                      <input
                        type="text"
                        value={verificationCode}
                        onChange={(e) => setVerificationCode(e.target.value)}
                        maxLength={6}
                        placeholder="000000"
                        className="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-center text-2xl tracking-widest"
                        required
                      />
                      <button
                        type="submit"
                        disabled={isVerifying || verificationCode.length !== 6}
                        className="bg-blue-600 text-white px-8 py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50"
                      >
                        {isVerifying ? 'Verifying...' : 'Enable MFA'}
                      </button>
                    </div>
                  </form>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}