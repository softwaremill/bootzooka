import { render, screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { UserContext, UserState } from "contexts";
import { userService } from "services";
import { PasswordDetails } from "./PasswordDetails";

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
  (userService.changePassword as jest.Mock).mockResolvedValueOnce({});

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
  expect(screen.getByRole("success")).toBeInTheDocument();
  expect(screen.getByText("Password changed")).toBeInTheDocument();
});

test("handles change password error", async () => {
  const testError = new Error("Test Error");
  (userService.changePassword as jest.Mock).mockRejectedValueOnce(testError);

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
  expect(screen.getByRole("error")).toBeInTheDocument();
  expect(screen.getByText("Test Error")).toBeInTheDocument();

  await userEvent.type(screen.getByLabelText("Repeat new password"), "test-newer-password");

  expect(screen.queryByRole("error")).not.toBeInTheDocument();
  expect(screen.queryByText("Test Error")).not.toBeInTheDocument();
});
