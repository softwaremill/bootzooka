import { screen } from "@testing-library/react";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import { UserContext, UserState, initialUserState } from "contexts";
import { ProtectedRoute } from "./ProtectedRoute";
import { Login } from "pages";
import { renderWithClient } from "tests";

const dispatch = jest.fn();

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders protected route for unlogged user", () => {
  renderWithClient(
    <MemoryRouter initialEntries={[""]}>
      <UserContext.Provider value={{ state: initialUserState, dispatch }}>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route element={<ProtectedRoute />}>
            <Route index element={<>Protected Text</>} />
          </Route>
        </Routes>
      </UserContext.Provider>
    </MemoryRouter>,
  );

  expect(screen.getByText("Please sign in")).toBeInTheDocument();
});

test("renders protected route for logged user", () => {
  const loggedUserState: UserState = {
    ...initialUserState,
    loggedIn: true,
  };

  renderWithClient(
    <MemoryRouter initialEntries={[""]}>
      <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route element={<ProtectedRoute />}>
            <Route index element={<>Protected Text</>} />
          </Route>
        </Routes>
      </UserContext.Provider>
    </MemoryRouter>,
  );

  expect(screen.getByText("Protected Text")).toBeInTheDocument();
});
