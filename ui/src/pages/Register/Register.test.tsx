import { screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { MemoryRouter, unstable_HistoryRouter as HistoryRouter } from "react-router-dom";
import { createMemoryHistory } from "@remix-run/router";
import { UserContext, initialUserState } from "contexts";
import { Register } from "./Register";
import { renderWithClient } from "tests";

const history = createMemoryHistory({ initialEntries: ["/login"] });
const dispatch = jest.fn();
const mockMutate = jest.fn();
const mockResponse = jest.fn();

jest.mock("api/apiComponents", () => ({
  usePostUserRegister: () => mockResponse(),
}));

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders header", () => {
  mockResponse.mockReturnValueOnce({
    mutate: mockMutate,
    reset: jest.fn(),
    isSuccess: true,
    data: { apiKey: "test-api-key" },
    isError: false,
    error: "",
  });

  renderWithClient(
    <MemoryRouter initialEntries={["/login"]}>
      <UserContext.Provider value={{ state: { ...initialUserState, loggedIn: false }, dispatch }}>
        <Register />
      </UserContext.Provider>
    </MemoryRouter>,
  );

  expect(screen.getByText("Please sign up")).toBeInTheDocument();
});

test("redirects when registered", () => {
  mockResponse.mockReturnValueOnce({
    mutate: mockMutate,
    reset: jest.fn(),
    isSuccess: true,
    data: { apiKey: "test-api-key" },
    isError: false,
    error: "",
  });

  renderWithClient(
    <HistoryRouter history={history}>
      <UserContext.Provider value={{ state: { ...initialUserState, loggedIn: true }, dispatch }}>
        <Register />
      </UserContext.Provider>
    </HistoryRouter>,
  );

  expect(history.location.pathname).toEqual("/main");
});

test("handles register success", async () => {
  mockResponse.mockReturnValueOnce({
    mutate: mockMutate,
    reset: jest.fn(),
    isSuccess: true,
    data: { apiKey: "test-api-key" },
    isError: false,
    error: "",
  });

  renderWithClient(
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

  await screen.findByRole("success");

  expect(mockMutate).toHaveBeenCalledWith({
    body: {
      login: "test-login",
      email: "test@email.address.pl",
      password: "test-password",
      repeatedPassword: "test-password",
    },
  });

  expect(dispatch).toHaveBeenCalledWith({ apiKey: "test-api-key", type: "SET_API_KEY" });
});

test("handles register error", async () => {
  mockResponse.mockReturnValueOnce({
    mutate: mockMutate,
    reset: jest.fn(),
    data: { apiKey: "test-api-key" },
    isSuccess: false,
    isError: true,
    error: "Test error",
  });

  renderWithClient(
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

  expect(mockMutate).toHaveBeenCalledWith({
    body: {
      login: "test-login",
      email: "test@email.address.pl",
      password: "test-password",
      repeatedPassword: "test-password",
    },
  });
  expect(dispatch).not.toHaveBeenCalled();

  await screen.findByRole("error");
});
