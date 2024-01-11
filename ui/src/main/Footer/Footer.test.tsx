import { screen } from "@testing-library/react";
import { Footer } from "./Footer";
import { versionService } from "services";
import { renderWithClient } from "tests";

jest.mock("services");

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders version data", async () => {
  (versionService.getVersion as jest.Mock).mockResolvedValueOnce({ buildDate: "testDate", buildSha: "testSha" });

  renderWithClient(<Footer />);

  const info = screen.getByText(/Bootzooka - application scaffolding by /);

  await screen.findAllByRole("loader");

  const buildSha = await screen.findByText(/testSha/i);

  expect(versionService.getVersion).toHaveBeenCalled();
  expect(info).toBeInTheDocument();
  expect(buildSha).toBeInTheDocument();
});
