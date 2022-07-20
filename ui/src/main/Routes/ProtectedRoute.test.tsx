import { render } from "@testing-library/react";
import { Routes, Route } from "react-router-dom";
import ProtectedRoute from "./ProtectedRoute";
import Login from "../../pages/Login/Login";
import { UserContext, UserState, initialUserState } from "../../contexts/UserContext/UserContext";
import { MemoryRouter } from "react-router-dom";

const dispatch = jest.fn();

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders protected route for unlogged user", () => {
  const { getByText } = render(
    <MemoryRouter initialEntries={[""]}>
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

  expect(getByText("Please sign in")).toBeInTheDocument();
});

test("renders protected route for logged user", () => {
  const loggedUserState: UserState = {
    ...initialUserState,
    loggedIn: true,
  };

  const { getByText } = render(
    <MemoryRouter initialEntries={[""]}>
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

  expect(getByText("Protected Text")).toBeInTheDocument();
});
