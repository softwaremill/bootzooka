import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { UserContext, initialUserState } from "contexts";
import { Main } from "./Main";

const dispatch = jest.fn();

beforeEach(() => {
  jest.clearAllMocks();
});

test("shows loader on unspecified logged in status", () => {
  render(
    <MemoryRouter initialEntries={[""]}>
      <UserContext.Provider value={{ state: initialUserState, dispatch }}>
        <Main />
      </UserContext.Provider>
    </MemoryRouter>,
  );

  expect(screen.getByRole("loader")).toBeInTheDocument();
});

test("shows app on logged in status", () => {
  render(
    <MemoryRouter initialEntries={[""]}>
      <UserContext.Provider value={{ state: { ...initialUserState, loggedIn: true }, dispatch }}>
        <Main />
      </UserContext.Provider>
    </MemoryRouter>,
  );

  expect(screen.getByText("Welcome to Bootzooka!")).toBeInTheDocument();
});

test("shows app on logged out status", () => {
  render(
    <MemoryRouter initialEntries={[""]}>
      <UserContext.Provider value={{ state: { ...initialUserState, loggedIn: false }, dispatch }}>
        <Main />
      </UserContext.Provider>
    </MemoryRouter>,
  );

  expect(screen.getByText("Welcome to Bootzooka!")).toBeInTheDocument();
});
