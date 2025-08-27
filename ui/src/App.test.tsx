import { screen } from '@testing-library/react';
import { App } from './App';
import { renderWithClient } from './tests';
import { UserContext } from './contexts/UserContext/User.context';
import { initialUserState } from './contexts/UserContext/UserContext.constants';

const dispatch = vi.fn();

beforeEach(() => {
  vi.clearAllMocks();
});

test('<App /> shoud show the app for anonymous users', async () => {
  renderWithClient(
    <UserContext.Provider value={{ state: { ...initialUserState }, dispatch }}>
      <App />
    </UserContext.Provider>
  );

  expect(await screen.findByText('Welcome to Bootzooka!')).toBeVisible();
});

test('<App /> shoud show the app for logged-in users', async () => {
  renderWithClient(
    <UserContext.Provider
      value={{
        state: {
          user: {
            login: 'user-login',
            email: 'email@address.pl',
            createdOn: '2020-10-09T09:57:17.995288Z',
          },
        },
        dispatch,
      }}
    >
      <App />
    </UserContext.Provider>
  );

  expect(await screen.findByText('Welcome to Bootzooka!')).toBeVisible();
});
