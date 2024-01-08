import { render, screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { PasswordReset } from "./PasswordReset";
import { passwordService } from "services";

jest.mock("services");

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders header", () => {
  render(<PasswordReset />);

  expect(screen.getByText("Password details")).toBeInTheDocument();
});

test("handles password reset success", async () => {
  delete (window as any).location;
  (window as any).location = new URL("https://www.example.com/password-reset?code=test-code");
  (passwordService.resetPassword as jest.Mock).mockResolvedValueOnce({});

  render(<PasswordReset />);

  await userEvent.type(screen.getByLabelText("New password"), "test-new-password");
  await userEvent.type(screen.getByLabelText("Repeat new password"), "test-new-password");
  await userEvent.click(screen.getByText("Update password"));

  await screen.findByRole("loader");

  expect(passwordService.resetPassword).toHaveBeenCalledWith({ code: "test-code", password: "test-new-password" });
  expect(screen.getByRole("success")).toBeInTheDocument();
  expect(screen.getByText("Password changed")).toBeInTheDocument();
});

test("handles lack of url code", async () => {
  delete (window as any).location;
  (window as any).location = new URL("https://www.example.com/password-reset");

  render(<PasswordReset />);

  await userEvent.type(screen.getByLabelText("New password"), "test-new-password");
  await userEvent.type(screen.getByLabelText("Repeat new password"), "test-new-password");
  await userEvent.click(screen.getByText("Update password"));

  await screen.findByRole("loader");

  expect(passwordService.resetPassword).toHaveBeenCalledWith({ code: "", password: "test-new-password" });
});

test("handles password reset error", async () => {
  delete (window as any).location;
  (window as any).location = new URL("https://www.example.com/password-reset?code=test-code");
  const testError = new Error("Test Error");
  (passwordService.resetPassword as jest.Mock).mockRejectedValueOnce(testError);

  render(<PasswordReset />);

  await userEvent.type(screen.getByLabelText("New password"), "test-new-password");
  await userEvent.type(screen.getByLabelText("Repeat new password"), "test-new-password");
  await userEvent.click(screen.getByText("Update password"));

  await screen.findByRole("loader");

  expect(passwordService.resetPassword).toHaveBeenCalledWith({ code: "test-code", password: "test-new-password" });
  expect(screen.getByText("Test Error")).toBeInTheDocument();
});
