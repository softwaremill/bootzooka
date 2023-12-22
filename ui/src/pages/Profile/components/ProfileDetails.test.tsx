import { render, waitFor } from "@testing-library/react";
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
  const { getByLabelText } = render(
    <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
      <ProfileDetails />
    </UserContext.Provider>,
  );

  expect((getByLabelText("Login") as HTMLInputElement).value).toEqual("user-login");
  expect((getByLabelText("Email address") as HTMLInputElement).value).toEqual("email@address.pl");
});

test("renders lack of current user data", () => {
  const { getByLabelText } = render(
    <UserContext.Provider value={{ state: { ...loggedUserState, user: null }, dispatch }}>
      <ProfileDetails />
    </UserContext.Provider>,
  );

  expect((getByLabelText("Login") as HTMLInputElement).value).toEqual("");
  expect((getByLabelText("Email address") as HTMLInputElement).value).toEqual("");
});

test("handles change details success", async () => {
  (userService.changeProfileDetails as jest.Mock).mockResolvedValueOnce({});

  const { getByLabelText, getByText, getByRole, findByRole } = render(
    <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
      <ProfileDetails />
    </UserContext.Provider>,
  );

  await userEvent.clear(getByLabelText("Login"));
  await userEvent.type(getByLabelText("Login"), "test-login");
  await userEvent.clear(getByLabelText("Email address"));
  await userEvent.type(getByLabelText("Email address"), "test@email.address");
  await userEvent.click(getByText("Update profile data"));

  await waitFor(() => expect(findByRole("loader")).toBeTruthy());

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
  expect(getByRole("success")).toBeInTheDocument();
  expect(getByText("Profile details changed")).toBeInTheDocument();
});

test("handles change details error", async () => {
  const testError = new Error("Test Error");
  (userService.changeProfileDetails as jest.Mock).mockRejectedValueOnce(testError);

  const { getByLabelText, getByText, findByRole, queryByRole, queryByText } = render(
    <UserContext.Provider value={{ state: loggedUserState, dispatch }}>
      <ProfileDetails />
    </UserContext.Provider>,
  );

  await userEvent.clear(getByLabelText("Login"));
  await userEvent.type(getByLabelText("Login"), "test-login");
  await userEvent.clear(getByLabelText("Email address"));
  await userEvent.type(getByLabelText("Email address"), "test@email.address");
  await userEvent.click(getByText("Update profile data"));

  await waitFor(() => expect(findByRole("loader")).toBeTruthy());

  expect(userService.changeProfileDetails).toHaveBeenCalledWith("test-api-key", {
    email: "test@email.address",
    login: "test-login",
  });
  expect(dispatch).not.toHaveBeenCalled();
  expect(getByText("Test Error")).toBeInTheDocument();

  await userEvent.type(getByLabelText("Email address"), "otherTest@email.address");

  expect(queryByRole("error")).not.toBeInTheDocument();
  expect(queryByText("Test Error")).not.toBeInTheDocument();
});
