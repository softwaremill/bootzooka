import { render, waitFor } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { passwordService } from "services";
import { RecoverLostPassword } from "./RecoverLostPassword";

jest.mock("services");

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

  await userEvent.type(getByLabelText("Login or email"), "test-login");
  await userEvent.click(getByText("Reset password"));

  await waitFor(() => expect(findByRole("loader")).toBeTruthy());

  expect(passwordService.claimPasswordReset).toHaveBeenCalledWith({ loginOrEmail: "test-login" });
  expect(getByRole("success")).toBeInTheDocument();
  expect(getByText("Password reset claim success")).toBeInTheDocument();
});

test("handles password recover error", async () => {
  const testError = new Error("Test Error");
  (passwordService.claimPasswordReset as jest.Mock).mockRejectedValueOnce(testError);

  const { getByLabelText, getByText, findByRole } = render(<RecoverLostPassword />);

  await userEvent.type(getByLabelText("Login or email"), "test-login");
  await userEvent.click(getByText("Reset password"));

  await waitFor(() => expect(findByRole("loader")).toBeTruthy());

  expect(passwordService.claimPasswordReset).toHaveBeenCalledWith({ loginOrEmail: "test-login" });
  expect(getByText("Test Error")).toBeInTheDocument();
});
