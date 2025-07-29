import { renderHook } from '@testing-library/react';
import { useApiKeyState, useUserCheck } from './auth';
import { UserContext } from 'contexts/UserContext/User.context';

const mockMutate = vi.fn();
const mockResponse = vi.fn();
const mockDispatch = vi.fn();

vi.mock('api/apiComponents', () => ({
  useGetUser: () => mockResponse(),
}));

beforeEach(() => {
  localStorage.clear();
  vi.clearAllMocks();
});

test('useUserCheck() should fetch user data when the API key is available', () => {
  mockResponse.mockReturnValueOnce({
    mutate: mockMutate,
    reset: vi.fn(),
    data: {
      login: 'test-user',
      email: 'test@example.com',
      createdOn: '2020-10-09T09:57:17.995288Z',
    },
    isSuccess: true,
    isError: false,
    error: '',
  });

  const { result } = renderHook(() => useUserCheck(), {
    wrapper: ({ children }) => (
      <UserContext.Provider
        value={{ state: { user: null }, dispatch: mockDispatch }}
      >
        {children}
      </UserContext.Provider>
    ),
  });

  expect(result.current.isSuccess).toBe(true);
  expect(result.current.data).toEqual({
    login: 'test-user',
    email: 'test@example.com',
    createdOn: '2020-10-09T09:57:17.995288Z',
  });
  expect(mockDispatch).toHaveBeenCalledWith({
    type: 'LOG_IN',
    user: result.current.data,
  });
});

test('useApiKeyState() should return undefined when no API key is stored', () => {
  const { result } = renderHook(() => useApiKeyState());

  expect(result.current[0]).toBeUndefined();
});

test('useApiKeyState() should return stored API key state', () => {
  const { result, rerender } = renderHook(() => useApiKeyState());

  const [, setState] = result.current;

  setState({ apiKey: '123' });

  rerender();

  expect(result.current[0]).toEqual({ apiKey: '123' });
});
