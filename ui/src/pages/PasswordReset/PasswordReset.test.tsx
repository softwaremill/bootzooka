import { render, waitFor } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { PasswordReset } from "./PasswordReset";
import { passwordService } from "services";

jest.mock("services");

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders header", () => {
  const { getByText } = render(<PasswordReset />);

  expect(getByText("Password details")).toBeInTheDocument();
});

test("handles password reset success", async () => {
  delete (window as any).location;
  (window as any).location = new URL("https://www.example.com/password-reset?code=test-code");
  (passwordService.resetPassword as jest.Mock).mockResolvedValueOnce({});

  const { getByLabelText, getByText, getByRole, findByRole } = render(<PasswordReset />);

  await userEvent.type(getByLabelText("New password"), "test-new-password");
  await userEvent.type(getByLabelText("Repeat new password"), "test-new-password");
  await userEvent.click(getByText("Update password"));

  await waitFor(() => expect(findByRole("loader")).toBeTruthy());

  expect(passwordService.resetPassword).toHaveBeenCalledWith({ code: "test-code", password: "test-new-password" });
  expect(getByRole("success")).toBeInTheDocument();
  expect(getByText("Password changed")).toBeInTheDocument();
});

test("handles lack of url code", async () => {
  delete (window as any).location;
  (window as any).location = new URL("https://www.example.com/password-reset");

  const { getByLabelText, getByText, findByRole } = render(<PasswordReset />);

  await userEvent.type(getByLabelText("New password"), "test-new-password");
  await userEvent.type(getByLabelText("Repeat new password"), "test-new-password");
  await userEvent.click(getByText("Update password"));

  await waitFor(() => expect(findByRole("loader")).toBeTruthy());

  expect(passwordService.resetPassword).toHaveBeenCalledWith({ code: "", password: "test-new-password" });
});

test("handles password reset error", async () => {
  delete (window as any).location;
  (window as any).location = new URL("https://www.example.com/password-reset?code=test-code");
  const testError = new Error("Test Error");
  (passwordService.resetPassword as jest.Mock).mockRejectedValueOnce(testError);

  const { getByLabelText, getByText, findByRole } = render(<PasswordReset />);

  await userEvent.type(getByLabelText("New password"), "test-new-password");
  await userEvent.type(getByLabelText("Repeat new password"), "test-new-password");
  await userEvent.click(getByText("Update password"));

  await waitFor(() => expect(findByRole("loader")).toBeTruthy());

  expect(passwordService.resetPassword).toHaveBeenCalledWith({ code: "test-code", password: "test-new-password" });
  expect(getByText("Test Error")).toBeInTheDocument();
});
