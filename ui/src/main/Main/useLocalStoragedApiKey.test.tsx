import React from "react";
import { MemoryRouter } from "react-router-dom";
import { render, fireEvent } from "@testing-library/react";
import { UserContextProvider, UserContext, UserAction } from "contexts";
import useLocalStoragedApiKey from "./useLocalStoragedApiKey";

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
      {actions && <a onClick={() => actions.forEach(dispatch)}>{label}</a>}
    </>
  );
};

beforeEach(() => {
  jest.clearAllMocks();
});

test("handles not stored api key", () => {
  localStorage.removeItem("apiKey");

  const { getByText } = render(
    <MemoryRouter initialEntries={[""]}>
      <UserContextProvider>
        <TestComponent />
      </UserContextProvider>
    </MemoryRouter>
  );

  expect(getByText("loggedIn:false")).toBeInTheDocument();
  expect(getByText("apiKey:null")).toBeInTheDocument();
});

test("handles stored api key", () => {
  localStorage.setItem("apiKey", "test-api-key");

  const { getByText } = render(
    <MemoryRouter initialEntries={[""]}>
      <UserContextProvider>
        <TestComponent />
      </UserContextProvider>
    </MemoryRouter>
  );

  expect(getByText("loggedIn:null")).toBeInTheDocument();
  expect(getByText('apiKey:"test-api-key"')).toBeInTheDocument();
});

test("handles user logging in", () => {
  localStorage.removeItem("apiKey");

  const { getByText } = render(
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
    </MemoryRouter>
  );

  fireEvent.click(getByText("log in"));

  expect(localStorage.getItem("apiKey")).toEqual("test-api-key");
  expect(getByText("loggedIn:true")).toBeInTheDocument();
  expect(getByText('apiKey:"test-api-key"')).toBeInTheDocument();
});

test("handles user logging out", () => {
  localStorage.setItem("apiKey", "test-api-key");

  const { getByText } = render(
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
    </MemoryRouter>
  );

  fireEvent.click(getByText("log in and out"));

  expect(getByText("loggedIn:false")).toBeInTheDocument();
  expect(getByText("apiKey:null")).toBeInTheDocument();
  expect(localStorage.getItem("apiKey")).toBeNull();
});
