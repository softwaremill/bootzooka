import { screen } from '@testing-library/react';
import { userEvent } from '@testing-library/user-event';
import { UserContext } from 'contexts/UserContext/User.context';
import { UserState } from 'contexts';
import { renderWithClient } from 'tests';
import { ProfileDetails } from './ProfileDetails';

const loggedUserState: UserState = {
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
  usePostUser: () => mockResponse(),
}));

beforeEach(() => {
  vi.clearAllMocks();
});

test('<ProfileDetails /> should render current user data', () => {
  mockResponse.mockReturnValueOnce({
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
  expect(
    (screen.getByLabelText('Email address') as HTMLInputElement).value
  ).toEqual('email@address.pl');
});

test('<ProfileDetails /> should not render any existing user data', () => {
  mockResponse.mockReturnValueOnce({
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

  expect(screen.getByText('Profile details not available.')).toBeVisible();
});

test('<ProfileDetails /> should handle details update successfully', async () => {
  mockResponse.mockReturnValueOnce({
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

  await userEvent.clear(screen.getByLabelText('Login'));
  await userEvent.type(screen.getByLabelText('Login'), 'test-login');
  await userEvent.clear(screen.getByLabelText('Email address'));
  await userEvent.type(
    screen.getByLabelText('Email address'),
    'test@email.address'
  );
  await userEvent.click(screen.getByText('Update profile data'));

  expect(mockMutate).toHaveBeenCalledWith({
    body: { email: 'test@email.address', login: 'test-login' },
  });

  expect(dispatch).toHaveBeenCalledWith({
    type: 'UPDATE_USER_DATA',
    user: { apiKey: 'test-api-key' },
  });

  await screen.findByRole('success');
  await screen.findByText('Profile details changed');
});

test('<ProfileDetails /> should handle details update error', async () => {
  mockResponse.mockReturnValueOnce({
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
  await userEvent.clear(screen.getByLabelText('Email address'));
  await userEvent.type(
    screen.getByLabelText('Email address'),
    'test@email.address'
  );
  await userEvent.click(screen.getByText('Update profile data'));

  expect(mockMutate).toHaveBeenCalledWith({
    body: { email: 'test@email.address', login: 'test-login' },
  });
  expect(dispatch).toHaveBeenCalledWith({
    type: 'UPDATE_USER_DATA',
    user: { apiKey: 'test-api-key' },
  });
  expect(await screen.findByRole('error')).toBeInTheDocument();
});
