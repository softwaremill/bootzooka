import React from "react";
import { render } from "@testing-library/react";
import Routes from "./Routes";
import { UserContext, UserState, initialUserState } from "../../contexts/UserContext/UserContext";
import { Router } from "react-router-dom";
import { createMemoryHistory } from "history";

const loggedUserState: UserState = {
  apiKey: "test-api-key",
  user: { login: "user-login", email: "email@address.pl", createdOn: "2020-10-09T09:57:17.995288Z" },
  loggedIn: true,
};

const dispatch = jest.fn();

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders main route", () => {
  const history = createMemoryHistory({ initialEntries: [""] });

  const { getByText } = render(
    <Router history={history}>
      <UserContext.Provider value={{ state: initialUserState, dispatch }}>
        <Routes />
      </UserContext.Provider>
    </Router>
  );

  expect(getByText("Welcome to Bootzooka!")).toBeInTheDocument();
});

test("renders protected route for unlogged user", () => {
  const history = createMemoryHistory({ initialEntries: ["/main"] });

  const { getByText } = render(
    <Router history={history}>
      <UserContext.Provider value={{ state: initialUserState, dispatch }}>
        <Routes />
      </UserContext.Provider>
    </Router>
  );

  expect(getByText("Please sign in")).toBeInTheDocument();
});

test("renders protected route for logged user", () => {
  const history = createMemoryHistory({ initialEntries: ["/main"] });

  const { getByText } = render(
    <Router history={history}>
      <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
        <Routes />
      </UserContext.Provider>
    </Router>
  );

  expect(getByText("Shhhh, this is a secret place.")).toBeInTheDocument();
});

test("renders not found page", () => {
  const history = createMemoryHistory({ initialEntries: ["/not-specified-route"] });

  const { getByText } = render(
    <Router history={history}>
      <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
        <Routes />
      </UserContext.Provider>
    </Router>
  );

  expect(getByText("You shouldn't be here for sure :)")).toBeInTheDocument();
});
