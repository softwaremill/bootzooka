import { screen } from "@testing-library/react";
import { Footer } from "./Footer";
import { renderWithClient } from "tests";

const onGetVersion = jest.fn();

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders version data", async () => {
  onGetVersion.mockResolvedValueOnce({ buildDate: "testDate", buildSha: "testSha" });

  renderWithClient(<Footer />);

  const info = screen.getByText(/Bootzooka - application scaffolding by /);

  await screen.findAllByRole("loader");

  const buildSha = await screen.findByText(/testSha/i);

  expect(onGetVersion).toHaveBeenCalled();
  expect(info).toBeInTheDocument();
  expect(buildSha).toBeInTheDocument();
});
