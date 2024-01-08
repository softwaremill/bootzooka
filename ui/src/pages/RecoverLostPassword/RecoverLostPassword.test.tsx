import { render, screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { passwordService } from "services";
import { RecoverLostPassword } from "./RecoverLostPassword";

jest.mock("services");

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders header", () => {
  render(<RecoverLostPassword />);

  expect(screen.getByText("Recover lost password")).toBeInTheDocument();
});

test("handles password recover success", async () => {
  (passwordService.claimPasswordReset as jest.Mock).mockResolvedValueOnce({});

  render(<RecoverLostPassword />);

  await userEvent.type(screen.getByLabelText("Login or email"), "test-login");
  await userEvent.click(screen.getByText("Reset password"));

  await screen.findByRole("loader");

  expect(passwordService.claimPasswordReset).toHaveBeenCalledWith({ loginOrEmail: "test-login" });
  expect(screen.getByRole("success")).toBeInTheDocument();
  expect(screen.getByText("Password reset claim success")).toBeInTheDocument();
});

test("handles password recover error", async () => {
  const testError = new Error("Test Error");
  (passwordService.claimPasswordReset as jest.Mock).mockRejectedValueOnce(testError);

  render(<RecoverLostPassword />);

  await userEvent.type(screen.getByLabelText("Login or email"), "test-login");
  await userEvent.click(screen.getByText("Reset password"));

  await screen.findByRole("loader");

  expect(passwordService.claimPasswordReset).toHaveBeenCalledWith({ loginOrEmail: "test-login" });
  expect(screen.getByText("Test Error")).toBeInTheDocument();
});
