import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { NotFound } from "./NotFound";

test("renders text content", () => {
  render(<NotFound />, { wrapper: MemoryRouter });
  const header = screen.getByText(/You shouldn't be here for sure :\)/i);
  expect(header).toBeInTheDocument();
});
