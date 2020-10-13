import React from "react";
import { render, fireEvent } from "@testing-library/react";
import ProfileDetails from "./ProfileDetails";
import { AppContext, AppState } from "../AppContext/AppContext";
import userService from "../UserService/UserService";

const mockState: AppState = {
  apiKey: "test-api-key",
  user: { login: "user-login", email: "email@address.pl", createdOn: "2020-10-09T09:57:17.995288Z" },
  loggedIn: true,
  messages: [],
};

jest.mock("../UserService/UserService");
console.error = jest.fn();
const dispatch = jest.fn();

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders header", () => {
  const { getByText } = render(
    <AppContext.Provider value={{ state: mockState, dispatch }}>
      <ProfileDetails />
    </AppContext.Provider>
  );

  expect(getByText("Profile details")).toBeInTheDocument();
});

test("handles change details success", async () => {
  (userService.changeProfileDetails as jest.Mock).mockResolvedValueOnce({});

  const { getByLabelText, getByText, findByRole } = render(
    <AppContext.Provider value={{ state: mockState, dispatch }}>
      <ProfileDetails />
    </AppContext.Provider>
  );

  fireEvent.blur(getByLabelText("Login"));
  fireEvent.change(getByLabelText("Login"), { target: { value: "test-login" } });
  fireEvent.blur(getByLabelText("Login"));
  fireEvent.change(getByLabelText("Email address"), { target: { value: "test@email.address" } });
  fireEvent.blur(getByLabelText("Email address"));
  fireEvent.click(getByText("Update profile data"));

  await findByRole(/loader/i);

  expect(userService.changeProfileDetails).toBeCalledWith("test-api-key", {
    email: "test@email.address",
    login: "test-login",
  });
  expect(dispatch).toBeCalledTimes(2);
  expect(dispatch).toBeCalledWith({
    type: "SET_USER_DATA",
    user: {
      createdOn: "2020-10-09T09:57:17.995288Z",
      email: "test@email.address",
      login: "test-login",
    },
  });
  expect(dispatch).toBeCalledWith({
    message: { content: "Profile details changed!", variant: "success" },
    type: "ADD_MESSAGE",
  });
});

test("handles change details error", async () => {
  const testError = new Error("Test Error");
  (userService.changeProfileDetails as jest.Mock).mockRejectedValueOnce(testError);

  const { getByLabelText, getByText, findByRole } = render(
    <AppContext.Provider value={{ state: mockState, dispatch }}>
      <ProfileDetails />
    </AppContext.Provider>
  );

  fireEvent.blur(getByLabelText("Login"));
  fireEvent.change(getByLabelText("Login"), { target: { value: "test-login" } });
  fireEvent.blur(getByLabelText("Login"));
  fireEvent.change(getByLabelText("Email address"), { target: { value: "test@email.address" } });
  fireEvent.blur(getByLabelText("Email address"));
  fireEvent.click(getByText("Update profile data"));

  await findByRole(/loader/i);

  expect(userService.changeProfileDetails).toBeCalledWith("test-api-key", {
    email: "test@email.address",
    login: "test-login",
  });
  expect(dispatch).toBeCalledTimes(1);
  expect(dispatch).toBeCalledWith({
    message: { content: "Could not change profile details! Test Error", variant: "danger" },
    type: "ADD_MESSAGE",
  });
  expect(console.error).toBeCalledWith(testError);
});
