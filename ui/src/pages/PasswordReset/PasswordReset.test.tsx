import { screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { PasswordReset } from "./PasswordReset";
import { passwordService } from "services";
import { renderWithClient } from "tests";

jest.mock("services");

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders header", () => {
  renderWithClient(<PasswordReset />);

  expect(screen.getByText("Password details")).toBeInTheDocument();
});

test("handles password reset success", async () => {
  delete (window as any).location;
  (window as any).location = new URL("https://www.example.com/password-reset?code=test-code");
  (passwordService.resetPassword as jest.Mock).mockResolvedValueOnce({});

  renderWithClient(<PasswordReset />);

  await userEvent.type(screen.getByLabelText("New password"), "test-new-password");
  await userEvent.type(screen.getByLabelText("Repeat new password"), "test-new-password");
  await userEvent.click(screen.getByText("Update password"));

  await screen.findByRole("success");
  await screen.findByText("Password changed");

  expect(passwordService.resetPassword).toHaveBeenCalledWith({ code: "test-code", password: "test-new-password" });
});

test("handles lack of url code", async () => {
  delete (window as any).location;
  (window as any).location = new URL("https://www.example.com/password-reset");
  (passwordService.resetPassword as jest.Mock).mockResolvedValueOnce({});

  renderWithClient(<PasswordReset />);

  await userEvent.type(screen.getByLabelText("New password"), "test-new-password");
  await userEvent.type(screen.getByLabelText("Repeat new password"), "test-new-password");
  await userEvent.click(screen.getByText("Update password"));

  expect(passwordService.resetPassword).toHaveBeenCalledWith({ code: "", password: "test-new-password" });
});

test("handles password reset error", async () => {
  delete (window as any).location;
  (window as any).location = new URL("https://www.example.com/password-reset?code=test-code");
  (passwordService.resetPassword as jest.Mock).mockRejectedValueOnce(new Error("Test Error"));

  renderWithClient(<PasswordReset />);

  await userEvent.type(screen.getByLabelText("New password"), "test-new-password");
  await userEvent.type(screen.getByLabelText("Repeat new password"), "test-new-password");
  await userEvent.click(screen.getByText("Update password"));

  await screen.findByRole("error");

  expect(passwordService.resetPassword).toHaveBeenCalledWith({ code: "test-code", password: "test-new-password" });
});
