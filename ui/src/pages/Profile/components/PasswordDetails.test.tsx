import { render, screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { UserContext, UserState } from "contexts";
import { userService } from "services";
import { PasswordDetails } from "./PasswordDetails";
import { mockAndDelayRejectedValueOnce, mockAndDelayResolvedValueOnce } from "../../../setupTests";

const mockState: UserState = {
  apiKey: "test-api-key",
  user: { login: "user-login", email: "email@address.pl", createdOn: "2020-10-09T09:57:17.995288Z" },
  loggedIn: true,
};
const dispatch = jest.fn();

jest.mock("services");

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders header", () => {
  render(
    <UserContext.Provider value={{ state: mockState, dispatch }}>
      <PasswordDetails />
    </UserContext.Provider>,
  );

  expect(screen.getByText("Password details")).toBeInTheDocument();
});

test("handles change password success", async () => {
  mockAndDelayResolvedValueOnce(userService.changePassword as jest.Mock, {});

  render(
    <UserContext.Provider value={{ state: mockState, dispatch }}>
      <PasswordDetails />
    </UserContext.Provider>,
  );

  await userEvent.type(screen.getByLabelText("Current password"), "test-password");
  await userEvent.type(screen.getByLabelText("New password"), "test-new-password");
  await userEvent.type(screen.getByLabelText("Repeat new password"), "test-new-password");
  await userEvent.click(screen.getByText("Update password"));

  await screen.findByRole("loader");
  await screen.findByRole("success");
  await screen.findByText("Password changed");

  expect(userService.changePassword).toHaveBeenCalledWith("test-api-key", {
    currentPassword: "test-password",
    newPassword: "test-new-password",
  });
});

test("handles change password error", async () => {
  mockAndDelayRejectedValueOnce(userService.changePassword as jest.Mock, new Error("Test Error"));

  render(
    <UserContext.Provider value={{ state: mockState, dispatch }}>
      <PasswordDetails />
    </UserContext.Provider>,
  );

  await userEvent.type(screen.getByLabelText("Current password"), "test-password");
  await userEvent.type(screen.getByLabelText("New password"), "test-new-password");
  await userEvent.type(screen.getByLabelText("Repeat new password"), "test-new-password");
  await userEvent.click(screen.getByText("Update password"));

  await screen.findByRole("loader");

  expect(userService.changePassword).toHaveBeenCalledWith("test-api-key", {
    currentPassword: "test-password",
    newPassword: "test-new-password",
  });

  await screen.findByRole("error");
  await screen.findByText("Test Error");

  await userEvent.type(screen.getByLabelText("Repeat new password"), "test-newer-password");

  expect(screen.queryByRole("error")).not.toBeInTheDocument();
  expect(screen.queryByText("Test Error")).not.toBeInTheDocument();
});
