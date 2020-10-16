import React from "react";
import { render } from "@testing-library/react";
import Loader from "./Loader";

test("renders loader", () => {
  const { getByRole } = render(<Loader />);
  const header = getByRole("loader");
  expect(header).toBeInTheDocument();
});
