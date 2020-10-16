import React from "react";
import { render, fireEvent } from "@testing-library/react";
import Top from "./Top";
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

test("renders brand name", () => {
  const history = createMemoryHistory({ initialEntries: [""] });

  const { getByText } = render(
    <Router history={history}>
      <UserContext.Provider value={{ state: initialUserState, dispatch }}>
        <Top />
      </UserContext.Provider>
    </Router>
  );

  expect(getByText("Bootzooka")).toBeInTheDocument();
});

test("renders nav bar unlogged user", () => {
  const history = createMemoryHistory({ initialEntries: ["/main"] });

  const { getByText } = render(
    <Router history={history}>
      <UserContext.Provider value={{ state: initialUserState, dispatch }}>
        <Top />
      </UserContext.Provider>
    </Router>
  );

  expect(getByText("Welcome")).toBeInTheDocument();
  expect(getByText("Home")).toBeInTheDocument();
  expect(getByText("Login")).toBeInTheDocument();
  expect(getByText("Register")).toBeInTheDocument();
});

test("renders nav bar for logged user", () => {
  const history = createMemoryHistory({ initialEntries: ["/main"] });

  const { getByText } = render(
    <Router history={history}>
      <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
        <Top />
      </UserContext.Provider>
    </Router>
  );

  expect(getByText("Welcome")).toBeInTheDocument();
  expect(getByText("Home")).toBeInTheDocument();
  expect(getByText("user-login")).toBeInTheDocument();
  expect(getByText("Logout")).toBeInTheDocument();
});

test("handles logout logged user", () => {
  const history = createMemoryHistory({ initialEntries: ["/main"] });

  const { getByText } = render(
    <Router history={history}>
      <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
        <Top />
      </UserContext.Provider>
    </Router>
  );

  fireEvent.click(getByText("Logout"));

  expect(dispatch).toBeCalledWith({ type: "LOG_OUT" });
});
