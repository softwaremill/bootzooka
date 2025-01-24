import { screen } from '@testing-library/react';
import { userEvent } from '@testing-library/user-event';
import { MemoryRouter, useLocation } from 'react-router';
import { UserContext } from 'contexts/UserContext/User.context';
import { initialUserState } from 'contexts/UserContext/UserContext.constants';
import { renderWithClient } from 'tests';
import { Login } from './Login';

const dispatch = vi.fn();
const mockMutate = vi.fn();
const mockResponse = vi.fn();

const LocationDisplay = () => {
  const location = useLocation();

  return <div data-testid="location-display">{location.pathname}</div>;
};

vi.mock('api/apiComponents', () => ({
  usePostUserLogin: () => mockResponse(),
}));

beforeEach(() => {
  vi.clearAllMocks();
});

test('renders header', () => {
  mockResponse.mockReturnValueOnce({
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
        value={{ state: { ...initialUserState, loggedIn: false }, dispatch }}
      >
        <Login />
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(screen.getByText('Please sign in')).toBeInTheDocument();
});

test('redirects when logged in', () => {
  mockResponse.mockReturnValueOnce({
    mutateAsync: mockMutate,
    reset: vi.fn(),
    data: { apiKey: 'test-api-key' },
    isSuccess: true,
    isPending: false,
    isError: false,
    error: '',
  });

  renderWithClient(
    <MemoryRouter initialEntries={['/login']}>
      <UserContext.Provider
        value={{ state: { ...initialUserState, loggedIn: true }, dispatch }}
      >
        <Login />
        <LocationDisplay />
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(screen.getByTestId('location-display')).toHaveTextContent('/main');
});

test('handles login success', async () => {
  mockResponse.mockReturnValueOnce({
    mutateAsync: mockMutate,
    reset: vi.fn(),
    data: { apiKey: 'test-api-key' },
    isSuccess: true,
    isError: false,
    isPending: false,
    error: '',
    onSuccess: dispatch({
      type: 'SET_API_KEY',
      apiKey: 'test-api-key',
    }),
  });

  renderWithClient(
    <MemoryRouter initialEntries={['/login']}>
      <UserContext.Provider
        value={{ state: { ...initialUserState, loggedIn: false }, dispatch }}
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

  expect(dispatch).toHaveBeenCalledWith({
    type: 'SET_API_KEY',
    apiKey: 'test-api-key',
  });
});

test('handles login error', async () => {
  mockResponse.mockReturnValueOnce({
    mutateAsync: mockMutate,
    reset: vi.fn(),
    data: { apiKey: 'test-api-key' },
    isSuccess: false,
    isError: true,
    error: 'Test error',
    isPending: false,
  });

  renderWithClient(
    <MemoryRouter initialEntries={['/login']}>
      <UserContext.Provider
        value={{ state: { ...initialUserState, loggedIn: false }, dispatch }}
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
