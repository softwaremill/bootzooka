import { screen } from '@testing-library/react';
import { userEvent } from '@testing-library/user-event';
import { MemoryRouter } from 'react-router';
import { UserContext } from 'contexts/UserContext/User.context';
import { initialUserState } from 'contexts/UserContext/UserContext.constants';
import { renderWithClient } from 'tests';
import { Login } from './Login';

const dispatch = vi.fn();
const mockMutate = vi.fn();
const mockApiKeyResponse = vi.fn();
const mockGetUserResponse = vi.fn(() => ({
  data: {
    user: {
      login: 'test-user',
      email: 'test-user@example.com',
      createdOn: '2023-10-01T12:00:00Z',
    },
  },
  isSuccess: true,
  isPending: false,
  isError: false,
}));

vi.mock('api/apiComponents', () => ({
  useGetUser: () => mockGetUserResponse(),
  usePostUserLogin: () => mockApiKeyResponse(),
}));

beforeEach(() => {
  vi.clearAllMocks();
});

test('<Login /> should render the header', () => {
  mockApiKeyResponse.mockReturnValueOnce({
    mutateAsync: mockMutate,
    reset: vi.fn(),
    data: { apiKey: 'test-api-key' },
    isSuccess: true,
    isError: false,
    error: '',
    isPending: false,
  });

  renderWithClient(
    <MemoryRouter initialEntries={['/login']}>
      <UserContext.Provider
        value={{ state: { ...initialUserState }, dispatch }}
      >
        <Login />
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(screen.getByText('Please sign in')).toBeInTheDocument();
});

test('<Login /> should handle the login form submission through the Enter key press', async () => {
  const onSuccess = vi.fn();

  mockApiKeyResponse.mockReturnValueOnce({
    mutateAsync: mockMutate.mockImplementationOnce(() => {
      onSuccess({ apiKey: 'test-api-key' });
    }),
    reset: vi.fn(),
    data: { apiKey: 'test-api-key' },
    isSuccess: true,
    isError: false,
    isPending: false,
    error: '',
  });

  renderWithClient(
    <MemoryRouter initialEntries={['/login']}>
      <UserContext.Provider
        value={{ state: { ...initialUserState }, dispatch }}
      >
        <Login />
      </UserContext.Provider>
    </MemoryRouter>
  );

  await userEvent.type(screen.getByLabelText('Login or email'), 'test-login');
  await userEvent.type(screen.getByLabelText('Password'), 'test-password');
  await userEvent.keyboard('{Enter}');

  await screen.findByRole('success');

  expect(mockMutate).toHaveBeenCalledWith({
    body: {
      loginOrEmail: 'test-login',
      password: 'test-password',
    },
  });

  expect(onSuccess).toHaveBeenCalledWith({
    apiKey: 'test-api-key',
  });

  expect(dispatch).toHaveBeenCalledTimes(1);
});

test('<Login /> should handle successful login attempt through the submit button click', async () => {
  const onSuccess = vi.fn();
  mockApiKeyResponse.mockReturnValueOnce({
    mutateAsync: mockMutate.mockImplementationOnce(() =>
      onSuccess({ apiKey: 'test-api-key' })
    ),
    reset: vi.fn(),
    data: { apiKey: 'test-api-key' },
    isSuccess: true,
    isError: false,
    isPending: false,
    error: '',
  });

  renderWithClient(
    <MemoryRouter initialEntries={['/login']}>
      <UserContext.Provider
        value={{ state: { ...initialUserState }, dispatch }}
      >
        <Login />
      </UserContext.Provider>
    </MemoryRouter>
  );

  await userEvent.type(screen.getByLabelText('Login or email'), 'test-login');
  await userEvent.type(screen.getByLabelText('Password'), 'test-password');
  await userEvent.click(screen.getByText('Sign In'));

  await screen.findByRole('success');

  expect(mockMutate).toHaveBeenCalledWith({
    body: {
      loginOrEmail: 'test-login',
      password: 'test-password',
    },
  });

  expect(onSuccess).toHaveBeenCalledWith({
    apiKey: 'test-api-key',
  });

  expect(dispatch).toHaveBeenCalledTimes(1);
});

test('<Login /> should handle failed login attempt', async () => {
  mockApiKeyResponse.mockReturnValue({
    mutateAsync: mockMutate,
    reset: vi.fn(),
    data: undefined,
    isSuccess: false,
    isError: true,
    error: 'Test error',
    isPending: false,
  });

  mockGetUserResponse.mockReturnValueOnce({
    data: {
      user: {
        login: 'test-user',
        email: 'test-user@example.com',
        createdOn: '2023-10-01T12:00:00Z',
      },
    },
    isSuccess: false,
    isPending: false,
    isError: false,
  });

  renderWithClient(
    <MemoryRouter initialEntries={['/login']}>
      <UserContext.Provider
        value={{ state: { ...initialUserState }, dispatch }}
      >
        <Login />
      </UserContext.Provider>
    </MemoryRouter>
  );

  await userEvent.type(screen.getByLabelText('Login or email'), 'test-login');
  await userEvent.type(screen.getByLabelText('Password'), 'test-password');
  await userEvent.click(screen.getByText('Sign In'));

  await screen.findByRole('error');

  expect(mockMutate).toHaveBeenCalledWith({
    body: { loginOrEmail: 'test-login', password: 'test-password' },
  });
  expect(dispatch).not.toHaveBeenCalled();
});
