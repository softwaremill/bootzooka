import React from "react";
import { render, fireEvent } from "@testing-library/react";
import PasswordDetails from "./PasswordDetails";
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
      <PasswordDetails />
    </UserContext.Provider>
  );

  expect(getByText("Password details")).toBeInTheDocument();
});

test("handles change password success", async () => {
  (userService.changePassword as jest.Mock).mockResolvedValueOnce({});

  const { getByLabelText, getByText, getByRole, findByRole } = render(
    <UserContext.Provider value={{ state: mockState, dispatch }}>
      <PasswordDetails />
    </UserContext.Provider>
  );

  fireEvent.blur(getByLabelText("Current password"));
  fireEvent.change(getByLabelText("Current password"), { target: { value: "test-password" } });
  fireEvent.blur(getByLabelText("Current password"));
  fireEvent.change(getByLabelText("New password"), { target: { value: "test-new-password" } });
  fireEvent.blur(getByLabelText("New password"));
  fireEvent.change(getByLabelText("Repeat new password"), { target: { value: "test-new-password" } });
  fireEvent.blur(getByLabelText("Repeat new password"));
  fireEvent.click(getByText("Update password"));

  await findByRole("loader");

  expect(userService.changePassword).toBeCalledWith("test-api-key", {
    currentPassword: "test-password",
    newPassword: "test-new-password",
  });
  expect(getByRole("success")).toBeInTheDocument();
  expect(getByText("Password changed")).toBeInTheDocument();
});

test("handles change password error", async () => {
  const testError = new Error("Test Error");
  (userService.changePassword as jest.Mock).mockRejectedValueOnce(testError);

  const { getByLabelText, getByText, findByRole, getByRole, queryByRole, queryByText } = render(
    <UserContext.Provider value={{ state: mockState, dispatch }}>
      <PasswordDetails />
    </UserContext.Provider>
  );

  fireEvent.blur(getByLabelText("Current password"));
  fireEvent.change(getByLabelText("Current password"), { target: { value: "test-password" } });
  fireEvent.blur(getByLabelText("Current password"));
  fireEvent.change(getByLabelText("New password"), { target: { value: "test-new-password" } });
  fireEvent.blur(getByLabelText("New password"));
  fireEvent.change(getByLabelText("Repeat new password"), { target: { value: "test-new-password" } });
  fireEvent.blur(getByLabelText("Repeat new password"));
  fireEvent.click(getByText("Update password"));

  await findByRole("loader");

  expect(userService.changePassword).toBeCalledWith("test-api-key", {
    currentPassword: "test-password",
    newPassword: "test-new-password",
  });
  expect(getByRole("error")).toBeInTheDocument();
  expect(getByText("Test Error")).toBeInTheDocument();

  fireEvent.change(getByLabelText("Repeat new password"), { target: { value: "test-newer-password" } });
  fireEvent.blur(getByLabelText("Repeat new password"));

  expect(queryByRole("error")).not.toBeInTheDocument();
  expect(queryByText("Test Error")).not.toBeInTheDocument();
});
