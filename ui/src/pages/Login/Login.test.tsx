import { screen } from '@testing-library/react';
import { userEvent } from '@testing-library/user-event';
import { MemoryRouter } from 'react-router';
import { UserContext } from '@/contexts/UserContext/User.context';
import { initialUserState } from '@/contexts/UserContext/UserContext.constants';
import { renderWithClient } from '@/tests';
import { Login } from './index';
import * as apiComponents from '@/api/apiComponents';

const dispatch = vi.fn();

vi.mock('@/api/apiComponents', () => ({
  useGetUser: vi.fn(),
  usePostUserLogin: vi.fn(),
}));

beforeEach(() => {
  vi.clearAllMocks();
});

const mockUseGetUser = (returnValue: object) =>
  vi
    .mocked(apiComponents.useGetUser)
    .mockReturnValue(
      returnValue as unknown as ReturnType<typeof apiComponents.useGetUser>
    );

const mockUsePostUserLogin = (returnValue: object) =>
  vi
    .mocked(apiComponents.usePostUserLogin)
    .mockReturnValue(
      returnValue as unknown as ReturnType<
        typeof apiComponents.usePostUserLogin
      >
    );

test('<Login /> should handle the login form submission through the Enter key press', async () => {
  const mockMutateAsync = vi.fn().mockResolvedValue({ apiKey: 'test-api-key' });
  const mockReset = vi.fn();

  mockUseGetUser({
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
  });

  mockUsePostUserLogin({
    mutateAsync: mockMutateAsync,
    reset: mockReset,
    data: undefined,
    isSuccess: false,
    isError: false,
    isPending: false,
    error: undefined,
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

  expect(
    screen.getByText('Enter your credentials to login')
  ).toBeInTheDocument();

  const loginElements = await screen.findAllByText('Login');

  await userEvent.type(screen.getByLabelText('Login or email'), 'test-login');
  await userEvent.type(screen.getByLabelText('Password'), 'test-password');
  await userEvent.click(loginElements[1]);

  await vi.waitFor(() => {
    expect(mockMutateAsync).toHaveBeenCalledWith({
      body: {
        loginOrEmail: 'test-login',
        password: 'test-password',
      },
    });
  });

  await vi.waitFor(() => {
    expect(dispatch).toHaveBeenCalledTimes(1);
  });
});

test('<Login /> should handle failed login attempt', async () => {
  const mockMutateAsync = vi.fn();

  mockUseGetUser({
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

  mockUsePostUserLogin({
    mutateAsync: mockMutateAsync,
    reset: vi.fn(),
    data: undefined,
    isSuccess: false,
    isError: false,
    isPending: false,
    error: undefined,
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

  const loginElements = await screen.findAllByText('Login');

  await userEvent.type(screen.getByLabelText('Login or email'), 'test-login');
  await userEvent.type(screen.getByLabelText('Password'), 'test-password');
  await userEvent.click(loginElements[1]);

  expect(mockMutateAsync).toHaveBeenCalledWith({
    body: { loginOrEmail: 'test-login', password: 'test-password' },
  });
  expect(dispatch).not.toHaveBeenCalled();
});
