import { render } from "@testing-library/react";
import { UserContext, UserState } from "contexts";
import { Profile } from "./Profile";

const mockState: UserState = {
  apiKey: "test-api-key",
  user: { login: "user-login", email: "email@address.pl", createdOn: "2020-10-09T09:57:17.995288Z" },
  loggedIn: true,
};

const dispatch = jest.fn();

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders headers", () => {
  const { getByText } = render(
    <UserContext.Provider value={{ state: mockState, dispatch }}>
      <Profile />
    </UserContext.Provider>
  );

  expect(getByText("Profile details")).toBeInTheDocument();
  expect(getByText("Password details")).toBeInTheDocument();
});
