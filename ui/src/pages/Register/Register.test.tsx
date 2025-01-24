import { screen } from '@testing-library/react';
import { userEvent } from '@testing-library/user-event';
import { MemoryRouter, useLocation } from 'react-router';
import { renderWithClient } from 'tests';
import { Register } from './Register';
import { UserContext } from 'contexts/UserContext/User.context';
import { initialUserState } from 'contexts/UserContext/UserContext.constants';

const dispatch = vi.fn();
const mockMutate = vi.fn();
const mockResponse = vi.fn();

const LocationDisplay = () => {
  const location = useLocation();

  return <div data-testid="location-display">{location.pathname}</div>;
};

vi.mock('api/apiComponents', () => ({
  usePostUserRegister: () => mockResponse(),
}));

beforeEach(() => {
  vi.clearAllMocks();
});

test('renders header', () => {
  mockResponse.mockReturnValue({
    mutate: mockMutate,
    reset: vi.fn(),
    isSuccess: true,
    isPending: false,
    data: { apiKey: 'test-api-key' },
    isError: false,
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
        <Register />
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(screen.getByText('Please sign up')).toBeInTheDocument();
});

test('redirects when registered', () => {
  mockResponse.mockReturnValueOnce({
    mutate: mockMutate,
    reset: vi.fn(),
    isSuccess: true,
    isPending: false,
    data: { apiKey: 'test-api-key' },
    isError: false,
    error: '',
  });

  renderWithClient(
    <MemoryRouter initialEntries={['/login']}>
      <UserContext.Provider
        value={{ state: { ...initialUserState, loggedIn: true }, dispatch }}
      >
        <Register />
        <LocationDisplay />
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(screen.getByTestId('location-display')).toHaveTextContent('/main');
});

test('handles register success', async () => {
  mockResponse.mockReturnValueOnce({
    mutate: mockMutate,
    reset: vi.fn(),
    isSuccess: true,
    data: { apiKey: 'test-api-key' },
    isError: false,
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
        <Register />
      </UserContext.Provider>
    </MemoryRouter>
  );

  await userEvent.type(screen.getByLabelText('Login'), 'test-login');
  await userEvent.type(
    screen.getByLabelText('Email address'),
    'test@email.address.pl'
  );
  await userEvent.type(screen.getByLabelText('Password'), 'test-password');
  await userEvent.type(
    screen.getByLabelText('Repeat password'),
    'test-password'
  );
  await userEvent.click(screen.getByText('Create new account'));

  await screen.findByRole('success');

  expect(mockMutate).toHaveBeenCalledWith({
    body: {
      login: 'test-login',
      email: 'test@email.address.pl',
      password: 'test-password',
      repeatedPassword: 'test-password',
    },
  });

  expect(dispatch).toHaveBeenCalledWith({
    apiKey: 'test-api-key',
    type: 'SET_API_KEY',
  });
});

test('handles register error', async () => {
  mockResponse.mockReturnValueOnce({
    mutate: mockMutate,
    reset: vi.fn(),
    data: { apiKey: 'test-api-key' },
    isSuccess: false,
    isError: true,
    error: 'Test error',
  });

  renderWithClient(
    <MemoryRouter initialEntries={['/login']}>
      <UserContext.Provider
        value={{ state: { ...initialUserState, loggedIn: false }, dispatch }}
      >
        <Register />
      </UserContext.Provider>
    </MemoryRouter>
  );

  await userEvent.type(screen.getByLabelText('Login'), 'test-login');
  await userEvent.type(
    screen.getByLabelText('Email address'),
    'test@email.address.pl'
  );
  await userEvent.type(screen.getByLabelText('Password'), 'test-password');
  await userEvent.type(
    screen.getByLabelText('Repeat password'),
    'test-password'
  );
  await userEvent.click(screen.getByText('Create new account'));

  expect(mockMutate).toHaveBeenCalledWith({
    body: {
      login: 'test-login',
      email: 'test@email.address.pl',
      password: 'test-password',
      repeatedPassword: 'test-password',
    },
  });
  expect(dispatch).not.toHaveBeenCalled();

  await screen.findByRole('error');
});
