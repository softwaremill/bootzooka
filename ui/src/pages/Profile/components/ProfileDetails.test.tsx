import { render, screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { UserContext, UserState } from "contexts";
import { userService } from "services";
import { ProfileDetails } from "./ProfileDetails";

const loggedUserState: UserState = {
  apiKey: "test-api-key",
  user: { login: "user-login", email: "email@address.pl", createdOn: "2020-10-09T09:57:17.995288Z" },
  loggedIn: true,
};
const dispatch = jest.fn();

jest.mock("services");

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders current user data", () => {
  render(
    <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
      <ProfileDetails />
    </UserContext.Provider>,
  );

  expect((screen.getByLabelText("Login") as HTMLInputElement).value).toEqual("user-login");
  expect((screen.getByLabelText("Email address") as HTMLInputElement).value).toEqual("email@address.pl");
});

test("renders lack of current user data", () => {
  render(
    <UserContext.Provider value={{ state: { ...loggedUserState, user: null }, dispatch }}>
      <ProfileDetails />
    </UserContext.Provider>,
  );

  expect((screen.getByLabelText("Login") as HTMLInputElement).value).toEqual("");
  expect((screen.getByLabelText("Email address") as HTMLInputElement).value).toEqual("");
});

test("handles change details success", async () => {
  (userService.changeProfileDetails as jest.Mock).mockResolvedValueOnce({});

  render(
    <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
      <ProfileDetails />
    </UserContext.Provider>,
  );

  await userEvent.clear(screen.getByLabelText("Login"));
  await userEvent.type(screen.getByLabelText("Login"), "test-login");
  await userEvent.clear(screen.getByLabelText("Email address"));
  await userEvent.type(screen.getByLabelText("Email address"), "test@email.address");
  await userEvent.click(screen.getByText("Update profile data"));

  await screen.findByRole("loader");

  expect(userService.changeProfileDetails).toHaveBeenCalledWith("test-api-key", {
    email: "test@email.address",
    login: "test-login",
  });
  expect(dispatch).toHaveBeenCalledTimes(1);
  expect(dispatch).toHaveBeenCalledWith({
    type: "UPDATE_USER_DATA",
    user: {
      email: "test@email.address",
      login: "test-login",
    },
  });
  expect(screen.getByRole("success")).toBeInTheDocument();
  expect(screen.getByText("Profile details changed")).toBeInTheDocument();
});

test("handles change details error", async () => {
  const testError = new Error("Test Error");
  (userService.changeProfileDetails as jest.Mock).mockRejectedValueOnce(testError);

  render(
    <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
      <ProfileDetails />
    </UserContext.Provider>,
  );

  await userEvent.clear(screen.getByLabelText("Login"));
  await userEvent.type(screen.getByLabelText("Login"), "test-login");
  await userEvent.clear(screen.getByLabelText("Email address"));
  await userEvent.type(screen.getByLabelText("Email address"), "test@email.address");
  await userEvent.click(screen.getByText("Update profile data"));

  await screen.findByRole("loader");

  expect(userService.changeProfileDetails).toHaveBeenCalledWith("test-api-key", {
    email: "test@email.address",
    login: "test-login",
  });
  expect(dispatch).not.toHaveBeenCalled();
  expect(screen.getByText("Test Error")).toBeInTheDocument();

  await userEvent.type(screen.getByLabelText("Email address"), "otherTest@email.address");

  expect(screen.queryByRole("error")).not.toBeInTheDocument();
  expect(screen.queryByText("Test Error")).not.toBeInTheDocument();
});
