import React from "react";
import { render } from "@testing-library/react";
import Footer from "./Footer";
import versionService from "../VersionService/VersionService";

jest.mock("../VersionService/VersionService");

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders version data", async () => {
  (versionService.getVersion as jest.Mock).mockResolvedValueOnce({ buildDate: "testDate", buildSha: "testSha" });

  const { getByText, findByText, findAllByRole } = render(<Footer />);

  const info = getByText(/Bootzooka - application scaffolding by /);

  await findAllByRole(/loader/i);

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

  await findAllByRole(/loader/i);

  expect(info).toBeInTheDocument();
  expect(getByText("Error: Test Error")).toBeInTheDocument();
});
