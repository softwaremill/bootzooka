import React from "react";
import { render } from "@testing-library/react";
import Welcome from "./Welcome";
import { Router } from "react-router-dom";
import { createMemoryHistory } from "history";

const history = createMemoryHistory({ initialEntries: ["/login"] });

test("renders text content", () => {
  const { getByText } = render(
    <Router history={history}>
      <Welcome />
    </Router>
  );
  const header = getByText("Welcome to Bootzooka!");
  expect(header).toBeInTheDocument();
});
