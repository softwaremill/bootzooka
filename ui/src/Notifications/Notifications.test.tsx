import React from "react";
import { render, fireEvent } from "@testing-library/react";
import Notifications from "./Notifications";
import { AppContext, Message, initialAppstate } from "../AppContext/AppContext";

const messages: Message[] = [
  { content: "test-content-1", variant: "danger" },
  { content: "test-content-2", variant: "danger" },
  { content: "test-content-3", variant: "success" },
  { content: "test-content-4", variant: "success" },
  { content: "test-content-5", variant: "success" },
  { content: "test-content-6", variant: "success" },
];
const dispatch = jest.fn();

beforeEach(() => {
  jest.clearAllMocks();
});

test("renders 5 messages in order", () => {
  const { getAllByText } = render(
    <AppContext.Provider value={{ state: { ...initialAppstate, messages }, dispatch }}>
      <Notifications />
    </AppContext.Provider>
  );

  const alerts = getAllByText(/test-content-[0-9]/i);
  expect(alerts).toHaveLength(5);
  expect(alerts[0]).toHaveTextContent("test-content-6");
  expect(alerts[1]).toHaveTextContent("test-content-5");
  expect(alerts[2]).toHaveTextContent("test-content-4");
  expect(alerts[3]).toHaveTextContent("test-content-3");
  expect(alerts[4]).toHaveTextContent("test-content-2");
});

test("dismiss chosen message", () => {
  const dispatch = jest.fn();
  const { getAllByText } = render(
    <AppContext.Provider value={{ state: { ...initialAppstate, messages }, dispatch }}>
      <Notifications />
    </AppContext.Provider>
  );

  const buttons = getAllByText("Close alert");
  fireEvent.click(buttons[1]);

  expect(dispatch).toBeCalledWith({ messageIndex: 4, type: "REMOVE_MESSAGE" });
});
