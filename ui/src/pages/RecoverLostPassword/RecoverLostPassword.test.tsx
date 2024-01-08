import { render, screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { passwordService } from "services";
import { RecoverLostPassword } from "./RecoverLostPassword";
import { mockAndDelayRejectedValueOnce, mockAndDelayResolvedValueOnce } from "../../setupTests";

jest.mock("services");

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders header", () => {
  render(<RecoverLostPassword />);

  expect(screen.getByText("Recover lost password")).toBeInTheDocument();
});

test("handles password recover success", async () => {
  mockAndDelayResolvedValueOnce(passwordService.claimPasswordReset as jest.Mock, {});

  render(<RecoverLostPassword />);

  await userEvent.type(screen.getByLabelText("Login or email"), "test-login");
  await userEvent.click(screen.getByText("Reset password"));

  await screen.findByRole("loader");

  expect(passwordService.claimPasswordReset).toHaveBeenCalledWith({ loginOrEmail: "test-login" });

  await screen.findByRole("success");
  await screen.findByText("Password reset claim success");
});

test("handles password recover error", async () => {
  mockAndDelayRejectedValueOnce(passwordService.claimPasswordReset as jest.Mock, new Error("Test Error"));

  render(<RecoverLostPassword />);

  await userEvent.type(screen.getByLabelText("Login or email"), "test-login");
  await userEvent.click(screen.getByText("Reset password"));

  await screen.findByRole("loader");

  expect(passwordService.claimPasswordReset).toHaveBeenCalledWith({ loginOrEmail: "test-login" });

  await screen.findByRole("error");
  await screen.findByText("Test Error");
});
