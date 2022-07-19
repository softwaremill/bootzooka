import { render } from "@testing-library/react";
import Welcome from "./Welcome";
import { MemoryRouter } from "react-router-dom";

test("renders text content", () => {
  const { getByText } = render(
    <MemoryRouter initialEntries={["/login"]}>
      <Welcome />
    </MemoryRouter>
  );
  const header = getByText("Welcome to Bootzooka!");
  expect(header).toBeInTheDocument();
});
