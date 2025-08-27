import { UserState } from '@/contexts';
import { AppNavbar } from '.';
import { renderWithClient } from '@/tests';
import { initialUserState } from '@/contexts/UserContext/UserContext.constants';
import { MemoryRouter } from 'react-router';
import { UserContext } from '@/contexts/UserContext/User.context';
import { screen } from '@testing-library/dom';
import userEvent from '@testing-library/user-event';

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
  usePostUserLogout: () => ({
    mutateAsync: mockMutate,
  }),
}));

beforeEach(() => {
  localStorage.clear();
  vi.clearAllMocks();
});

test('<AppNavbar /> shoud render the brand name', () => {
  renderWithClient(
    <MemoryRouter initialEntries={['']}>
      <UserContext.Provider value={{ state: initialUserState, dispatch }}>
        <AppNavbar />
      </UserContext.Provider>
    </MemoryRouter>
  );

  screen
    .getAllByText('Bootzooka')
    .forEach((el) => expect(el).toBeInTheDocument());
});

test('<AppNavbar /> should render the nav bar for unlogged user', () => {
  renderWithClient(
    <MemoryRouter initialEntries={['/main']}>
      <UserContext.Provider value={{ state: initialUserState, dispatch }}>
        <AppNavbar />
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(screen.getByText('Welcome')).toBeVisible();
  expect(screen.getByText('Home')).toBeVisible();
  expect(screen.getByText('Login')).toBeVisible();
  expect(screen.getByText('Register')).toBeVisible();
});

test('<AppNavbar /> should render the nav bar for logged-in user', () => {
  localStorage.setItem('apiKey', '{ "apiKey": "test-api-key" }');
  renderWithClient(
    <MemoryRouter initialEntries={['/main']}>
      <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
        <AppNavbar />
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(screen.getByText('Welcome')).toBeVisible();
  expect(screen.getByText('Home')).toBeVisible();
  expect(screen.getByText('user-login')).toBeVisible();
  expect(screen.getByText('Logout')).toBeVisible();
});

test('<AppNavbar /> should handle the logout for logged-in user', async () => {
  localStorage.setItem('apiKey', '{ "apiKey": "test-api-key" }');
  renderWithClient(
    <MemoryRouter initialEntries={['/main']}>
      <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
        <AppNavbar />
      </UserContext.Provider>
    </MemoryRouter>
  );
  await userEvent.click(screen.getByText(/logout/i));
  expect(mockMutate).toHaveBeenCalledTimes(1);
  expect(mockMutate).toHaveBeenCalledWith({ body: { apiKey: 'test-api-key' } });
});

test('<AppNavbar /> should render login and logout items for anonymous users', () => {
  renderWithClient(
    <MemoryRouter initialEntries={['/main']}>
      <UserContext.Provider value={{ state: initialUserState, dispatch }}>
        <AppNavbar />
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(screen.queryByText('user-login')).not.toBeInTheDocument();
  expect(screen.queryByText('Logout')).not.toBeInTheDocument();
  expect(screen.getByText('Login')).toBeVisible();
  expect(screen.getByText('Register')).toBeVisible();
});
