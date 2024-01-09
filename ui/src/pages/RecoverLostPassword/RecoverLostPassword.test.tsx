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

  expect(passwordService.claimPasswordReset).toHaveBeenCalledWith({ loginOrEmail: "test-login" });

  await screen.findByRole("success");
  await screen.findByText("Password reset claim success");
});

test("handles password recover error", async () => {
  (passwordService.claimPasswordReset as jest.Mock).mockRejectedValueOnce(new Error("Test Error"));

  render(<RecoverLostPassword />);

  await userEvent.type(screen.getByLabelText("Login or email"), "test-login");
  await userEvent.click(screen.getByText("Reset password"));

  expect(passwordService.claimPasswordReset).toHaveBeenCalledWith({ loginOrEmail: "test-login" });

  await screen.findByRole("error");
});
