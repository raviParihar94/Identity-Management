// store/auth-store.ts
import { create } from 'zustand'; // ✅ make sure this line exists
import { apiClient } from '@/lib/api-client';
import { User, AuthResponse, ApiResponse } from '@/types';

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  accessToken: string | null;

  login: (username: string, password: string) => Promise<AuthResponse>;
  register: (username: string, email: string, password: string) => Promise<void>;
  verifyMfa: (username: string, code: string) => Promise<void>;
  logout: () => void;
  setUser: (user: User | null) => void;
  checkAuth: () => void;
  loginWithOAuth: (authResponse: AuthResponse) => Promise<void>;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  isAuthenticated: false,
  isLoading: false,
  error: null,
  accessToken: null,

  // ✅ Normal username/password login
  login: async (username: string, password: string) => {
    console.log('Store: login called', { username });
    set({ isLoading: true, error: null });
    try {
      console.log('Store: sending API request to /api/auth/login');
      const response = await apiClient.post<ApiResponse<AuthResponse>>(
        '/api/auth/login',
        { username, password }
      );
      console.log('Store: API response received', response);
      const authData = response.data.data!;
      if (authData.mfaRequired) {
        console.log('Store: MFA required');
        set({ isLoading: false });
        return authData;
      }
      console.log('Store: Login success, setting token');
      localStorage.setItem('accessToken', authData.accessToken!);
      set({
        user: {
          username: authData.username,
          email: authData.email,
          roles: authData.roles || [],
          mfaEnabled: authData.mfaEnabled,
        },
        isAuthenticated: true,
        isLoading: false,
        accessToken: authData.accessToken!,
      });
      return authData;
    } catch (error: any) {
      console.error('Store: Login error', error);
      const errorMessage = error.response?.data?.message || 'Login failed';
      set({ error: errorMessage, isLoading: false });
      throw error;
    }
  },

  // ✅ Registration
  register: async (username: string, email: string, password: string) => {
    set({ isLoading: true, error: null });
    try {
      const response = await apiClient.post<ApiResponse<AuthResponse>>(
        '/api/auth/register',
        { username, email, password }
      );
      const authData = response.data.data!;
      localStorage.setItem('accessToken', authData.accessToken!);
      set({
        user: {
          username: authData.username,
          email: authData.email,
          roles: authData.roles || [],
          mfaEnabled: authData.mfaEnabled,
        },
        isAuthenticated: true,
        isLoading: false,
        accessToken: authData.accessToken!,
      });
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 'Registration failed';
      set({ error: errorMessage, isLoading: false });
      throw error;
    }
  },

  // ✅ MFA verify
  verifyMfa: async (username: string, code: string) => {
    set({ isLoading: true, error: null });
    try {
      const response = await apiClient.post<ApiResponse<AuthResponse>>(
        '/api/auth/mfa/verify',
        { username, code }
      );
      const authData = response.data.data!;
      localStorage.setItem('accessToken', authData.accessToken!);
      set({
        user: {
          username: authData.username,
          email: authData.email,
          roles: authData.roles || [],
          mfaEnabled: authData.mfaEnabled,
        },
        isAuthenticated: true,
        isLoading: false,
        accessToken: authData.accessToken!,
      });
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 'MFA verification failed';
      set({ error: errorMessage, isLoading: false });
      throw error;
    }
  },

  // ✅ Logout
  logout: () => {
    localStorage.removeItem('accessToken');
    set({
      user: null,
      isAuthenticated: false,
      accessToken: null,
      error: null,
    });
  },

  // ✅ Manual user setter
  setUser: (user: User | null) => {
    set({ user, isAuthenticated: !!user });
  },

  // ✅ Check if already logged in
  checkAuth: () => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      set({ isAuthenticated: true, accessToken: token });
    }
  },

  // ✅ OAuth Login handler (for GitHub, Google, etc.)
  loginWithOAuth: async (authResponse: AuthResponse) => {
    localStorage.setItem('accessToken', authResponse.accessToken!);
    set({
      user: {
        username: authResponse.username,
        email: authResponse.email,
        roles: authResponse.roles || [],
        mfaEnabled: authResponse.mfaEnabled,
      },
      isAuthenticated: true,
      isLoading: false,
      accessToken: authResponse.accessToken!,
    });
  },
}));
