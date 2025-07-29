import { UserContext } from 'contexts/UserContext/User.context';
import { initialUserState } from 'contexts/UserContext/UserContext.constants';
import { Login } from 'pages';
import { MemoryRouter, Route, Routes } from 'react-router';
import { renderWithClient } from 'tests';
import { PublicOnlyRoute } from './PublicOnlyRoute';
import { screen } from '@testing-library/react';

const dispatch = vi.fn();

test('<PublicOnlyRoute /> should not render login page only for logged-in', () => {
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

test('<PublicOnlyRoute /> should render login page to anonymous user', () => {
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
