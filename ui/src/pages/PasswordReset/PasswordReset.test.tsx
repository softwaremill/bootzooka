import { render, screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { PasswordReset } from "./PasswordReset";
import { passwordService } from "services";
import { mockAndDelayRejectedValueOnce, mockAndDelayResolvedValueOnce } from "../../setupTests";

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
  mockAndDelayResolvedValueOnce(passwordService.resetPassword as jest.Mock, {});

  render(<PasswordReset />);

  await userEvent.type(screen.getByLabelText("New password"), "test-new-password");
  await userEvent.type(screen.getByLabelText("Repeat new password"), "test-new-password");
  await userEvent.click(screen.getByText("Update password"));

  await screen.findByRole("loader");
  await screen.findByRole("success");
  await screen.findByText("Password changed");

  expect(passwordService.resetPassword).toHaveBeenCalledWith({ code: "test-code", password: "test-new-password" });
});

test("handles lack of url code", async () => {
  delete (window as any).location;
  (window as any).location = new URL("https://www.example.com/password-reset");
  mockAndDelayResolvedValueOnce(passwordService.resetPassword as jest.Mock, {});

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
  mockAndDelayRejectedValueOnce(passwordService.resetPassword as jest.Mock, new Error("Test Error"));

  render(<PasswordReset />);

  await userEvent.type(screen.getByLabelText("New password"), "test-new-password");
  await userEvent.type(screen.getByLabelText("Repeat new password"), "test-new-password");
  await userEvent.click(screen.getByText("Update password"));

  await screen.findByRole("loader");
  await screen.findByText("Test Error");

  expect(passwordService.resetPassword).toHaveBeenCalledWith({ code: "test-code", password: "test-new-password" });
});
