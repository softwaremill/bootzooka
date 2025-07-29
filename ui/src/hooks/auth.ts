import { useLocalStorage } from '@uidotdev/usehooks';
import { useGetUser } from 'api/apiComponents';
import { useUserContext } from 'contexts/UserContext/User.context';
import { useEffect } from 'react';

export interface ApiKeyState {
  apiKey: string | null;
}

export const useApiKeyState = () =>
  useLocalStorage<ApiKeyState | null>('apiKey');

export const useUserCheck = () => {
  const [apiKeyState] = useApiKeyState();
  console.log(apiKeyState);
  const { dispatch } = useUserContext();

  const { data, isSuccess, isError } = useGetUser(
    { headers: { Authorization: `Bearer ${apiKeyState?.apiKey}` } },
    {
      enabled: Boolean(apiKeyState?.apiKey),
      retry: false,
    }
  );

  useEffect(() => {
    if (isSuccess && data) {
      dispatch({ type: 'LOG_IN', user: data });
    }
  }, [isSuccess, data, dispatch]);

  useEffect(() => {
    if (!apiKeyState?.apiKey) {
      dispatch({ type: 'LOG_OUT' });
    }
  }, [apiKeyState, dispatch]);

  return {
    data,
    isSuccess,
    isError,
  } as const;
};
