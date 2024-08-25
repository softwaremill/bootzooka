import axios from "axios";
import { userService } from "./UserService";

jest.mock("axios");

afterEach(() => {
  jest.clearAllMocks();
});

test("logs out user", async () => {
  const data = {};
  const testApiKey = "test-api-key";

  (axios.request as jest.Mock).mockResolvedValueOnce({ data });

  await expect(userService.logout(testApiKey)).resolves.toEqual(data);
  expect(axios.request).toHaveBeenCalledWith({
    headers: { Authorization: `Bearer ${testApiKey}` },
    method: "POST",
    url: "api/v1/user/logout",
    data: { apiKey: testApiKey },
  });
});

test("gets current user", async () => {
  const data = { login: "test-login", email: "test-email", createdOn: "test-date" };
  const testApiKey = "test-api-key";

  (axios.request as jest.Mock).mockResolvedValueOnce({ data });

  await expect(userService.getCurrentUser(testApiKey)).resolves.toEqual(data);
  expect(axios.request).toHaveBeenCalledWith({
    headers: { Authorization: `Bearer ${testApiKey}` },
    method: "GET",
    url: "api/v1/user",
  });
});

test("changes profile details", async () => {
  const data = {};
  const testApiKey = "test-api-key";
  const params = { email: "new-email", login: "new-login" };

  (axios.request as jest.Mock).mockResolvedValueOnce({ data });

  await expect(userService.changeProfileDetails(testApiKey, params)).resolves.toEqual(data);
  expect(axios.request).toHaveBeenCalledWith({
    headers: { Authorization: `Bearer ${testApiKey}` },
    method: "POST",
    url: "api/v1/user",
    data: params,
  });
});

test("changes password", async () => {
  const data = { apiKey: "new-api-key" };
  const testApiKey = "test-api-key";
  const params = { currentPassword: "test-current-password", newPassword: "test-new-password" };

  (axios.request as jest.Mock).mockResolvedValueOnce({ data });

  await expect(userService.changePassword(testApiKey, params)).resolves.toEqual(data);
  expect(axios.request).toHaveBeenCalledWith({
    headers: { Authorization: `Bearer ${testApiKey}` },
    method: "POST",
    url: "api/v1/user/changepassword",
    data: params,
  });
});
