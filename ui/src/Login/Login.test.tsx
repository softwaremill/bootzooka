import React from "react";
import { render, fireEvent } from "@testing-library/react";
import Login from "./Login";
import { AppContext, initialAppState } from "../AppContext/AppContext";
import { Router } from "react-router-dom";
import { createMemoryHistory } from "history";
import userService from "../UserService/UserService";

const history = createMemoryHistory({ initialEntries: ["/login"] });

jest.mock("../UserService/UserService");
console.error = jest.fn();
const dispatch = jest.fn();

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders header", () => {
  const { getByText } = render(
    <Router history={history}>
      <AppContext.Provider value={{ state: { ...initialAppState, loggedIn: false }, dispatch }}>
        <Login />
      </AppContext.Provider>
    </Router>
  );

  expect(getByText("Please sign in")).toBeInTheDocument();
});

test("redirects when logged in", () => {
  render(
    <Router history={history}>
      <AppContext.Provider value={{ state: { ...initialAppState, loggedIn: true }, dispatch }}>
        <Login />
      </AppContext.Provider>
    </Router>
  );

  expect(history.location.pathname).toEqual("/main");
});

test("handles login success", async () => {
  (userService.login as jest.Mock).mockResolvedValueOnce({
    apiKey: "test-api-key",
  });

  const { getByLabelText, getByText, findByRole } = render(
    <Router history={history}>
      <AppContext.Provider value={{ state: { ...initialAppState, loggedIn: false }, dispatch }}>
        <Login />
      </AppContext.Provider>
    </Router>
  );

  fireEvent.blur(getByLabelText("Login or email"));
  fireEvent.change(getByLabelText("Login or email"), { target: { value: "test-login" } });
  fireEvent.blur(getByLabelText("Login or email"));
  fireEvent.change(getByLabelText("Password"), { target: { value: "test-password" } });
  fireEvent.blur(getByLabelText("Password"));
  fireEvent.click(getByText("Sign In"));

  await findByRole(/loader/i);

  expect(userService.login).toBeCalledWith({ loginOrEmail: "test-login", password: "test-password" });
  expect(dispatch).toBeCalledTimes(2);
  expect(dispatch).toBeCalledWith({ apiKey: "test-api-key", type: "SET_API_KEY" });
  expect(dispatch).toBeCalledWith({
    message: { content: "Successfully logged in.", variant: "success" },
    type: "ADD_MESSAGE",
  });
});

test("handles login error", async () => {
  const testError = new Error("Test Error");
  (userService.login as jest.Mock).mockRejectedValueOnce(testError);

  const { getByLabelText, getByText, findByRole } = render(
    <Router history={history}>
      <AppContext.Provider value={{ state: { ...initialAppState, loggedIn: false }, dispatch }}>
        <Login />
      </AppContext.Provider>
    </Router>
  );

  fireEvent.blur(getByLabelText("Login or email"));
  fireEvent.change(getByLabelText("Login or email"), { target: { value: "test-login" } });
  fireEvent.blur(getByLabelText("Login or email"));
  fireEvent.change(getByLabelText("Password"), { target: { value: "test-password" } });
  fireEvent.blur(getByLabelText("Password"));
  fireEvent.click(getByText("Sign In"));

  await findByRole(/loader/i);

  expect(userService.login).toBeCalledWith({ loginOrEmail: "test-login", password: "test-password" });
  expect(dispatch).toBeCalledTimes(1);
  expect(dispatch).toBeCalledWith({
    message: { content: "Incorrect login/email or password!", variant: "danger" },
    type: "ADD_MESSAGE",
  });
  expect(console.error).toBeCalledWith(testError);
});
