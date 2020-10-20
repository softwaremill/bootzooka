import React from "react";
import { render } from "@testing-library/react";
import ProtectedRoute from "./ProtectedRoute";
import { UserContext, UserState, initialUserState } from "../../contexts/UserContext/UserContext";
import { Router } from "react-router-dom";
import { createMemoryHistory } from "history";

const loggedUserState: UserState = {
  ...initialUserState,
  loggedIn: true,
};

const dispatch = jest.fn();

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders protected route for unlogged user", () => {
  const history = createMemoryHistory({ initialEntries: ["/test-route"] });

  const { getByText } = render(
    <Router history={history}>
      <UserContext.Provider value={{ state: initialUserState, dispatch }}>
        <ProtectedRoute path="/test-route">
          <>Protected Text</>
        </ProtectedRoute>
      </UserContext.Provider>
    </Router>
  );

  expect(getByText("Please sign in")).toBeInTheDocument();
});

test("renders protected route for logged user", () => {
  const history = createMemoryHistory({ initialEntries: ["/test-route"] });

  const { getByText } = render(
    <Router history={history}>
      <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
        <ProtectedRoute path="/test-route">
          <>Protected Text</>
        </ProtectedRoute>
      </UserContext.Provider>
    </Router>
  );

  expect(getByText("Protected Text")).toBeInTheDocument();
});
