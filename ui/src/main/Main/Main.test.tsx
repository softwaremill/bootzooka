import { screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { UserContext } from 'contexts/UserContext/User.context';
import { initialUserState } from 'contexts/UserContext/UserContext.constants';
import { renderWithClient } from 'tests';
import { Main } from './Main';

const dispatch = vi.fn();

beforeEach(() => {
  vi.clearAllMocks();
});

test('shows loader on unspecified logged in status', () => {
  renderWithClient(
    <MemoryRouter initialEntries={['']}>
      <UserContext.Provider value={{ state: initialUserState, dispatch }}>
        <Main />
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(screen.getByRole('loader')).toBeInTheDocument();
});

test('shows app on logged in status', () => {
  renderWithClient(
    <MemoryRouter initialEntries={['']}>
      <UserContext.Provider
        value={{ state: { ...initialUserState, loggedIn: true }, dispatch }}
      >
        <Main />
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(screen.getByText('Welcome to Bootzooka!')).toBeInTheDocument();
});

test('shows app on logged out status', () => {
  renderWithClient(
    <MemoryRouter initialEntries={['']}>
      <UserContext.Provider
        value={{ state: { ...initialUserState, loggedIn: false }, dispatch }}
      >
        <Main />
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(screen.getByText('Welcome to Bootzooka!')).toBeInTheDocument();
});
