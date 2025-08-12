import { screen } from '@testing-library/react';
import { userEvent } from '@testing-library/user-event';
import { UserContext } from '@/contexts/UserContext/User.context';
import { UserState } from '@/contexts';
import { renderWithClient } from '@/tests';
import { ProfileDetails } from './ProfileDetails';
import * as apiComponents from '@/api/apiComponents';
import { UpdateUserIN } from '@/api/apiSchemas';
import { Mock } from 'vitest';

const loggedUserState: UserState = {
  user: {
    login: 'user-login',
    email: 'email@address.pl',
    createdOn: '2020-10-09T09:57:17.995288Z',
  },
};
const dispatch = vi.fn();
const mockMutate = vi.fn();

vi.mock('@/api/apiComponents', () => ({
  usePostUser: vi.fn(),
}));

vi.mock('sonner', () => ({
  toast: vi.fn(),
}));

beforeEach(() => {
  vi.clearAllMocks();
});

const mockPostUserResponse = ({
  onSuccess,
  data,
}: {
  onSuccess?: Mock;
  data: UpdateUserIN;
}) => {
  const mockMutateWithCallback = vi.fn().mockImplementation(async () => {
    if (onSuccess) onSuccess({ body: data });
  });

  const mockMutate = vi.fn();
  const mutateToUse = onSuccess ? mockMutateWithCallback : mockMutate;

  const mockResult = {
    mutate: mutateToUse,
    reset: vi.fn(),
    data: { body: data },
    isSuccess: true,
    isError: false,
    error: '',
  } as unknown as ReturnType<typeof apiComponents.usePostUser>;

  vi.mocked(apiComponents.usePostUser).mockReturnValue(mockResult);

  return { mockResult, mutate: mutateToUse };
};

const mockPostUser = (returnValue: object) =>
  vi
    .mocked(apiComponents.usePostUser)
    .mockReturnValue(
      returnValue as unknown as ReturnType<typeof apiComponents.usePostUser>
    );

test('<ProfileDetails /> should render current user data', () => {
  mockPostUser({
    mutate: mockMutate,
    reset: vi.fn(),
    data: { apiKey: 'test-api-key' },
    isSuccess: true,
    isError: false,
    error: '',
  });

  renderWithClient(
    <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
      <ProfileDetails />
    </UserContext.Provider>
  );

  expect((screen.getByLabelText('Login') as HTMLInputElement).value).toEqual(
    'user-login'
  );
  expect((screen.getByLabelText('Email') as HTMLInputElement).value).toEqual(
    'email@address.pl'
  );
});

test('<ProfileDetails /> should not render any existing user data', () => {
  mockPostUser({
    mutate: mockMutate,
    reset: vi.fn(),
    data: { apiKey: 'test-api-key' },
    isSuccess: true,
    isError: false,
    error: '',
  });

  renderWithClient(
    <UserContext.Provider value={{ state: { user: null }, dispatch }}>
      <ProfileDetails />
    </UserContext.Provider>
  );

  expect(screen.getByText('Profile details not available')).toBeVisible();
});

test('<ProfileDetails /> should handle details update successfully', async () => {
  const onSuccess = vi.fn();
  const { mutate } = mockPostUserResponse({
    onSuccess,
    data: {
      login: 'test-login',
      email: 'test@email.address',
    },
  });

  renderWithClient(
    <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
      <ProfileDetails />
    </UserContext.Provider>
  );

  await userEvent.clear(screen.getByLabelText('Login'));
  await userEvent.type(screen.getByLabelText('Login'), 'test-login');
  await userEvent.clear(screen.getByLabelText('Email'));
  await userEvent.type(screen.getByLabelText('Email'), 'test@email.address');
  await userEvent.click(screen.getByText('Update profile details'));

  expect(mutate).toHaveBeenCalledWith({
    body: { email: 'test@email.address', login: 'test-login' },
  });

  expect(onSuccess).toHaveBeenCalledWith({
    body: { email: 'test@email.address', login: 'test-login' },
  });
});

test('<ProfileDetails /> should handle details update error', async () => {
  mockPostUser({
    mutate: mockMutate,
    reset: vi.fn(),
    data: { apiKey: 'test-api-key' },
    isSuccess: true,
    isError: true,
    error: 'Test error',
  });

  renderWithClient(
    <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
      <ProfileDetails />
    </UserContext.Provider>
  );

  await userEvent.clear(screen.getByLabelText('Login'));
  await userEvent.type(screen.getByLabelText('Login'), 'test-login');
  await userEvent.clear(screen.getByLabelText('Email'));
  await userEvent.type(screen.getByLabelText('Email'), 'test@email.address');
  await userEvent.click(screen.getByText('Update profile details'));

  expect(mockMutate).toHaveBeenCalledWith({
    body: { email: 'test@email.address', login: 'test-login' },
  });
});
