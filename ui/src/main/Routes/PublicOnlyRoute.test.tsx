import { UserContext } from 'contexts/UserContext/User.context';
import { initialUserState } from 'contexts/UserContext/UserContext.constants';
import { Login } from 'pages';
import { MemoryRouter, Route, Routes } from 'react-router';
import { renderWithClient } from 'tests';
import { PublicOnlyRoute } from './PublicOnlyRoute';
import { screen } from '@testing-library/react';

const dispatch = vi.fn();

test('<PublicOnlyRoute /> should render login page for anonymous', () => {
  renderWithClient(
    <MemoryRouter initialEntries={['/login']}>
      <UserContext.Provider value={{ state: initialUserState, dispatch }}>
        <Routes>
          <Route element={<PublicOnlyRoute />}>
            <Route path="/login" element={<Login />} />
          </Route>
          <Route index element={<>Public Text</>} />
        </Routes>
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(screen.getByText('Please sign in')).toBeVisible();
});

test('<PublicOnlyRoute /> should not render login page to logged in users', () => {
  renderWithClient(
    <MemoryRouter initialEntries={['/login']}>
      <UserContext.Provider
        value={{
          state: {
            user: {
              login: 'test-user',
              email: 'test@example.com',
              createdOn: '2020-10-09T09:57:17.995288Z',
            },
          },
          dispatch,
        }}
      >
        <Routes>
          <Route element={<PublicOnlyRoute />}>
            <Route path="/login" element={<Login />} />
          </Route>
          <Route path="*" element={<>Public Text</>} />
        </Routes>
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(screen.getByText('Public Text')).toBeVisible();
});
