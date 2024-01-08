import { render, screen, waitFor } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { MemoryRouter, unstable_HistoryRouter as HistoryRouter } from "react-router-dom";
import { createMemoryHistory } from "@remix-run/router";
import { UserContext, initialUserState } from "contexts";
import { userService } from "services";
import { Register } from "./Register";
import { mockAndDelayRejectedValueOnce, mockAndDelayResolvedValueOnce } from "../../setupTests";

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
        <Register />
      </UserContext.Provider>
    </MemoryRouter>,
  );

  expect(screen.getByText("Please sign up")).toBeInTheDocument();
});

test("redirects when registered", () => {
  render(
    <HistoryRouter history={history}>
      <UserContext.Provider value={{ state: { ...initialUserState, loggedIn: true }, dispatch }}>
        <Register />
      </UserContext.Provider>
    </HistoryRouter>,
  );

  expect(history.location.pathname).toEqual("/main");
});

test("handles register success", async () => {
  mockAndDelayResolvedValueOnce(userService.registerUser as jest.Mock, { apiKey: "test-api-key" });

  render(
    <HistoryRouter history={history}>
      <UserContext.Provider value={{ state: { ...initialUserState, loggedIn: false }, dispatch }}>
        <Register />
      </UserContext.Provider>
    </HistoryRouter>,
  );

  await userEvent.type(screen.getByLabelText("Login"), "test-login");
  await userEvent.type(screen.getByLabelText("Email address"), "test@email.address.pl");
  await userEvent.type(screen.getByLabelText("Password"), "test-password");
  await userEvent.type(screen.getByLabelText("Repeat password"), "test-password");
  await userEvent.click(screen.getByText("Create new account"));

  await screen.findByRole("loader");

  expect(userService.registerUser).toHaveBeenCalledWith({
    login: "test-login",
    email: "test@email.address.pl",
    password: "test-password",
  });

  await waitFor(() => expect(dispatch).toHaveBeenCalledWith({ apiKey: "test-api-key", type: "SET_API_KEY" }));
});

test("handles register error", async () => {
  mockAndDelayRejectedValueOnce(userService.registerUser as jest.Mock, new Error("Test Error"));

  render(
    <MemoryRouter initialEntries={["/login"]}>
      <UserContext.Provider value={{ state: { ...initialUserState, loggedIn: false }, dispatch }}>
        <Register />
      </UserContext.Provider>
    </MemoryRouter>,
  );

  await userEvent.type(screen.getByLabelText("Login"), "test-login");
  await userEvent.type(screen.getByLabelText("Email address"), "test@email.address.pl");
  await userEvent.type(screen.getByLabelText("Password"), "test-password");
  await userEvent.type(screen.getByLabelText("Repeat password"), "test-password");
  await userEvent.click(screen.getByText("Create new account"));

  await screen.findByRole("loader");

  expect(userService.registerUser).toHaveBeenCalledWith({
    login: "test-login",
    email: "test@email.address.pl",
    password: "test-password",
  });
  expect(dispatch).not.toHaveBeenCalled();

  await screen.findByRole("error");
  await screen.findByText("Test Error");
});
