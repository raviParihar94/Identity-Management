// types/index.ts
export interface User {
  username: string;
  email: string;
  roles: string[];
  mfaEnabled: boolean;
}

export interface AuthResponse {
  accessToken?: string;
  refreshToken?: string;
  username: string;
  email: string;
  mfaEnabled: boolean;
  mfaRequired?: boolean;
  roles?: string[];
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data?: T;
  errorCode?: string;
}

export interface ErrorResponse {
  errorCode: string;
  message: string;
  details?: string;
  timestamp: string;
  path: string;
}

export interface Screen {
  id: number;
  screenId: string;
  name: string;
  description: string;
  path: string;
  isActive: boolean;
}

export interface MfaSetupResponse {
  secret: string;
  qrCodeUrl: string;
  manualEntryKey: string;
}

export interface ScreenAccessResponse {
  hasAccess: boolean;
  screenId: string;
  screenName: string;
  message: string;
}

// Screen IDs (centralized)
export const SCREEN_IDS = {
  LOGIN: 'SCR_LOGIN_001',
  REGISTER: 'SCR_REGISTER_001',
  DASHBOARD: 'SCR_DASHBOARD_001',
  PROFILE: 'SCR_PROFILE_001',
  MFA_SETUP: 'SCR_MFA_SETUP_001',
  ADMIN_DASHBOARD: 'SCR_ADMIN_DASH_001',
  USER_MANAGEMENT: 'SCR_USER_MGMT_001',
  SETTINGS: 'SCR_SETTINGS_001',
} as const;

// Error Codes (centralized)
export const ERROR_CODES = {
  AUTH_INVALID_CREDENTIALS: 'ERR_AUTH_001',
  AUTH_ACCOUNT_LOCKED: 'ERR_AUTH_002',
  AUTH_TOO_MANY_ATTEMPTS: 'ERR_AUTH_003',
  MFA_INVALID_CODE: 'ERR_MFA_001',
  MFA_NOT_ENABLED: 'ERR_MFA_002',
  USER_NOT_FOUND: 'ERR_USER_001',
  SCREEN_NOT_FOUND: 'ERR_SCREEN_001',
  ACCESS_DENIED: 'ERR_ACCESS_001',
  VALIDATION_ERROR: 'ERR_VAL_001',
  REGISTRATION_USERNAME_EXISTS: 'ERR_REG_001',
  REGISTRATION_EMAIL_EXISTS: 'ERR_REG_002',
  SYSTEM_ERROR: 'ERR_SYS_001',
} as const;