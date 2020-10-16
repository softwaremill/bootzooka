import React from "react";
import { render, fireEvent } from "@testing-library/react";
import { UserContextProvider, UserContext, UserAction } from "./UserContext";

const TestComponent: React.FC<{ action?: UserAction; label?: string }> = ({ action, label }) => {
  const { state, dispatch } = React.useContext(UserContext);
  return (
    <>
      <div>{JSON.stringify(state)}</div>
      {action && <a onClick={() => dispatch(action)}>{label}</a>}
    </>
  );
};

test("handles set api key action", () => {
  const { getByText } = render(
    <UserContextProvider>
      <TestComponent action={{ type: "SET_API_KEY", apiKey: "test-api-key" }} label="set api key" />
    </UserContextProvider>
  );

  expect(getByText('{"apiKey":null,"user":null,"loggedIn":null}')).toBeInTheDocument();

  fireEvent.click(getByText("set api key"));

  expect(getByText('{"apiKey":"test-api-key","user":null,"loggedIn":null}')).toBeInTheDocument();
});

test("handles login action", () => {
  const { getByText } = render(
    <UserContextProvider>
      <TestComponent
        action={{
          type: "LOG_IN",
          user: { login: "user-login", email: "email@address.pl", createdOn: "2020-10-09T09:57:17.995288Z" },
        }}
        label="log in"
      />
    </UserContextProvider>
  );

  expect(getByText('{"apiKey":null,"user":null,"loggedIn":null}')).toBeInTheDocument();

  fireEvent.click(getByText("log in"));

  expect(
    getByText(
      '{"apiKey":null,"user":{"login":"user-login","email":"email@address.pl","createdOn":"2020-10-09T09:57:17.995288Z"},"loggedIn":true}'
    )
  ).toBeInTheDocument();
});

test("handles log in and update details action", () => {
  const { getAllByText, getByText } = render(
    <UserContextProvider>
      <TestComponent
        action={{
          type: "LOG_IN",
          user: { login: "user-login", email: "email@address.pl", createdOn: "2020-10-09T09:57:17.995288Z" },
        }}
        label="log in"
      />
      <TestComponent
        action={{
          type: "UPDATE_USER_DATA",
          user: { login: "updated-user-login", email: "updatedEmail@address.pl" },
        }}
        label="update user data"
      />
    </UserContextProvider>
  );

  expect(getAllByText('{"apiKey":null,"user":null,"loggedIn":null}')[0]).toBeInTheDocument();

  fireEvent.click(getByText("update user data"));

  expect(getAllByText('{"apiKey":null,"user":null,"loggedIn":null}')[0]).toBeInTheDocument();

  fireEvent.click(getByText("log in"));

  expect(
    getAllByText(
      '{"apiKey":null,"user":{"login":"user-login","email":"email@address.pl","createdOn":"2020-10-09T09:57:17.995288Z"},"loggedIn":true}'
    )[0]
  ).toBeInTheDocument();

  fireEvent.click(getByText("update user data"));

  expect(
    getAllByText(
      '{"apiKey":null,"user":{"login":"updated-user-login","email":"updatedEmail@address.pl","createdOn":"2020-10-09T09:57:17.995288Z"},"loggedIn":true}'
    )[0]
  ).toBeInTheDocument();
});

test("handles log in and log out action", () => {
  const { getAllByText, getByText } = render(
    <UserContextProvider>
      <TestComponent
        action={{
          type: "LOG_IN",
          user: { login: "user-login", email: "email@address.pl", createdOn: "2020-10-09T09:57:17.995288Z" },
        }}
        label="log in"
      />
      <TestComponent
        action={{
          type: "LOG_OUT",
        }}
        label="log out"
      />
    </UserContextProvider>
  );

  expect(getAllByText('{"apiKey":null,"user":null,"loggedIn":null}')[0]).toBeInTheDocument();

  fireEvent.click(getByText("log out"));

  expect(getAllByText('{"apiKey":null,"user":null,"loggedIn":false}')[0]).toBeInTheDocument();

  fireEvent.click(getByText("log in"));

  expect(
    getAllByText(
      '{"apiKey":null,"user":{"login":"user-login","email":"email@address.pl","createdOn":"2020-10-09T09:57:17.995288Z"},"loggedIn":true}'
    )[0]
  ).toBeInTheDocument();

  fireEvent.click(getByText("log out"));

  expect(getAllByText('{"apiKey":null,"user":null,"loggedIn":false}')[0]).toBeInTheDocument();
});
