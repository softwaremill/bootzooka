import { render } from "@testing-library/react";
import { Footer } from "./Footer";
import { versionService } from "services";

jest.mock("services");

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders version data", async () => {
  (versionService.getVersion as jest.Mock).mockResolvedValueOnce({ buildDate: "testDate", buildSha: "testSha" });

  const { getByText, findByText, findAllByRole } = render(<Footer />);

  const info = getByText(/Bootzooka - application scaffolding by /);

  await findAllByRole("loader");

  const buildSha = await findByText(/testSha/i);

  expect(versionService.getVersion).toBeCalledWith();
  expect(info).toBeInTheDocument();
  expect(buildSha).toBeInTheDocument();
});
