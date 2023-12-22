import { render, waitFor } from "@testing-library/react";
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
  const { getByText } = render(
    <UserContext.Provider value={{ state: mockState, dispatch }}>
      <PasswordDetails />
    </UserContext.Provider>,
  );

  expect(getByText("Password details")).toBeInTheDocument();
});

test("handles change password success", async () => {
  (userService.changePassword as jest.Mock).mockResolvedValueOnce({});

  const { getByLabelText, getByText, getByRole, findByRole } = render(
    <UserContext.Provider value={{ state: mockState, dispatch }}>
      <PasswordDetails />
    </UserContext.Provider>,
  );

  await userEvent.type(getByLabelText("Current password"), "test-password");
  await userEvent.type(getByLabelText("New password"), "test-new-password");
  await userEvent.type(getByLabelText("Repeat new password"), "test-new-password");
  await userEvent.click(getByText("Update password"));

  await waitFor(() => expect(findByRole("loader")).toBeTruthy());

  expect(userService.changePassword).toHaveBeenCalledWith("test-api-key", {
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
    </UserContext.Provider>,
  );

  await userEvent.type(getByLabelText("Current password"), "test-password");
  await userEvent.type(getByLabelText("New password"), "test-new-password");
  await userEvent.type(getByLabelText("Repeat new password"), "test-new-password");
  await userEvent.click(getByText("Update password"));

  await waitFor(() => expect(findByRole("loader")).toBeTruthy());

  expect(userService.changePassword).toHaveBeenCalledWith("test-api-key", {
    currentPassword: "test-password",
    newPassword: "test-new-password",
  });
  expect(getByRole("error")).toBeInTheDocument();
  expect(getByText("Test Error")).toBeInTheDocument();

  await userEvent.type(getByLabelText("Repeat new password"), "test-newer-password");

  expect(queryByRole("error")).not.toBeInTheDocument();
  expect(queryByText("Test Error")).not.toBeInTheDocument();
});
