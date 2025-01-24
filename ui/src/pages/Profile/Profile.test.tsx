import { screen } from '@testing-library/react';
import { UserState } from 'contexts';
import { UserContext } from 'contexts/UserContext/User.context';
import { renderWithClient } from 'tests';
import { Profile } from './Profile';

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

beforeEach(() => {
  vi.clearAllMocks();
});

test('renders headers', () => {
  renderWithClient(
    <UserContext.Provider value={{ state: mockState, dispatch }}>
      <Profile />
    </UserContext.Provider>
  );

  expect(screen.getByText('Profile details')).toBeInTheDocument();
  expect(screen.getByText('Password details')).toBeInTheDocument();
});
