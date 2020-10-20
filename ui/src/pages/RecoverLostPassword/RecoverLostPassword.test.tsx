import React from "react";
import { render, fireEvent } from "@testing-library/react";
import RecoverLostPassword from "./RecoverLostPassword";
import passwordService from "../../services/PasswordService/PasswordService";

jest.mock("../../services/PasswordService/PasswordService");

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders header", () => {
  const { getByText } = render(<RecoverLostPassword />);

  expect(getByText("Recover lost password")).toBeInTheDocument();
});

test("handles password recover success", async () => {
  (passwordService.claimPasswordReset as jest.Mock).mockResolvedValueOnce({});

  const { getByLabelText, getByText, getByRole, findByRole } = render(<RecoverLostPassword />);

  fireEvent.blur(getByLabelText("Login or email"));
  fireEvent.change(getByLabelText("Login or email"), { target: { value: "test-login" } });
  fireEvent.blur(getByLabelText("Login or email"));
  fireEvent.click(getByText("Reset password"));

  await findByRole("loader");

  expect(passwordService.claimPasswordReset).toBeCalledWith({ loginOrEmail: "test-login" });
  expect(getByRole("success")).toBeInTheDocument();
  expect(getByText("Password reset claim success")).toBeInTheDocument();
});

test("handles password recover error", async () => {
  const testError = new Error("Test Error");
  (passwordService.claimPasswordReset as jest.Mock).mockRejectedValueOnce(testError);

  const { getByLabelText, getByText, findByRole } = render(<RecoverLostPassword />);

  fireEvent.blur(getByLabelText("Login or email"));
  fireEvent.change(getByLabelText("Login or email"), { target: { value: "test-login" } });
  fireEvent.blur(getByLabelText("Login or email"));
  fireEvent.click(getByText("Reset password"));

  await findByRole("loader");

  expect(passwordService.claimPasswordReset).toBeCalledWith({ loginOrEmail: "test-login" });
  expect(getByText("Test Error")).toBeInTheDocument();
});
