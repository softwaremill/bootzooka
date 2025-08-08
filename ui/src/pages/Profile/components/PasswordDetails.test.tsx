import { screen } from '@testing-library/react';
import { userEvent } from '@testing-library/user-event';
import { UserState } from '@/contexts';
import { UserContext } from '@/contexts/UserContext/User.context';
import { renderWithClient } from '@/tests';
import { PasswordDetails } from './PasswordDetails';
import { Mock } from 'vitest';
import * as apiComponents from '@/api/apiComponents';

const mockState: UserState = {
  user: {
    login: 'user-login',
    email: 'email@address.pl',
    createdOn: '2020-10-09T09:57:17.995288Z',
  },
};

const dispatch = vi.fn();
const mockMutate = vi.fn();

vi.mock('@/api/apiComponents', () => ({
  usePostUserChangepassword: vi.fn(),
}));

beforeEach(() => {
  localStorage.clear();
  vi.clearAllMocks();
});

const setStorageApiKeyState = () =>
  localStorage.setItem('apiKey', '{ "apiKey": "test-api-key" }');

const mockSuccessfulResponse = (apiKey = 'test-api-key') => {
  vi.mocked(apiComponents.usePostUserChangepassword).mockReturnValueOnce({
    mutate: mockMutate,
    reset: vi.fn(),
    data: { apiKey },
    isSuccess: true,
    isError: false,
    error: '',
  } as unknown as ReturnType<typeof apiComponents.usePostUserChangepassword>);
};

const mockErrorResponse = (mutate: Mock = vi.fn(), apiKey?: string) =>
  vi.mocked(apiComponents.usePostUserChangepassword).mockReturnValue({
    mutate,
    reset: vi.fn(),
    data: apiKey ? { apiKey } : undefined,
    isSuccess: false,
    isError: true,
    error: 'Some error',
  } as unknown as ReturnType<typeof apiComponents.usePostUserChangepassword>);

const mockCustomSuccessfulResponse = (
  apiKey = 'test-api-key',
  onSuccess?: Mock
) => {
  const mockMutateWithCallback = vi
    .fn()
    .mockImplementation(async (variables) => {
      if (onSuccess) {
        onSuccess({ apiKey }, variables, undefined);
      }
    });

  const mutateToUse = onSuccess ? mockMutateWithCallback : mockMutate;

  const mockResult = {
    mutate: mutateToUse,
    reset: vi.fn(),
    data: { apiKey },
    isSuccess: true,
    isError: false,
    error: '',
  } as unknown as ReturnType<typeof apiComponents.usePostUserChangepassword>;

  vi.mocked(apiComponents.usePostUserChangepassword).mockReturnValue(
    mockResult
  );

  return { mockResult, mutate: mutateToUse };
};

test('<PasswordDetails /> should render a message about unavailable details when the API key is missing"', async () => {
  mockErrorResponse();

  renderWithClient(
    <UserContext.Provider value={{ state: mockState, dispatch }}>
      <PasswordDetails />
    </UserContext.Provider>
  );

  expect(screen.getByText('Password details not available')).toBeVisible();
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

test('<PasswordDetails /> should successfully submit the form', async () => {
  setStorageApiKeyState();

  const storageHandler = vi.fn();
  const { mutate } = mockCustomSuccessfulResponse(
    'new-test-api-key',
    storageHandler
  );

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
  await userEvent.click(screen.getByText('Update password details'));

  await screen.findByText('Password changed');

  expect(mutate).toHaveBeenCalledTimes(1);
  expect(mutate).toHaveBeenCalledWith({
    body: {
      repeatedPassword: 'test-new-password',
      currentPassword: 'test-password',
      newPassword: 'test-new-password',
    },
  });

  expect(storageHandler).toHaveBeenCalledTimes(1);
});

test('<PasswordDetails /> should display an error message when the API call fails', async () => {
  setStorageApiKeyState();

  const mutate = vi.fn();

  mockErrorResponse(mutate, 'test-api-key');

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
  await userEvent.click(screen.getByText('Update password details'));

  expect(mutate).toHaveBeenCalledTimes(1);
  expect(mutate).toHaveBeenCalledWith({
    body: {
      repeatedPassword: 'test-new-password',
      currentPassword: 'test-password',
      newPassword: 'test-new-password',
    },
  });

  expect(await screen.findByText('Unknown error')).toBeInTheDocument();
});
