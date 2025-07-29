import { screen } from '@testing-library/react';
import { userEvent } from '@testing-library/user-event';
import { UserState } from 'contexts';
import { UserContext } from 'contexts/UserContext/User.context';
import { renderWithClient } from 'tests';
import { PasswordDetails } from './PasswordDetails';

const mockState: UserState = {
  user: {
    login: 'user-login',
    email: 'email@address.pl',
    createdOn: '2020-10-09T09:57:17.995288Z',
  },
};
const dispatch = vi.fn();
const mockMutate = vi.fn();
const mockResponse = vi.fn();

vi.mock('api/apiComponents', () => ({
  usePostUserChangepassword: () => mockResponse(),
}));

beforeEach(() => {
  localStorage.clear();
  vi.clearAllMocks();
});

const setStorageApiKeyState = () =>
  localStorage.setItem('apiKey', '{ "apiKey": "test-api-key" }');

const mockSuccessfulResponse = (apiKey = 'test-api-key') => {
  return mockResponse.mockReturnValueOnce({
    mutate: mockMutate,
    reset: vi.fn(),
    data: { apiKey },
    isSuccess: true,
    isError: false,
    error: '',
  });
};

test('<PasswordDetails /> should render a message about unavailable details when the API key is missing"', async () => {
  renderWithClient(
    <UserContext.Provider value={{ state: mockState, dispatch }}>
      <PasswordDetails />
    </UserContext.Provider>
  );

  expect(screen.getByText('Password details not available.')).toBeVisible();
});

test('<PasswordDetails /> should render the form when the API key is available', async () => {
  setStorageApiKeyState();
  mockSuccessfulResponse();

  renderWithClient(
    <UserContext.Provider value={{ state: mockState, dispatch }}>
      <PasswordDetails />
    </UserContext.Provider>
  );

  expect(screen.getByLabelText('Current password')).toBeVisible();
  expect(screen.getByLabelText('New password')).toBeVisible();
  expect(screen.getByLabelText('Repeat new password')).toBeVisible();
});

test('<PasswordDetails /> should successfully submit the form by clicking the submit button', async () => {
  setStorageApiKeyState();
  mockSuccessfulResponse('new-test-api-key');

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
});

test('<PasswordDetails /> should succesfully submit the form by pressing the Enter key', async () => {
  setStorageApiKeyState();
  mockSuccessfulResponse('new-test-api-key');

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

  await userEvent.keyboard('{Enter}');

  await screen.findByRole('success');
  await screen.findByText('Password changed');
});

test('<PasswordDetails /> should display an error message when the API call fails', async () => {
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
