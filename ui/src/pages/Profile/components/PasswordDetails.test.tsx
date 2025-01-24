import { screen } from '@testing-library/react';
import { userEvent } from '@testing-library/user-event';
import { UserState } from 'contexts';
import { UserContext } from 'contexts/UserContext/User.context';
import { renderWithClient } from 'tests';
import { PasswordDetails } from './PasswordDetails';

const mockState: UserState = {
  apiKey: 'test-api-key',
  user: {
    login: 'user-login',
    email: 'email@address.pl',
    createdOn: '2020-10-09T09:57:17.995288Z',
  },
  loggedIn: true,
};
const dispatch = vi.fn();
const mockMutate = vi.fn();
const mockResponse = vi.fn();

vi.mock('api/apiComponents', () => ({
  usePostUserChangepassword: () => mockResponse(),
}));

beforeEach(() => {
  vi.clearAllMocks();
});

test('renders header', () => {
  mockResponse.mockReturnValueOnce({
    mutate: mockMutate,
    reset: vi.fn(),
    data: { apiKey: 'test-api-key' },
    isSuccess: true,
    isError: false,
    error: '',
  });

  renderWithClient(
    <UserContext.Provider value={{ state: mockState, dispatch }}>
      <PasswordDetails />
    </UserContext.Provider>
  );

  expect(screen.getByText('Password details')).toBeInTheDocument();
});

test('handles change password success', async () => {
  mockResponse.mockReturnValueOnce({
    mutate: mockMutate,
    reset: vi.fn(),
    data: { apiKey: 'test-api-key' },
    isSuccess: true,
    isError: false,
    error: '',
  });

  renderWithClient(
    <UserContext.Provider value={{ state: mockState, dispatch }}>
      <PasswordDetails />
    </UserContext.Provider>
  );

  await userEvent.type(
    screen.getByLabelText('Current password'),
    'test-password'
  );
  await userEvent.type(
    screen.getByLabelText('New password'),
    'test-new-password'
  );
  await userEvent.type(
    screen.getByLabelText('Repeat new password'),
    'test-new-password'
  );
  await userEvent.click(screen.getByText('Update password'));

  await screen.findByRole('success');
  await screen.findByText('Password changed');

  expect(mockMutate).toHaveBeenCalledTimes(1);
  expect(mockMutate).toHaveBeenCalledWith({
    body: {
      repeatedPassword: 'test-new-password',
      currentPassword: 'test-password',
      newPassword: 'test-new-password',
    },
  });
  expect(dispatch).toHaveBeenCalledWith({
    type: 'SET_API_KEY',
    apiKey: 'test-api-key',
  });
});

test('handles change password error', async () => {
  mockResponse.mockReturnValueOnce({
    mutate: mockMutate,
    reset: vi.fn(),
    data: { apiKey: 'test-api-key' },
    isSuccess: false,
    isError: true,
    error: 'Test error',
  });

  renderWithClient(
    <UserContext.Provider value={{ state: mockState, dispatch }}>
      <PasswordDetails />
    </UserContext.Provider>
  );

  await userEvent.type(
    screen.getByLabelText('Current password'),
    'test-password'
  );
  await userEvent.type(
    screen.getByLabelText('New password'),
    'test-new-password'
  );
  await userEvent.type(
    screen.getByLabelText('Repeat new password'),
    'test-new-password'
  );
  await userEvent.click(screen.getByText('Update password'));

  expect(mockMutate).toHaveBeenCalledTimes(1);
  expect(mockMutate).toHaveBeenCalledWith({
    body: {
      repeatedPassword: 'test-new-password',
      currentPassword: 'test-password',
      newPassword: 'test-new-password',
    },
  });

  await screen.findByRole('error');
  expect(await screen.findByText('Unknown error')).toBeInTheDocument();
});
