import { screen } from "@testing-library/react";
import { Footer } from "./Footer";
import { renderWithClient } from "tests";
import { useGetAdminVersion } from "api/apiComponents";

jest.mock("api/apiComponents", () => ({
  useGetAdminVersion: jest.fn(),
}));

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders version data", async () => {
  const mockedUseGetAdminVersion = useGetAdminVersion as jest.Mock;

  mockedUseGetAdminVersion.mockReturnValue({
    isPending: false,
    isLoading: false,
    isError: false,
    isSuccess: true,
    data: { buildDate: "testDate", buildSha: "testSha" },
  });

  renderWithClient(<Footer />);

  const info = screen.getByText(/Bootzooka - application scaffolding by /);
  const buildSha = await screen.findByText(/testSha/i);

  expect(mockedUseGetAdminVersion).toHaveBeenCalled();
  expect(info).toBeInTheDocument();
  expect(buildSha).toBeInTheDocument();
});
