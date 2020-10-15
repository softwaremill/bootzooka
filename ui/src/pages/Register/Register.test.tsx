import React from "react";
import { render, fireEvent } from "@testing-library/react";
import Register from "./Register";
import { UserContext, initialUserState } from "../../contexts/UserContext/UserContext";
import { Router } from "react-router-dom";
import { createMemoryHistory } from "history";
import userService from "../../services/UserService/UserService";

const history = createMemoryHistory({ initialEntries: ["/login"] });

jest.mock("../../services/UserService/UserService");
const dispatch = jest.fn();

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders header", () => {
  const { getByText } = render(
    <Router history={history}>
      <UserContext.Provider value={{ state: { ...initialUserState, loggedIn: false }, dispatch }}>
        <Register />
      </UserContext.Provider>
    </Router>
  );

  expect(getByText("Please sign up")).toBeInTheDocument();
});

test("redirects when registered", () => {
  render(
    <Router history={history}>
      <UserContext.Provider value={{ state: { ...initialUserState, loggedIn: true }, dispatch }}>
        <Register />
      </UserContext.Provider>
    </Router>
  );

  expect(history.location.pathname).toEqual("/main");
});

test("handles register success", async () => {
  (userService.registerUser as jest.Mock).mockResolvedValueOnce({
    apiKey: "test-api-key",
  });

  const { getByLabelText, getByText, findByRole } = render(
    <Router history={history}>
      <UserContext.Provider value={{ state: { ...initialUserState, loggedIn: false }, dispatch }}>
        <Register />
      </UserContext.Provider>
    </Router>
  );

  fireEvent.blur(getByLabelText("Login"));
  fireEvent.change(getByLabelText("Login"), { target: { value: "test-login" } });
  fireEvent.blur(getByLabelText("Login"));
  fireEvent.change(getByLabelText("Email address"), { target: { value: "test@email.address.pl" } });
  fireEvent.blur(getByLabelText("Email address"));
  fireEvent.change(getByLabelText("Password"), { target: { value: "test-password" } });
  fireEvent.blur(getByLabelText("Password"));
  fireEvent.change(getByLabelText("Repeat password"), { target: { value: "test-password" } });
  fireEvent.blur(getByLabelText("Repeat password"));
  fireEvent.click(getByText("Register"));

  await findByRole("loader");

  expect(userService.registerUser).toBeCalledWith({
    login: "test-login",
    email: "test@email.address.pl",
    password: "test-password",
  });
  expect(dispatch).toBeCalledTimes(1);
  expect(dispatch).toBeCalledWith({ apiKey: "test-api-key", type: "SET_API_KEY" });
  expect(history.location.pathname).toEqual("/main");
});

test("handles register error", async () => {
  const testError = new Error("Test Error");
  (userService.registerUser as jest.Mock).mockRejectedValueOnce(testError);

  const { getByLabelText, getByText, findByRole } = render(
    <Router history={history}>
      <UserContext.Provider value={{ state: { ...initialUserState, loggedIn: false }, dispatch }}>
        <Register />
      </UserContext.Provider>
    </Router>
  );

  fireEvent.blur(getByLabelText("Login"));
  fireEvent.change(getByLabelText("Login"), { target: { value: "test-login" } });
  fireEvent.blur(getByLabelText("Login"));
  fireEvent.change(getByLabelText("Email address"), { target: { value: "test@email.address.pl" } });
  fireEvent.blur(getByLabelText("Email address"));
  fireEvent.change(getByLabelText("Password"), { target: { value: "test-password" } });
  fireEvent.blur(getByLabelText("Password"));
  fireEvent.change(getByLabelText("Repeat password"), { target: { value: "test-password" } });
  fireEvent.blur(getByLabelText("Repeat password"));
  fireEvent.click(getByText("Register"));

  await findByRole("loader");

  expect(userService.registerUser).toBeCalledWith({
    login: "test-login",
    email: "test@email.address.pl",
    password: "test-password",
  });
  expect(dispatch).not.toBeCalled();
  expect(getByText("Test Error")).toBeInTheDocument();
});
