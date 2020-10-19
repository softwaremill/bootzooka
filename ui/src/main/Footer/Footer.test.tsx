import React from "react";
import { render } from "@testing-library/react";
import Footer from "./Footer";
import versionService from "../../services/VersionService/VersionService";

jest.mock("../../services/VersionService/VersionService");

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders version data", async () => {
  (versionService.getVersion as jest.Mock).mockResolvedValueOnce({ buildDate: "testDate", buildSha: "testSha" });

  const { getByText, findByText, findAllByRole } = render(<Footer />);

  const info = getByText(/Bootzooka - application scaffolding by /);

  await findAllByRole("loader");

  const buildDate = await findByText(/testDate/i);
  const buildSha = await findByText(/testSha/i);

  expect(versionService.getVersion).toBeCalledWith();
  expect(info).toBeInTheDocument();
  expect(buildDate).toBeInTheDocument();
  expect(buildSha).toBeInTheDocument();
});

test("catches error of version data", async () => {
  const testError = new Error("Test Error");
  (versionService.getVersion as jest.Mock).mockRejectedValueOnce(testError);

  const { getByText, findAllByRole } = render(<Footer />);

  const info = getByText(/Bootzooka - application scaffolding by /);

  await findAllByRole("loader");

  expect(info).toBeInTheDocument();
  expect(getByText("Test Error")).toBeInTheDocument();
});

test("catches undefined error of version data", async () => {
  (versionService.getVersion as jest.Mock).mockRejectedValueOnce(undefined);

  const { getByText, findAllByRole } = render(<Footer />);

  const info = getByText(/Bootzooka - application scaffolding by /);

  await findAllByRole("loader");

  expect(info).toBeInTheDocument();
  expect(getByText("Unknown error")).toBeInTheDocument();
});
