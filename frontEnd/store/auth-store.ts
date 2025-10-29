// store/auth-store.ts
import { create } from 'zustand';
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
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  isAuthenticated: false,
  isLoading: false,
  error: null,
  accessToken: null,

  login: async (username: string, password: string) => {
    set({ isLoading: true, error: null });
    try {
      const response = await apiClient.post<ApiResponse<AuthResponse>>(
        '/api/auth/login',
        { username, password }
      );

      const authData = response.data.data!;

      if (authData.mfaRequired) {
        set({ isLoading: false });
        return authData;
      }

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
      const errorMessage = error.response?.data?.message || 'Login failed';
      set({ error: errorMessage, isLoading: false });
      throw error;
    }
  },

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

  logout: () => {
    localStorage.removeItem('accessToken');
    set({
      user: null,
      isAuthenticated: false,
      accessToken: null,
      error: null,
    });
  },

  setUser: (user: User | null) => {
    set({ user, isAuthenticated: !!user });
  },

  checkAuth: () => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      set({ isAuthenticated: true, accessToken: token });
    }
  },
}));