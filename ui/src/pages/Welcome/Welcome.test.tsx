import { render } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { Welcome } from "./Welcome";

test("renders text content", () => {
  const { getByText } = render(
    <MemoryRouter initialEntries={["/login"]}>
      <Welcome />
    </MemoryRouter>
  );
  const header = getByText("Welcome to Bootzooka!");
  expect(header).toBeInTheDocument();
});
