import React from "react";
import { MemoryRouter } from "react-router-dom";
import { render, fireEvent, act } from "@testing-library/react";
import { UserContextProvider, UserContext, UserAction } from "contexts";
import { userService } from "services";
import useLoginOnApiKey from "./useLoginOnApiKey";

jest.mock("services");

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
      {actions && <a onClick={() => actions.forEach(dispatch)}>{label}</a>}
    </>
  );
};

beforeEach(() => {
  jest.clearAllMocks();
});

test("default state", () => {
  localStorage.removeItem("apiKey");

  const { getByText } = render(
    <MemoryRouter initialEntries={[""]}>
      <UserContextProvider>
        <TestComponent />
      </UserContextProvider>
    </MemoryRouter>
  );

  expect(getByText("loggedIn:null")).toBeInTheDocument();
  expect(getByText("apiKey:null")).toBeInTheDocument();
});

test("handles set correct api key", async () => {
  (userService.getCurrentUser as jest.Mock).mockResolvedValueOnce({
    login: "user-login",
    email: "email@address.pl",
    createdOn: "2020-10-09T09:57:17.995288Z",
  });

  const { getByText } = render(
    <MemoryRouter initialEntries={[""]}>
      <UserContextProvider>
        <TestComponent actions={[{ type: "SET_API_KEY", apiKey: "test-api-key" }]} label="set api key" />
      </UserContextProvider>
    </MemoryRouter>
  );

  await act(async () => {
    fireEvent.click(getByText("set api key"));
  });

  expect(userService.getCurrentUser).toBeCalledWith("test-api-key");
  expect(getByText("loggedIn:true")).toBeInTheDocument();
  expect(getByText('apiKey:"test-api-key"')).toBeInTheDocument();
});

test("handles set wrong api key", async () => {
  (userService.getCurrentUser as jest.Mock).mockRejectedValueOnce(new Error("Test Error"));

  const { getByText } = render(
    <MemoryRouter initialEntries={[""]}>
      <UserContextProvider>
        <TestComponent actions={[{ type: "SET_API_KEY", apiKey: "test-api-key" }]} label="set api key" />
      </UserContextProvider>
    </MemoryRouter>
  );

  await act(async () => {
    fireEvent.click(getByText("set api key"));
  });

  expect(userService.getCurrentUser).toBeCalledWith("test-api-key");
  expect(getByText("loggedIn:false")).toBeInTheDocument();
  expect(getByText("apiKey:null")).toBeInTheDocument();
});
