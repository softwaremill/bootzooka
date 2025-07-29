import { screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { UserState } from 'contexts';
import { UserContext } from 'contexts/UserContext/User.context';
import { initialUserState } from 'contexts/UserContext/UserContext.constants';
import { renderWithClient } from 'tests';
import { Routes } from './Routes';

const loggedUserState: UserState = {
  user: {
    login: 'user-login',
    email: 'email@address.pl',
    createdOn: '2020-10-09T09:57:17.995288Z',
  },
};

const dispatch = vi.fn();

beforeEach(() => {
  vi.clearAllMocks();
});

test('<Routes /> should render the main route', () => {
  renderWithClient(
    <MemoryRouter initialEntries={['']}>
      <UserContext.Provider value={{ state: initialUserState, dispatch }}>
        <Routes />
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(screen.getByText('Welcome to Bootzooka!')).toBeInTheDocument();
});

test('<Routes /> should render protected route for unlogged user', () => {
  renderWithClient(
    <MemoryRouter initialEntries={['/main']}>
      <UserContext.Provider value={{ state: initialUserState, dispatch }}>
        <Routes />
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(screen.getByText('Please sign in')).toBeInTheDocument();
});

test('<Routes /> should render protected route for logged-in user', () => {
  renderWithClient(
    <MemoryRouter initialEntries={['/main']}>
      <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
        <Routes />
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(
    screen.getByText('Shhhh, this is a secret place.')
  ).toBeInTheDocument();
});

test('<Routes /> should render not found page', () => {
  renderWithClient(
    <MemoryRouter initialEntries={['/not-specified-route']}>
      <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
        <Routes />
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(
    screen.getByText("You shouldn't be here for sure :)")
  ).toBeInTheDocument();
});
