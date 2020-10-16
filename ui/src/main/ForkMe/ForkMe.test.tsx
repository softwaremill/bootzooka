import React from "react";
import { render } from "@testing-library/react";
import ForkMe from "./ForkMe";

test("renders image", () => {
  const { getByAltText } = render(<ForkMe />);
  const header = getByAltText(/fork me on github/i);
  expect(header).toBeInTheDocument();
});

test("renders children", () => {
  const { getByText } = render(<ForkMe>test children content</ForkMe>);
  const header = getByText(/test children content/i);
  expect(header).toBeInTheDocument();
});
