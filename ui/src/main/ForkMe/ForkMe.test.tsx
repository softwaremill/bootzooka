import { render, screen } from "@testing-library/react";
import { ForkMe } from "./ForkMe";

test("renders image", () => {
  render(<ForkMe />);
  const header = screen.getByAltText(/fork me on github/i);
  expect(header).toBeInTheDocument();
});

test("renders children", () => {
  render(<ForkMe>test children content</ForkMe>);
  const header = screen.getByText(/test children content/i);
  expect(header).toBeInTheDocument();
});
