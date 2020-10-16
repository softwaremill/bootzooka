import React from "react";
import { render, fireEvent } from "@testing-library/react";
import PasswordReset from "./PasswordReset";
import passwordService from "../../services/PasswordService/PasswordService";

jest.mock("../../services/PasswordService/PasswordService");

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

  fireEvent.blur(getByLabelText("New password"));
  fireEvent.change(getByLabelText("New password"), { target: { value: "test-new-password" } });
  fireEvent.blur(getByLabelText("New password"));
  fireEvent.change(getByLabelText("Repeat new password"), { target: { value: "test-new-password" } });
  fireEvent.blur(getByLabelText("Repeat new password"));
  fireEvent.click(getByText("Update password"));

  await findByRole("loader");

  expect(passwordService.resetPassword).toBeCalledWith({ code: "test-code", password: "test-new-password" });
  expect(getByRole("success")).toBeInTheDocument();
  expect(getByText("Password changed")).toBeInTheDocument();
});

test("handles lack of url code", async () => {
  delete (window as any).location;
  (window as any).location = new URL("https://www.example.com/password-reset");

  const { getByLabelText, getByText, findByRole } = render(<PasswordReset />);

  fireEvent.blur(getByLabelText("New password"));
  fireEvent.change(getByLabelText("New password"), { target: { value: "test-new-password" } });
  fireEvent.blur(getByLabelText("New password"));
  fireEvent.change(getByLabelText("Repeat new password"), { target: { value: "test-new-password" } });
  fireEvent.blur(getByLabelText("Repeat new password"));
  fireEvent.click(getByText("Update password"));

  await findByRole("loader");

  expect(passwordService.resetPassword).toBeCalledWith({ code: "", password: "test-new-password" });
});

test("handles password reset error", async () => {
  delete (window as any).location;
  (window as any).location = new URL("https://www.example.com/password-reset?code=test-code");
  const testError = new Error("Test Error");
  (passwordService.resetPassword as jest.Mock).mockRejectedValueOnce(testError);

  const { getByLabelText, getByText, findByRole } = render(<PasswordReset />);

  fireEvent.blur(getByLabelText("New password"));
  fireEvent.change(getByLabelText("New password"), { target: { value: "test-new-password" } });
  fireEvent.blur(getByLabelText("New password"));
  fireEvent.change(getByLabelText("Repeat new password"), { target: { value: "test-new-password" } });
  fireEvent.blur(getByLabelText("Repeat new password"));
  fireEvent.click(getByText("Update password"));

  await findByRole("loader");

  expect(passwordService.resetPassword).toBeCalledWith({ code: "test-code", password: "test-new-password" });
  expect(getByText("Test Error")).toBeInTheDocument();
});
