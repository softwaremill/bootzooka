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

test('<Main /> should show the app for logged-in users', () => {
  renderWithClient(
    <MemoryRouter initialEntries={['']}>
      <UserContext.Provider
        value={{ state: { ...initialUserState }, dispatch }}
      >
        <Main />
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(screen.getByText('Welcome to Bootzooka!')).toBeInTheDocument();
});

test('<Main /> should show the app for logged-out users', () => {
  renderWithClient(
    <MemoryRouter initialEntries={['']}>
      <UserContext.Provider
        value={{ state: { ...initialUserState }, dispatch }}
      >
        <Main />
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(screen.getByText('Welcome to Bootzooka!')).toBeInTheDocument();
});
