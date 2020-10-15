import React from "react";
import { render } from "@testing-library/react";
import Welcome from "./Welcome";

test("renders text content", () => {
  const { getByText } = render(<Welcome />);
  const header = getByText(/Welcome to Bootzooka!/i);
  expect(header).toBeInTheDocument();
});
