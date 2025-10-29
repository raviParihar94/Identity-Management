// hooks/useScreenAccess.ts
import { useState, useEffect } from 'react';
import { apiClient } from '@/lib/api-client';
import { ScreenAccessResponse, ApiResponse } from '@/types';

export const useScreenAccess = (screenId: string) => {
  const [hasAccess, setHasAccess] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const checkAccess = async () => {
      try {
        const response = await apiClient.post<ApiResponse<ScreenAccessResponse>>(
          '/api/screens/check-access',
          { screenId }
        );

        setHasAccess(response.data.data?.hasAccess || false);
        setIsLoading(false);
      } catch (err: any) {
        setError(err.response?.data?.message || 'Failed to check access');
        setHasAccess(false);
        setIsLoading(false);
      }
    };

    if (screenId) {
      checkAccess();
    }
  }, [screenId]);

  return { hasAccess, isLoading, error };
};

