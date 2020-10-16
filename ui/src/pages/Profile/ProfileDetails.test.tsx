import React from "react";
import { render, fireEvent } from "@testing-library/react";
import ProfileDetails from "./ProfileDetails";
import { UserContext, UserState } from "../../contexts/UserContext/UserContext";
import userService from "../../services/UserService/UserService";

const loggedUserState: UserState = {
  apiKey: "test-api-key",
  user: { login: "user-login", email: "email@address.pl", createdOn: "2020-10-09T09:57:17.995288Z" },
  loggedIn: true,
};

jest.mock("../../services/UserService/UserService");
const dispatch = jest.fn();

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders current user data", () => {
  const { getByLabelText } = render(
    <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
      <ProfileDetails />
    </UserContext.Provider>
  );

  expect((getByLabelText("Login") as HTMLInputElement).value).toEqual("user-login");
  expect((getByLabelText("Email address") as HTMLInputElement).value).toEqual("email@address.pl");
});

test("renders lack of current user data", () => {
  const { getByLabelText } = render(
    <UserContext.Provider value={{ state: { ...loggedUserState, user: null }, dispatch }}>
      <ProfileDetails />
    </UserContext.Provider>
  );

  expect((getByLabelText("Login") as HTMLInputElement).value).toEqual("");
  expect((getByLabelText("Email address") as HTMLInputElement).value).toEqual("");
});

test("handles change details success", async () => {
  (userService.changeProfileDetails as jest.Mock).mockResolvedValueOnce({});

  const { getByLabelText, getByText, getByRole, findByRole } = render(
    <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
      <ProfileDetails />
    </UserContext.Provider>
  );

  fireEvent.blur(getByLabelText("Login"));
  fireEvent.change(getByLabelText("Login"), { target: { value: "test-login" } });
  fireEvent.blur(getByLabelText("Login"));
  fireEvent.change(getByLabelText("Email address"), { target: { value: "test@email.address" } });
  fireEvent.blur(getByLabelText("Email address"));
  fireEvent.click(getByText("Update profile data"));

  await findByRole("loader");

  expect(userService.changeProfileDetails).toBeCalledWith("test-api-key", {
    email: "test@email.address",
    login: "test-login",
  });
  expect(dispatch).toBeCalledTimes(1);
  expect(dispatch).toBeCalledWith({
    type: "UPDATE_USER_DATA",
    user: {
      email: "test@email.address",
      login: "test-login",
    },
  });
  expect(getByRole("success")).toBeInTheDocument();
  expect(getByText("Profile details changed")).toBeInTheDocument();
});

test("handles change details error", async () => {
  const testError = new Error("Test Error");
  (userService.changeProfileDetails as jest.Mock).mockRejectedValueOnce(testError);

  const { getByLabelText, getByText, findByRole, queryByRole, queryByText } = render(
    <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
      <ProfileDetails />
    </UserContext.Provider>
  );

  fireEvent.blur(getByLabelText("Login"));
  fireEvent.change(getByLabelText("Login"), { target: { value: "test-login" } });
  fireEvent.blur(getByLabelText("Login"));
  fireEvent.change(getByLabelText("Email address"), { target: { value: "test@email.address" } });
  fireEvent.blur(getByLabelText("Email address"));
  fireEvent.click(getByText("Update profile data"));

  await findByRole("loader");

  expect(userService.changeProfileDetails).toBeCalledWith("test-api-key", {
    email: "test@email.address",
    login: "test-login",
  });
  expect(dispatch).not.toBeCalled();
  expect(getByText("Test Error")).toBeInTheDocument();

  fireEvent.change(getByLabelText("Email address"), { target: { value: "otherTest@email.address" } });
  fireEvent.blur(getByLabelText("Email address"));

  expect(queryByRole("error")).not.toBeInTheDocument();
  expect(queryByText("Test Error")).not.toBeInTheDocument();
});
