import { screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { MemoryRouter, unstable_HistoryRouter as HistoryRouter } from "react-router-dom";
import { createMemoryHistory } from "@remix-run/router";
import { Login } from "./Login";
import { UserContext, initialUserState } from "contexts";
import { renderWithClient } from "tests";

const history = createMemoryHistory({ initialEntries: ["/login"] });
const dispatch = jest.fn();

const onLogin = jest.fn();

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders header", () => {
  renderWithClient(
    <MemoryRouter initialEntries={["/login"]}>
      <UserContext.Provider value={{ state: { ...initialUserState, loggedIn: false }, dispatch }}>
        <Login />
      </UserContext.Provider>
    </MemoryRouter>,
  );

  expect(screen.getByText("Please sign in")).toBeInTheDocument();
});

test("redirects when logged in", () => {
  renderWithClient(
    <HistoryRouter history={history}>
      <UserContext.Provider value={{ state: { ...initialUserState, loggedIn: true }, dispatch }}>
        <Login />
      </UserContext.Provider>
    </HistoryRouter>,
  );

  expect(history.location.pathname).toEqual("/main");
});

test("handles login success", async () => {
  onLogin.mockResolvedValueOnce({ apiKey: "test-api-key" });

  renderWithClient(
    <HistoryRouter history={history}>
      <UserContext.Provider value={{ state: { ...initialUserState, loggedIn: false }, dispatch }}>
        <Login />
      </UserContext.Provider>
    </HistoryRouter>,
  );

  await userEvent.type(screen.getByLabelText("Login or email"), "test-login");
  await userEvent.type(screen.getByLabelText("Password"), "test-password");
  await userEvent.click(screen.getByText("Sign In"));

  await screen.findByRole("success");

  expect(onLogin).toHaveBeenCalledWith({ loginOrEmail: "test-login", password: "test-password" });
  expect(dispatch).toHaveBeenCalledWith({ apiKey: "test-api-key", type: "SET_API_KEY" });
});

test("handles login error", async () => {
  onLogin.mockRejectedValueOnce(new Error("Test Error"));

  renderWithClient(
    <MemoryRouter initialEntries={["/login"]}>
      <UserContext.Provider value={{ state: { ...initialUserState, loggedIn: false }, dispatch }}>
        <Login />
      </UserContext.Provider>
    </MemoryRouter>,
  );

  await userEvent.type(screen.getByLabelText("Login or email"), "test-login");
  await userEvent.type(screen.getByLabelText("Password"), "test-password");
  await userEvent.click(screen.getByText("Sign In"));

  await screen.findByRole("error");

  expect(onLogin).toHaveBeenCalledWith({ loginOrEmail: "test-login", password: "test-password" });
  expect(dispatch).not.toHaveBeenCalled();
});
