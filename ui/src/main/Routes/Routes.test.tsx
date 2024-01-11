import { screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { UserContext, UserState, initialUserState } from "contexts";
import { Routes } from "./Routes";
import { renderWithClient } from "tests";

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
  renderWithClient(
    <MemoryRouter initialEntries={[""]}>
      <UserContext.Provider value={{ state: initialUserState, dispatch }}>
        <Routes />
      </UserContext.Provider>
    </MemoryRouter>,
  );

  expect(screen.getByText("Welcome to Bootzooka!")).toBeInTheDocument();
});

test("renders protected route for unlogged user", () => {
  renderWithClient(
    <MemoryRouter initialEntries={["/main"]}>
      <UserContext.Provider value={{ state: initialUserState, dispatch }}>
        <Routes />
      </UserContext.Provider>
    </MemoryRouter>,
  );

  expect(screen.getByText("Please sign in")).toBeInTheDocument();
});

test("renders protected route for logged user", () => {
  renderWithClient(
    <MemoryRouter initialEntries={["/main"]}>
      <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
        <Routes />
      </UserContext.Provider>
    </MemoryRouter>,
  );

  expect(screen.getByText("Shhhh, this is a secret place.")).toBeInTheDocument();
});

test("renders not found page", () => {
  renderWithClient(
    <MemoryRouter initialEntries={["/not-specified-route"]}>
      <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
        <Routes />
      </UserContext.Provider>
    </MemoryRouter>,
  );

  expect(screen.getByText("You shouldn't be here for sure :)")).toBeInTheDocument();
});
