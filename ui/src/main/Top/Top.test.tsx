import { render, fireEvent } from "@testing-library/react";
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
  const { getByText } = render(
    <MemoryRouter initialEntries={[""]}>
      <UserContext.Provider value={{ state: initialUserState, dispatch }}>
        <Top />
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(getByText("Bootzooka")).toBeInTheDocument();
});

test("renders nav bar unlogged user", () => {
  const { getByText } = render(
    <MemoryRouter initialEntries={["/main"]}>
      <UserContext.Provider value={{ state: initialUserState, dispatch }}>
        <Top />
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(getByText("Welcome")).toBeInTheDocument();
  expect(getByText("Home")).toBeInTheDocument();
  expect(getByText("Login")).toBeInTheDocument();
  expect(getByText("Register")).toBeInTheDocument();
});

test("renders nav bar for logged user", () => {
  const { getByText } = render(
    <MemoryRouter initialEntries={["/main"]}>
      <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
        <Top />
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(getByText("Welcome")).toBeInTheDocument();
  expect(getByText("Home")).toBeInTheDocument();
  expect(getByText("user-login")).toBeInTheDocument();
  expect(getByText("Logout")).toBeInTheDocument();
});

test("handles logout logged user", () => {
  const { getByText } = render(
    <MemoryRouter initialEntries={["/main"]}>
      <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
        <Top />
      </UserContext.Provider>
    </MemoryRouter>
  );

  fireEvent.click(getByText("Logout"));

  expect(dispatch).toBeCalledWith({ type: "LOG_OUT" });
});
