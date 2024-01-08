import { render, screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { UserContext, UserState, initialUserState } from "contexts";
import { Top } from "./Top";

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
  render(
    <MemoryRouter initialEntries={[""]}>
      <UserContext.Provider value={{ state: initialUserState, dispatch }}>
        <Top />
      </UserContext.Provider>
    </MemoryRouter>,
  );

  expect(screen.getByText("Bootzooka")).toBeInTheDocument();
});

test("renders nav bar unlogged user", () => {
  render(
    <MemoryRouter initialEntries={["/main"]}>
      <UserContext.Provider value={{ state: initialUserState, dispatch }}>
        <Top />
      </UserContext.Provider>
    </MemoryRouter>,
  );

  expect(screen.getByText("Welcome")).toBeInTheDocument();
  expect(screen.getByText("Home")).toBeInTheDocument();
  expect(screen.getByText("Login")).toBeInTheDocument();
  expect(screen.getByText("Register")).toBeInTheDocument();
});

test("renders nav bar for logged user", () => {
  render(
    <MemoryRouter initialEntries={["/main"]}>
      <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
        <Top />
      </UserContext.Provider>
    </MemoryRouter>,
  );

  expect(screen.getByText("Welcome")).toBeInTheDocument();
  expect(screen.getByText("Home")).toBeInTheDocument();
  expect(screen.getByText("user-login")).toBeInTheDocument();
  expect(screen.getByText("Logout")).toBeInTheDocument();
});

test("handles logout logged user", async () => {
  render(
    <MemoryRouter initialEntries={["/main"]}>
      <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
        <Top />
      </UserContext.Provider>
    </MemoryRouter>,
  );

  await userEvent.click(screen.getByText("Logout"));

  expect(dispatch).toHaveBeenCalledWith({ type: "LOG_OUT" });
});
