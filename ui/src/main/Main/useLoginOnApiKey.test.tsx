import React from "react";
import { MemoryRouter } from "react-router-dom";
import { screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { UserContextProvider, UserContext, UserAction, UserState } from "contexts";
import useLoginOnApiKey from "./useLoginOnApiKey";
import { renderWithClient } from "tests";

const mockRefetch = jest.fn();
const mockResponse = jest.fn();

const loggedUserState: UserState = {
  apiKey: "test-api-key",
  user: { login: "user-login", email: "email@address.pl", createdOn: "2020-10-09T09:57:17.995288Z" },
  loggedIn: true,
};
const dispatch = jest.fn();

jest.mock("api/apiComponents", () => ({
  useGetUser: () => mockResponse(),
}));

const TestComponent: React.FC<{ actions?: UserAction[]; label?: string }> = ({ actions, label }) => {
  const { state, dispatch } = React.useContext(UserContext);
  useLoginOnApiKey();
  return (
    <>
      <div>
        {Object.entries(state).map(([key, value]) => (
          <span key={key}>
            {key}:{JSON.stringify(value)}
          </span>
        ))}
      </div>
      {actions && <button onClick={() => actions.forEach(dispatch)}>{label}</button>}
    </>
  );
};

beforeEach(() => {
  jest.clearAllMocks();
});

test("default state", () => {
  localStorage.removeItem("apiKey");
  mockResponse.mockReturnValueOnce({
    data: [],
  });

  renderWithClient(
    <MemoryRouter initialEntries={[""]}>
      <UserContextProvider>
        <TestComponent />
      </UserContextProvider>
    </MemoryRouter>,
  );

  expect(screen.getByText("loggedIn:null")).toBeInTheDocument();
  expect(screen.getByText("apiKey:null")).toBeInTheDocument();
});

test("handles set correct api key", async () => {
  mockResponse.mockReturnValueOnce({
    refetch: mockRefetch.mockResolvedValue({
      data: {
        login: "user-login",
        email: "email@address.pl",
        createdOn: "2020-10-09T09:57:17.995288Z",
      },
    }),
  });

  renderWithClient(
    <MemoryRouter initialEntries={[""]}>
      <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
        <TestComponent actions={[{ type: "SET_API_KEY", apiKey: "test-api-key" }]} label="set api key" />
      </UserContext.Provider>
    </MemoryRouter>,
  );

  await userEvent.click(screen.getByText("set api key"));

  expect(dispatch).toHaveBeenCalledWith({ apiKey: "test-api-key", type: "SET_API_KEY" }, 0, [
    { apiKey: "test-api-key", type: "SET_API_KEY" },
  ]);
  expect(screen.getByText("loggedIn:true")).toBeInTheDocument();
  expect(screen.getByText('apiKey:"test-api-key"')).toBeInTheDocument();
});

test("handles set wrong api key", async () => {
  const loggedUserState: UserState = {
    apiKey: null,
    user: null,
    loggedIn: false,
  };

  mockResponse.mockReturnValueOnce({
    refetch: mockRefetch.mockRejectedValueOnce(new Error("Wrong api key")),
  });

  renderWithClient(
    <MemoryRouter initialEntries={[""]}>
      <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
        <TestComponent actions={[{ type: "SET_API_KEY", apiKey: "test-api-key" }]} label="set api key" />
      </UserContext.Provider>
    </MemoryRouter>,
  );

  await userEvent.click(screen.getByText("set api key"));

  expect(dispatch).toHaveBeenCalledWith({ apiKey: "test-api-key", type: "SET_API_KEY" }, 0, [
    { apiKey: "test-api-key", type: "SET_API_KEY" },
  ]);
  expect(screen.getByText("loggedIn:false")).toBeInTheDocument();
  expect(screen.getByText("apiKey:null")).toBeInTheDocument();
});
