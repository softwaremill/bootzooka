import React from "react";
import { render } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import NotFound from "./NotFound";

test("renders text content", () => {
  const { getByText } = render(<NotFound />, { wrapper: MemoryRouter });
  const header = getByText(/You shouldn\'t be here for sure \:\)/i);
  expect(header).toBeInTheDocument();
});
