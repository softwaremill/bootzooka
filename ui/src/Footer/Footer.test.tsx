import React from "react";
import { render } from "@testing-library/react";
import Footer from "./Footer";
import versionService from "../VersionService/VersionService";

jest.mock("../VersionService/VersionService");
console.error = jest.fn();

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders version data", async () => {
  (versionService.getVersion as jest.Mock).mockImplementationOnce(async () => ({
    buildDate: "testDate",
    buildSha: "testSha",
  }));

  const { getByText, findByText, findByRole } = render(<Footer />);

  const info = getByText(/Bootzooka - application scaffolding by /);

  await findByRole(/loader/i);

  const buildDate = await findByText(/testDate/i);
  const buildSha = await findByText(/testSha/i);

  expect(versionService.getVersion).toBeCalledWith();
  expect(info).toBeInTheDocument();
  expect(buildDate).toBeInTheDocument();
  expect(buildSha).toBeInTheDocument();
});

test("catches error of version data", async () => {
  const testError = new Error("Test Error");
  (versionService.getVersion as jest.Mock).mockImplementationOnce(async () => {
    throw testError;
  });

  const { getByText, findByRole } = render(<Footer />);

  const info = getByText(/Bootzooka - application scaffolding by /);

  await findByRole(/loader/i);

  expect(info).toBeInTheDocument();
  expect(console.error).toBeCalledWith(testError);
});
