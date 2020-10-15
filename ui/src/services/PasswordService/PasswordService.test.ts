import axios from "axios";
import passwordService from "./PasswordService";

jest.mock("axios");

afterEach(() => {
  jest.clearAllMocks();
});

test("claims reset password", async () => {
  const data = {};
  const params = { loginOrEmail: "test-login-or-email" };

  (axios.post as jest.Mock).mockResolvedValueOnce({ data });

  await expect(passwordService.claimPasswordReset(params)).resolves.toEqual(data);
  expect(axios.post).toBeCalledWith("api/v1/passwordreset/forgot", params);
});

test("resets password", async () => {
  const data = {};
  const params = { code: "test-code", password: "test-password" };

  (axios.post as jest.Mock).mockResolvedValueOnce({ data });

  await expect(passwordService.resetPassword(params)).resolves.toEqual(data);
  expect(axios.post).toBeCalledWith("api/v1/passwordreset/reset", params);
});
