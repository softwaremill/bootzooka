import React from "react";
import { MemoryRouter } from "react-router-dom";
import { render, screen } from "@testing-library/react";
import { UserContextProvider, UserContext, UserAction } from "contexts";
import useLocalStoragedApiKey from "./useLocalStoragedApiKey";
import { userEvent } from "@testing-library/user-event";

const TestComponent: React.FC<{ actions?: UserAction[]; label?: string }> = ({ actions, label }) => {
  const { state, dispatch } = React.useContext(UserContext);
  useLocalStoragedApiKey();
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

test("handles not stored api key", () => {
  localStorage.removeItem("apiKey");

  render(
    <MemoryRouter initialEntries={[""]}>
      <UserContextProvider>
        <TestComponent />
      </UserContextProvider>
    </MemoryRouter>,
  );

  expect(screen.getByText("loggedIn:false")).toBeInTheDocument();
  expect(screen.getByText("apiKey:null")).toBeInTheDocument();
});

test("handles stored api key", () => {
  localStorage.setItem("apiKey", "test-api-key");

  render(
    <MemoryRouter initialEntries={[""]}>
      <UserContextProvider>
        <TestComponent />
      </UserContextProvider>
    </MemoryRouter>,
  );

  expect(screen.getByText("loggedIn:null")).toBeInTheDocument();
  expect(screen.getByText('apiKey:"test-api-key"')).toBeInTheDocument();
});

test("handles user logging in", async () => {
  localStorage.removeItem("apiKey");

  render(
    <MemoryRouter initialEntries={[""]}>
      <UserContextProvider>
        <TestComponent
          actions={[
            { type: "SET_API_KEY", apiKey: "test-api-key" },
            {
              type: "LOG_IN",
              user: { login: "user-login", email: "email@address.pl", createdOn: "2020-10-09T09:57:17.995288Z" },
            },
          ]}
          label="log in"
        />
      </UserContextProvider>
    </MemoryRouter>,
  );

  await userEvent.click(screen.getByText("log in"));

  expect(localStorage.getItem("apiKey")).toEqual("test-api-key");
  expect(screen.getByText("loggedIn:true")).toBeInTheDocument();
  expect(screen.getByText('apiKey:"test-api-key"')).toBeInTheDocument();
});

test("handles user logging out", async () => {
  localStorage.setItem("apiKey", "test-api-key");

  render(
    <MemoryRouter initialEntries={[""]}>
      <UserContextProvider>
        <TestComponent
          actions={[
            {
              type: "LOG_IN",
              user: { login: "user-login", email: "email@address.pl", createdOn: "2020-10-09T09:57:17.995288Z" },
            },
            {
              type: "LOG_OUT",
            },
          ]}
          label="log in and out"
        />
      </UserContextProvider>
    </MemoryRouter>,
  );

  await userEvent.click(screen.getByText("log in and out"));

  expect(screen.getByText("loggedIn:false")).toBeInTheDocument();
  expect(screen.getByText("apiKey:null")).toBeInTheDocument();
  expect(localStorage.getItem("apiKey")).toBeNull();
});
