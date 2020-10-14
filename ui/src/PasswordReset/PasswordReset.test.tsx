import React from "react";
import { render, fireEvent } from "@testing-library/react";
import PasswordReset from "./PasswordReset";
import { AppContext, initialAppState } from "../AppContext/AppContext";
import passwordService from "../PasswordService/PasswordService";
import { Router } from "react-router-dom";
import { createMemoryHistory } from "history";

const history = createMemoryHistory({ initialEntries: ["/password-reset"] });

jest.mock("../PasswordService/PasswordService");
console.error = jest.fn();
const dispatch = jest.fn();

delete (window as any).location;
(window as any).location = new URL("https://www.example.com/password-reset?code=test-code");

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders header", () => {
  const { getByText } = render(
        <PasswordReset />
  );

  expect(getByText("Password details")).toBeInTheDocument();
});

test("handles password reset success", async () => {
  (passwordService.resetPassword as jest.Mock).mockResolvedValueOnce({});

  const { getByLabelText, getByText, findByRole } = render(
        <PasswordReset />
  );

  fireEvent.blur(getByLabelText("New password"));
  fireEvent.change(getByLabelText("New password"), { target: { value: "test-new-password" } });
  fireEvent.blur(getByLabelText("New password"));
  fireEvent.change(getByLabelText("Repeat new password"), { target: { value: "test-new-password" } });
  fireEvent.blur(getByLabelText("Repeat new password"));
  fireEvent.click(getByText("Update password"));

  await findByRole(/loader/i);

  expect(passwordService.resetPassword).toBeCalledWith({ code: "test-code", password: "test-new-password" });
  expect(getByText("Password reset success.")).toBeInTheDocument();
});

test("handles password reset error", async () => {
  const testError = new Error("Test Error");
  (passwordService.resetPassword as jest.Mock).mockRejectedValueOnce(testError);

  const { getByLabelText, getByText, findByRole } = render(
      <PasswordReset />
  );

  fireEvent.blur(getByLabelText("New password"));
  fireEvent.change(getByLabelText("New password"), { target: { value: "test-new-password" } });
  fireEvent.blur(getByLabelText("New password"));
  fireEvent.change(getByLabelText("Repeat new password"), { target: { value: "test-new-password" } });
  fireEvent.blur(getByLabelText("Repeat new password"));
  fireEvent.click(getByText("Update password"));

  await findByRole(/loader/i);

  expect(passwordService.resetPassword).toBeCalledWith({ code: "test-code", password: "test-new-password" });
  expect(getByText("Error: Test Error")).toBeInTheDocument();
});
