import { render, screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { MemoryRouter, unstable_HistoryRouter as HistoryRouter } from "react-router-dom";
import { createMemoryHistory } from "@remix-run/router";
import { Login } from "./Login";
import { UserContext, initialUserState } from "contexts";
import { userService } from "services";

const history = createMemoryHistory({ initialEntries: ["/login"] });
const dispatch = jest.fn();

jest.mock("services");

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders header", () => {
  render(
    <MemoryRouter initialEntries={["/login"]}>
      <UserContext.Provider value={{ state: { ...initialUserState, loggedIn: false }, dispatch }}>
        <Login />
      </UserContext.Provider>
    </MemoryRouter>,
  );

  expect(screen.getByText("Please sign in")).toBeInTheDocument();
});

test("redirects when logged in", () => {
  render(
    <HistoryRouter history={history}>
      <UserContext.Provider value={{ state: { ...initialUserState, loggedIn: true }, dispatch }}>
        <Login />
      </UserContext.Provider>
    </HistoryRouter>,
  );

  expect(history.location.pathname).toEqual("/main");
});

test("handles login success", async () => {
  (userService.login as jest.Mock).mockResolvedValueOnce({
    apiKey: "test-api-key",
  });

  render(
    <HistoryRouter history={history}>
      <UserContext.Provider value={{ state: { ...initialUserState, loggedIn: false }, dispatch }}>
        <Login />
      </UserContext.Provider>
    </HistoryRouter>,
  );

  await userEvent.type(screen.getByLabelText("Login or email"), "test-login");
  await userEvent.type(screen.getByLabelText("Password"), "test-password");
  await userEvent.click(screen.getByText("Sign In"));

  await screen.findByRole("loader");

  expect(userService.login).toHaveBeenCalledWith({ loginOrEmail: "test-login", password: "test-password" });
  expect(dispatch).toHaveBeenCalledTimes(1);
  expect(dispatch).toHaveBeenCalledWith({ apiKey: "test-api-key", type: "SET_API_KEY" });
  expect(history.location.pathname).toEqual("/main");
});

test("handles login error", async () => {
  const testError = new Error("Test Error");
  (userService.login as jest.Mock).mockRejectedValueOnce(testError);

  render(
    <MemoryRouter initialEntries={["/login"]}>
      <UserContext.Provider value={{ state: { ...initialUserState, loggedIn: false }, dispatch }}>
        <Login />
      </UserContext.Provider>
    </MemoryRouter>,
  );

  await userEvent.type(screen.getByLabelText("Login or email"), "test-login");
  await userEvent.type(screen.getByLabelText("Password"), "test-password");
  await userEvent.click(screen.getByText("Sign In"));

  await screen.findByRole("loader");

  expect(userService.login).toHaveBeenCalledWith({ loginOrEmail: "test-login", password: "test-password" });
  expect(dispatch).not.toHaveBeenCalled();
  expect(screen.getByText("Test Error")).toBeInTheDocument();
});
