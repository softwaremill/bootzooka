import React from "react";
import { render } from "@testing-library/react";
import Main from "./Main";
import { UserContext, initialUserState } from "../../contexts/UserContext/UserContext";
import { Router } from "react-router-dom";
import { createMemoryHistory } from "history";

const history = createMemoryHistory({ initialEntries: [""] });

const dispatch = jest.fn();

beforeEach(() => {
  jest.clearAllMocks();
});

test("shows loader on unspecified logged in status", () => {
  const { getByRole } = render(
    <Router history={history}>
      <UserContext.Provider value={{ state: initialUserState, dispatch }}>
        <Main />
      </UserContext.Provider>
    </Router>
  );

  expect(getByRole("loader")).toBeInTheDocument();
});

test("shows app on logged in status", () => {
  const { getByText } = render(
    <Router history={history}>
      <UserContext.Provider value={{ state: { ...initialUserState, loggedIn: true }, dispatch }}>
        <Main />
      </UserContext.Provider>
    </Router>
  );

  expect(getByText("Welcome to Bootzooka!")).toBeInTheDocument();
});

test("shows app on logged out status", () => {
  const { getByText } = render(
    <Router history={history}>
      <UserContext.Provider value={{ state: { ...initialUserState, loggedIn: false }, dispatch }}>
        <Main />
      </UserContext.Provider>
    </Router>
  );

  expect(getByText("Welcome to Bootzooka!")).toBeInTheDocument();
});
