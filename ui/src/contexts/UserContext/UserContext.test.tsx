import React from "react";
import { render, screen } from "@testing-library/react";
import { UserContextProvider, UserContext, UserAction } from "./UserContext";
import { userEvent } from "@testing-library/user-event";

const TestComponent: React.FC<{ action?: UserAction; label?: string }> = ({ action, label }) => {
  const { state, dispatch } = React.useContext(UserContext);
  return (
    <>
      <div>{JSON.stringify(state)}</div>
      {action && <button onClick={() => dispatch(action)}>{label}</button>}
    </>
  );
};

test("handles set api key action", async () => {
  render(
    <UserContextProvider>
      <TestComponent action={{ type: "SET_API_KEY", apiKey: "test-api-key" }} label="set api key" />
    </UserContextProvider>,
  );

  expect(screen.getByText('{"apiKey":null,"user":null,"loggedIn":null}')).toBeInTheDocument();

  await userEvent.click(screen.getByText("set api key"));

  expect(screen.getByText('{"apiKey":"test-api-key","user":null,"loggedIn":null}')).toBeInTheDocument();
});

test("handles login action", async () => {
  render(
    <UserContextProvider>
      <TestComponent
        action={{
          type: "LOG_IN",
          user: { login: "user-login", email: "email@address.pl", createdOn: "2020-10-09T09:57:17.995288Z" },
        }}
        label="log in"
      />
    </UserContextProvider>,
  );

  expect(screen.getByText('{"apiKey":null,"user":null,"loggedIn":null}')).toBeInTheDocument();

  await userEvent.click(screen.getByText("log in"));

  expect(
    screen.getByText(
      '{"apiKey":null,"user":{"login":"user-login","email":"email@address.pl","createdOn":"2020-10-09T09:57:17.995288Z"},"loggedIn":true}',
    ),
  ).toBeInTheDocument();
});

test("handles log in and update details action", async () => {
  render(
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
    </UserContextProvider>,
  );

  expect(screen.getAllByText('{"apiKey":null,"user":null,"loggedIn":null}')[0]).toBeInTheDocument();

  await userEvent.click(screen.getByText("update user data"));

  expect(screen.getAllByText('{"apiKey":null,"user":null,"loggedIn":null}')[0]).toBeInTheDocument();

  await userEvent.click(screen.getByText("log in"));

  expect(
    screen.getAllByText(
      '{"apiKey":null,"user":{"login":"user-login","email":"email@address.pl","createdOn":"2020-10-09T09:57:17.995288Z"},"loggedIn":true}',
    )[0],
  ).toBeInTheDocument();

  await userEvent.click(screen.getByText("update user data"));

  expect(
    screen.getAllByText(
      '{"apiKey":null,"user":{"login":"updated-user-login","email":"updatedEmail@address.pl","createdOn":"2020-10-09T09:57:17.995288Z"},"loggedIn":true}',
    )[0],
  ).toBeInTheDocument();
});

test("handles log in and log out action", async () => {
  render(
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
    </UserContextProvider>,
  );

  expect(screen.getAllByText('{"apiKey":null,"user":null,"loggedIn":null}')[0]).toBeInTheDocument();

  await userEvent.click(screen.getByText("log out"));

  expect(screen.getAllByText('{"apiKey":null,"user":null,"loggedIn":false}')[0]).toBeInTheDocument();

  await userEvent.click(screen.getByText("log in"));

  expect(
    screen.getAllByText(
      '{"apiKey":null,"user":{"login":"user-login","email":"email@address.pl","createdOn":"2020-10-09T09:57:17.995288Z"},"loggedIn":true}',
    )[0],
  ).toBeInTheDocument();

  await userEvent.click(screen.getByText("log out"));

  expect(screen.getAllByText('{"apiKey":null,"user":null,"loggedIn":false}')[0]).toBeInTheDocument();
});
