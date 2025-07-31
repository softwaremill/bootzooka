import { screen } from '@testing-library/react';
import { userEvent } from '@testing-library/user-event';
import { MemoryRouter } from 'react-router';
import { renderWithClient } from 'tests';
import { Register } from './Register';
import { UserContext } from 'contexts/UserContext/User.context';
import { initialUserState } from 'contexts/UserContext/UserContext.constants';
import { Mock } from 'vitest';

const dispatch = vi.fn();
const mockMutate = vi.fn();
const mockResponse = vi.fn();

vi.mock('api/apiComponents', () => ({
  usePostUserRegister: () => mockResponse(),
}));

const mockCustomSuccessfulResponse = (
  apiKey = 'test-api-key',
  onSuccess?: Mock
) => {
  const mockMutateWithCallback = vi.fn().mockImplementation(async () => {
    if (onSuccess) {
      onSuccess({ apiKey });
    }
  });

  const mutateToUse = onSuccess ? mockMutateWithCallback : mockMutate;

  const mockResult = mockResponse.mockReturnValueOnce({
    mutate: mutateToUse,
    reset: vi.fn(),
    data: { apiKey },
    isSuccess: true,
    isError: false,
    error: '',
  });

  return { mockResult, mutate: mutateToUse };
};

beforeEach(() => {
  vi.clearAllMocks();
});

test('<Register /> should render the header text', () => {
  mockResponse.mockReturnValue({
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
        value={{ state: { ...initialUserState }, dispatch }}
      >
        <Register />
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(screen.getByText('Please sign up')).toBeInTheDocument();
});

test('<Register /> should handle successful registration through the submit button click', async () => {
  const onSuccess = vi.fn();

  const { mutate } = mockCustomSuccessfulResponse('test-api-key', onSuccess);

  renderWithClient(
    <MemoryRouter initialEntries={['/login']}>
      <UserContext.Provider
        value={{ state: { ...initialUserState }, dispatch }}
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

  expect(mutate).toHaveBeenCalledWith({
    body: {
      login: 'test-login',
      email: 'test@email.address.pl',
      password: 'test-password',
      repeatedPassword: 'test-password',
    },
  });

  expect(onSuccess).toHaveBeenCalledWith({
    apiKey: 'test-api-key',
  });
});

test('<Register /> should handle successful registration through the Enter key press', async () => {
  const onSuccess = vi.fn();

  const { mutate } = mockCustomSuccessfulResponse('test-api-key', onSuccess);

  renderWithClient(
    <MemoryRouter initialEntries={['/login']}>
      <UserContext.Provider
        value={{ state: { ...initialUserState }, dispatch }}
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
  await userEvent.keyboard('{Enter}');

  await screen.findByRole('success');

  expect(mutate).toHaveBeenCalledWith({
    body: {
      login: 'test-login',
      email: 'test@email.address.pl',
      password: 'test-password',
      repeatedPassword: 'test-password',
    },
  });

  expect(onSuccess).toHaveBeenCalledWith({
    apiKey: 'test-api-key',
  });
});

test('<Register /> should handle failed registration attempt', async () => {
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
        value={{ state: { ...initialUserState }, dispatch }}
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

  await screen.findByRole('error');
});
