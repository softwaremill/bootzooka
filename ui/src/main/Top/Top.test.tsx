import { screen } from '@testing-library/react';
import { userEvent } from '@testing-library/user-event';
import { MemoryRouter } from 'react-router';
import { UserState } from 'contexts';
import { UserContext } from 'contexts/UserContext/User.context';
import { initialUserState } from 'contexts/UserContext/UserContext.constants';
import { Top } from './Top';
import { renderWithClient } from '../../tests';

const loggedUserState: UserState = {
  user: {
    login: 'user-login',
    email: 'email@address.pl',
    createdOn: '2020-10-09T09:57:17.995288Z',
  },
};

const dispatch = vi.fn();
const mockMutate = vi.fn();

vi.mock('api/apiComponents', () => ({
  usePostUserLogout: () => ({
    mutateAsync: mockMutate,
  }),
}));

beforeEach(() => {
  localStorage.clear();
  vi.clearAllMocks();
});

test('<Top /> shoud render the brand name', () => {
  renderWithClient(
    <MemoryRouter initialEntries={['']}>
      <UserContext.Provider value={{ state: initialUserState, dispatch }}>
        <Top />
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(screen.getByText('Bootzooka')).toBeInTheDocument();
});

test('<Top /> should render the nav bar for unlogged user', () => {
  renderWithClient(
    <MemoryRouter initialEntries={['/main']}>
      <UserContext.Provider value={{ state: initialUserState, dispatch }}>
        <Top />
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(screen.getByText('Welcome')).toBeInTheDocument();
  expect(screen.getByText('Home')).toBeInTheDocument();
  expect(screen.getByText('Login')).toBeInTheDocument();
  expect(screen.getByText('Register')).toBeInTheDocument();
});

test('<Top /> should render the nav bar for logged-in user', () => {
  localStorage.setItem('apiKey', '{ "apiKey": "test-api-key" }');
  renderWithClient(
    <MemoryRouter initialEntries={['/main']}>
      <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
        <Top />
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(screen.getByText('Welcome')).toBeVisible();
  expect(screen.getByText('Home')).toBeVisible();
  expect(screen.getByText('user-login')).toBeVisible();
  expect(screen.getByText('Logout')).toBeVisible();
});

test('<Top /> should handle the logout for logged-in user', async () => {
  localStorage.setItem('apiKey', '{ "apiKey": "test-api-key" }');
  renderWithClient(
    <MemoryRouter initialEntries={['/main']}>
      <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
        <Top />
      </UserContext.Provider>
    </MemoryRouter>
  );
  await userEvent.click(screen.getByText(/logout/i));
  expect(mockMutate).toHaveBeenCalledTimes(1);
  expect(mockMutate).toHaveBeenCalledWith({ body: { apiKey: 'test-api-key' } });
});

test('<Top /> should render login and logout items for anonymous users', () => {
  renderWithClient(
    <MemoryRouter initialEntries={['/main']}>
      <UserContext.Provider value={{ state: initialUserState, dispatch }}>
        <Top />
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(screen.queryByText('user-login')).not.toBeInTheDocument();
  expect(screen.queryByText('Logout')).not.toBeInTheDocument();
  expect(screen.getByText('Login')).toBeVisible();
  expect(screen.getByText('Register')).toBeVisible();
});
