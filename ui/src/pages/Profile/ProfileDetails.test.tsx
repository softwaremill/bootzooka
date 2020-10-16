import React from "react";
import { render, fireEvent } from "@testing-library/react";
import ProfileDetails from "./ProfileDetails";
import { UserContext, UserState } from "../../contexts/UserContext/UserContext";
import userService from "../../services/UserService/UserService";

const mockState: UserState = {
  apiKey: "test-api-key",
  user: { login: "user-login", email: "email@address.pl", createdOn: "2020-10-09T09:57:17.995288Z" },
  loggedIn: true,
};

jest.mock("../../services/UserService/UserService");
const dispatch = jest.fn();

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders header", () => {
  const { getByText } = render(
    <UserContext.Provider value={{ state: mockState, dispatch }}>
      <ProfileDetails />
    </UserContext.Provider>
  );

  expect(getByText("Profile details")).toBeInTheDocument();
});

test("handles change details success", async () => {
  (userService.changeProfileDetails as jest.Mock).mockResolvedValueOnce({});

  const { getByLabelText, getByText, getByRole, findByRole } = render(
    <UserContext.Provider value={{ state: mockState, dispatch }}>
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
    <UserContext.Provider value={{ state: mockState, dispatch }}>
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
