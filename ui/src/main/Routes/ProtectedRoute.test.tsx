import { screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router';
import { UserState } from 'contexts';
import { UserContext } from 'contexts/UserContext/User.context';
import { initialUserState } from 'contexts/UserContext/UserContext.constants';
import { renderWithClient } from 'tests';
import { Login } from 'pages';
import { ProtectedRoute } from './ProtectedRoute';

const dispatch = vi.fn();

beforeEach(() => {
  vi.clearAllMocks();
});

test('renders protected route for unlogged user', () => {
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

test('renders protected route for logged user', () => {
  const loggedUserState: UserState = {
    ...initialUserState,
    loggedIn: true,
  };

  renderWithClient(
    <MemoryRouter initialEntries={['']}>
      <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route element={<ProtectedRoute />}>
            <Route index element={<>Protected Text</>} />
          </Route>
        </Routes>
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(screen.getByText('Protected Text')).toBeInTheDocument();
});
