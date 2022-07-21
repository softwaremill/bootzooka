import { render, fireEvent } from "@testing-library/react";
import { MemoryRouter, unstable_HistoryRouter as HistoryRouter } from "react-router-dom";
import { createMemoryHistory } from "history";
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
  const { getByText } = render(
    <MemoryRouter initialEntries={["/login"]}>
      <UserContext.Provider value={{ state: { ...initialUserState, loggedIn: false }, dispatch }}>
        <Login />
      </UserContext.Provider>
    </MemoryRouter>
  );

  expect(getByText("Please sign in")).toBeInTheDocument();
});

test("redirects when logged in", () => {
  render(
    <HistoryRouter history={history}>
      <UserContext.Provider value={{ state: { ...initialUserState, loggedIn: true }, dispatch }}>
        <Login />
      </UserContext.Provider>
    </HistoryRouter>
  );

  expect(history.location.pathname).toEqual("/main");
});

test("handles login success", async () => {
  (userService.login as jest.Mock).mockResolvedValueOnce({
    apiKey: "test-api-key",
  });

  const { getByLabelText, getByText, findByRole } = render(
    <HistoryRouter history={history}>
      <UserContext.Provider value={{ state: { ...initialUserState, loggedIn: false }, dispatch }}>
        <Login />
      </UserContext.Provider>
    </HistoryRouter>
  );

  fireEvent.blur(getByLabelText("Login or email"));
  fireEvent.change(getByLabelText("Login or email"), { target: { value: "test-login" } });
  fireEvent.blur(getByLabelText("Login or email"));
  fireEvent.change(getByLabelText("Password"), { target: { value: "test-password" } });
  fireEvent.blur(getByLabelText("Password"));
  fireEvent.click(getByText("Sign In"));

  await findByRole("loader");

  expect(userService.login).toBeCalledWith({ loginOrEmail: "test-login", password: "test-password" });
  expect(dispatch).toBeCalledTimes(1);
  expect(dispatch).toBeCalledWith({ apiKey: "test-api-key", type: "SET_API_KEY" });
  expect(history.location.pathname).toEqual("/main");
});

test("handles login error", async () => {
  const testError = new Error("Test Error");
  (userService.login as jest.Mock).mockRejectedValueOnce(testError);

  const { getByLabelText, getByText, findByRole } = render(
    <MemoryRouter initialEntries={["/login"]}>
      <UserContext.Provider value={{ state: { ...initialUserState, loggedIn: false }, dispatch }}>
        <Login />
      </UserContext.Provider>
    </MemoryRouter>
  );

  fireEvent.blur(getByLabelText("Login or email"));
  fireEvent.change(getByLabelText("Login or email"), { target: { value: "test-login" } });
  fireEvent.blur(getByLabelText("Login or email"));
  fireEvent.change(getByLabelText("Password"), { target: { value: "test-password" } });
  fireEvent.blur(getByLabelText("Password"));
  fireEvent.click(getByText("Sign In"));

  await findByRole("loader");

  expect(userService.login).toBeCalledWith({ loginOrEmail: "test-login", password: "test-password" });
  expect(dispatch).not.toBeCalled();
  expect(getByText("Test Error")).toBeInTheDocument();
});
