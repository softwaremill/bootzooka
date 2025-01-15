import { screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { MemoryRouter, unstable_HistoryRouter as HistoryRouter } from "react-router-dom";
import { createMemoryHistory } from "@remix-run/router";
import { Login } from "./Login";
import { UserContext, initialUserState } from "contexts";
import { renderWithClient } from "tests";

const history = createMemoryHistory({ initialEntries: ["/login"] });
const dispatch = jest.fn();
const mockMutate = jest.fn();
const mockResponse = jest.fn();

jest.mock("api/apiComponents", () => ({
  usePostUserLogin: () => mockResponse(),
}));

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders header", () => {
  mockResponse.mockReturnValueOnce({
    mutateAsync: mockMutate,
    reset: jest.fn(),
    data: { apiKey: "test-api-key" },
    isSuccess: true,
    isError: false,
    error: "",
  });

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
  mockResponse.mockReturnValueOnce({
    mutateAsync: mockMutate,
    reset: jest.fn(),
    data: { apiKey: "test-api-key" },
    isSuccess: true,
    isError: false,
    error: "",
  });

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
  mockResponse.mockReturnValueOnce({
    mutateAsync: mockMutate,
    reset: jest.fn(),
    data: { apiKey: "test-api-key" },
    isSuccess: true,
    isError: false,
    error: "",
    onSuccess: dispatch({
      type: "SET_API_KEY",
      apiKey: "test-api-key",
    }),
  });

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

  expect(mockMutate).toHaveBeenCalledWith({
    body: {
      loginOrEmail: "test-login",
      password: "test-password",
    },
  });

  expect(dispatch).toHaveBeenCalledWith({
    type: "SET_API_KEY",
    apiKey: "test-api-key",
  });
});

test("handles login error", async () => {
  mockResponse.mockReturnValueOnce({
    mutateAsync: mockMutate,
    reset: jest.fn(),
    data: { apiKey: "test-api-key" },
    isSuccess: false,
    isError: true,
    error: "Test error",
  });

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

  expect(mockMutate).toHaveBeenCalledWith({ body: { loginOrEmail: "test-login", password: "test-password" } });
  expect(dispatch).not.toHaveBeenCalled();
});
