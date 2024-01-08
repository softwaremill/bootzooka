import { render, screen } from "@testing-library/react";
import { Loader } from "./Loader";

test("renders loader", () => {
  render(<Loader />);
  const header = screen.getByRole("loader");
  expect(header).toBeInTheDocument();
});
