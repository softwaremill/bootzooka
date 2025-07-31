import { screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router';
import { UserContext } from 'contexts/UserContext/User.context';
import { initialUserState } from 'contexts/UserContext/UserContext.constants';
import { renderWithClient } from 'tests';
import { Login } from 'pages';
import { ProtectedRoute } from './ProtectedRoute';

const dispatch = vi.fn();

beforeEach(() => {
  vi.clearAllMocks();
});

test('<ProtectedRoute /> should not render protected route for anonymous user', () => {
  renderWithClient(
    <MemoryRouter initialEntries={['']}>
      <UserContext.Provider value={{ state: initialUserState, dispatch }}>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route element={<ProtectedRoute />}>
            <Route index element={<>Protected Text</>} />
          </Route>
        </Routes>
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(screen.getByText('Please sign in')).toBeInTheDocument();
});

test('<ProtectedRoute /> should render protected route to a logged-in user', () => {
  renderWithClient(
    <MemoryRouter initialEntries={['/protected-page']}>
      <UserContext.Provider
        value={{
          state: {
            user: {
              createdOn: '2023-10-01T12:00:00Z',
              login: 'test-user',
              email: 'test-user@example.com',
            },
          },
          dispatch,
        }}
      >
        <Routes>
          <Route path="/protected-page" element={<ProtectedRoute />}>
            <Route index element={<>Protected Text</>} />
          </Route>
          <Route path="/login" element={<>Login page</>} />
        </Routes>
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(screen.getByText('Protected Text')).toBeVisible();
});
