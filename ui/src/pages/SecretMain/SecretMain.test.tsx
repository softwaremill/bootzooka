import { render, screen } from "@testing-library/react";
import { SecretMain } from "./SecretMain";

test("renders text content", () => {
  render(<SecretMain />);
  const header = screen.getByText(/Shhhh, this is a secret place./i);
  expect(header).toBeInTheDocument();
});
