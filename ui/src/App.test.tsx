import { render, screen } from "@testing-library/react";
import { App } from "./App";

test("should render", () => {
  render(<App />);
  const header = screen.getByText("Welcome to Bootzooka!");
  expect(header).toBeInTheDocument();
});
