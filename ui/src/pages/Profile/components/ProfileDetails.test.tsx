import { screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { UserContext, UserState } from "contexts";
import { userService } from "services";
import { ProfileDetails } from "./ProfileDetails";
import { renderWithClient } from "tests";

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
  renderWithClient(
    <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
      <ProfileDetails />
    </UserContext.Provider>,
  );

  expect((screen.getByLabelText("Login") as HTMLInputElement).value).toEqual("user-login");
  expect((screen.getByLabelText("Email address") as HTMLInputElement).value).toEqual("email@address.pl");
});

test("renders lack of current user data", () => {
  renderWithClient(
    <UserContext.Provider value={{ state: { ...loggedUserState, user: null }, dispatch }}>
      <ProfileDetails />
    </UserContext.Provider>,
  );

  expect((screen.getByLabelText("Login") as HTMLInputElement).value).toEqual("");
  expect((screen.getByLabelText("Email address") as HTMLInputElement).value).toEqual("");
});

test("handles change details success", async () => {
  (userService.changeProfileDetails as jest.Mock).mockResolvedValueOnce({});

  renderWithClient(
    <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
      <ProfileDetails />
    </UserContext.Provider>,
  );

  await userEvent.clear(screen.getByLabelText("Login"));
  await userEvent.type(screen.getByLabelText("Login"), "test-login");
  await userEvent.clear(screen.getByLabelText("Email address"));
  await userEvent.type(screen.getByLabelText("Email address"), "test@email.address");
  await userEvent.click(screen.getByText("Update profile data"));

  expect(userService.changeProfileDetails).toHaveBeenCalledWith("test-api-key", {
    email: "test@email.address",
    login: "test-login",
  });

  expect(dispatch).toHaveBeenCalledWith({
    type: "UPDATE_USER_DATA",
    user: { email: "test@email.address", login: "test-login" },
  });

  await screen.findByRole("success");
  await screen.findByText("Profile details changed");
});

test("handles change details error", async () => {
  (userService.changeProfileDetails as jest.Mock).mockRejectedValueOnce(new Error("Test Error"));

  renderWithClient(
    <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
      <ProfileDetails />
    </UserContext.Provider>,
  );

  await userEvent.clear(screen.getByLabelText("Login"));
  await userEvent.type(screen.getByLabelText("Login"), "test-login");
  await userEvent.clear(screen.getByLabelText("Email address"));
  await userEvent.type(screen.getByLabelText("Email address"), "test@email.address");
  await userEvent.click(screen.getByText("Update profile data"));

  expect(userService.changeProfileDetails).toHaveBeenCalledWith("test-api-key", {
    email: "test@email.address",
    login: "test-login",
  });
  expect(dispatch).not.toHaveBeenCalled();

  await screen.findByRole("error");
});
