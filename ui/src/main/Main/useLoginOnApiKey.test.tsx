import React from "react";
import { MemoryRouter } from "react-router-dom";
import { render, screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
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
      {actions && <button onClick={() => actions.forEach(dispatch)}>{label}</button>}
    </>
  );
};

beforeEach(() => {
  jest.clearAllMocks();
});

test("default state", () => {
  localStorage.removeItem("apiKey");

  render(
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
  (userService.getCurrentUser as jest.Mock).mockResolvedValueOnce({
    login: "user-login",
    email: "email@address.pl",
    createdOn: "2020-10-09T09:57:17.995288Z",
  });

  render(
    <MemoryRouter initialEntries={[""]}>
      <UserContextProvider>
        <TestComponent actions={[{ type: "SET_API_KEY", apiKey: "test-api-key" }]} label="set api key" />
      </UserContextProvider>
    </MemoryRouter>,
  );

  await userEvent.click(screen.getByText("set api key"));

  expect(userService.getCurrentUser).toHaveBeenCalledWith("test-api-key");
  expect(screen.getByText("loggedIn:true")).toBeInTheDocument();
  expect(screen.getByText('apiKey:"test-api-key"')).toBeInTheDocument();
});

test("handles set wrong api key", async () => {
  (userService.getCurrentUser as jest.Mock).mockRejectedValueOnce(new Error("Test Error"));

  render(
    <MemoryRouter initialEntries={[""]}>
      <UserContextProvider>
        <TestComponent actions={[{ type: "SET_API_KEY", apiKey: "test-api-key" }]} label="set api key" />
      </UserContextProvider>
    </MemoryRouter>,
  );

  await userEvent.click(screen.getByText("set api key"));

  expect(userService.getCurrentUser).toHaveBeenCalledWith("test-api-key");
  expect(screen.getByText("loggedIn:false")).toBeInTheDocument();
  expect(screen.getByText("apiKey:null")).toBeInTheDocument();
});
