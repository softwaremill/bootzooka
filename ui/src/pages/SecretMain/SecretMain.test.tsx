import React from "react";
import { render } from "@testing-library/react";
import SecretMain from "./SecretMain";

test("renders text content", () => {
  const { getByText } = render(<SecretMain />);
  const header = getByText(/Shhhh, this is a secret place./i);
  expect(header).toBeInTheDocument();
});
