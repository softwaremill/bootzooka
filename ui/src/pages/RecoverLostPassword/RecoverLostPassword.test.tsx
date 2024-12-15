import { screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { RecoverLostPassword } from "./RecoverLostPassword";
import { renderWithClient } from "tests";

const onClaimPasswordReset = jest.fn();

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders header", () => {
  renderWithClient(<RecoverLostPassword />);

  expect(screen.getByText("Recover lost password")).toBeInTheDocument();
});

test("handles password recover success", async () => {
  onClaimPasswordReset.mockResolvedValueOnce({});

  renderWithClient(<RecoverLostPassword />);

  await userEvent.type(screen.getByLabelText("Login or email"), "test-login");
  await userEvent.click(screen.getByText("Reset password"));

  expect(onClaimPasswordReset).toHaveBeenCalledWith({ loginOrEmail: "test-login" });

  await screen.findByRole("success");
  await screen.findByText("Password reset claim success");
});

test("handles password recover error", async () => {
  onClaimPasswordReset.mockRejectedValueOnce(new Error("Test Error"));

  renderWithClient(<RecoverLostPassword />);

  await userEvent.type(screen.getByLabelText("Login or email"), "test-login");
  await userEvent.click(screen.getByText("Reset password"));

  expect(onClaimPasswordReset).toHaveBeenCalledWith({ loginOrEmail: "test-login" });

  await screen.findByRole("error");
});
