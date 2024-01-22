import { screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { UserContext, UserState } from "contexts";
import { userService } from "services";
import { PasswordDetails } from "./PasswordDetails";
import { renderWithClient } from "tests";

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
  renderWithClient(
    <UserContext.Provider value={{ state: mockState, dispatch }}>
      <PasswordDetails />
    </UserContext.Provider>,
  );

  expect(screen.getByText("Password details")).toBeInTheDocument();
});

test("handles change password success", async () => {
  const apiKey = "test-api-key";
  (userService.changePassword as jest.Mock).mockResolvedValueOnce({ apiKey });

  renderWithClient(
    <UserContext.Provider value={{ state: mockState, dispatch }}>
      <PasswordDetails />
    </UserContext.Provider>,
  );

  await userEvent.type(screen.getByLabelText("Current password"), "test-password");
  await userEvent.type(screen.getByLabelText("New password"), "test-new-password");
  await userEvent.type(screen.getByLabelText("Repeat new password"), "test-new-password");
  await userEvent.click(screen.getByText("Update password"));

  await screen.findByRole("success");
  await screen.findByText("Password changed");

  expect(userService.changePassword).toHaveBeenCalledWith("test-api-key", {
    currentPassword: "test-password",
    newPassword: "test-new-password",
  });
  expect(dispatch).toHaveBeenCalledWith({ apiKey: "test-api-key", type: "SET_API_KEY" });
});

test("handles change password error", async () => {
  (userService.changePassword as jest.Mock).mockRejectedValueOnce(new Error("Test Error"));

  renderWithClient(
    <UserContext.Provider value={{ state: mockState, dispatch }}>
      <PasswordDetails />
    </UserContext.Provider>,
  );

  await userEvent.type(screen.getByLabelText("Current password"), "test-password");
  await userEvent.type(screen.getByLabelText("New password"), "test-new-password");
  await userEvent.type(screen.getByLabelText("Repeat new password"), "test-new-password");
  await userEvent.click(screen.getByText("Update password"));

  expect(userService.changePassword).toHaveBeenCalledWith("test-api-key", {
    currentPassword: "test-password",
    newPassword: "test-new-password",
  });

  await screen.findByRole("error");

  await userEvent.type(screen.getByLabelText("Repeat new password"), "test-newer-password");

  expect(screen.queryByRole("error")).not.toBeInTheDocument();
  expect(screen.queryByText("Test Error")).not.toBeInTheDocument();
});
