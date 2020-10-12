import React from "react";
import { render } from "@testing-library/react";
import Footer from "./Footer";
import versionService from "../VersionService/VersionService";

jest.mock("../VersionService/VersionService");
console.error = jest.fn();

beforeEach(() => {
  jest.clearAllMocks();
});

test("catches error of version data", async () => {
  (versionService.getVersion as jest.Mock).mockImplementationOnce(async () => ({
    buildDate: "testDate",
    buildSha: "testSha",
  }));

  const { findByText, findByRole } = render(<Footer />);

  await findByRole(/loader/i);

  const buildDate = await findByText(/testDate/i);
  const buildSha = await findByText(/testSha/i);

  expect(versionService.getVersion).toBeCalledWith();
  expect(buildDate).toBeInTheDocument();
  expect(buildSha).toBeInTheDocument();
});

test("renders varsion data", async () => {
  const testError = new Error("Test Error");
  (versionService.getVersion as jest.Mock).mockImplementationOnce(async () => {
    throw testError;
  });

  const { findByRole } = render(<Footer />);

  await findByRole(/loader/i);

  expect(console.error).toBeCalledWith(testError);
});
