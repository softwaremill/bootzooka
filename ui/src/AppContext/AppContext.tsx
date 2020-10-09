import React from "react";
import immer from "immer";

export interface Message {
  content: string;
  variant: "success" | "danger" | "warning" | "info";
}

interface AppState {
  messages: Message[];
}

const initialAppstate: AppState = {
  messages: [],
};

type AppAction = { type: "ADD_MESSAGE"; message: Message } | { type: "REMOVE_MESSAGE"; messageIndex: number };

const UIReducer = (state: AppState, action: AppAction): AppState => {
  switch (action.type) {
    case "ADD_MESSAGE":
      return immer(state, (draftState) => {
        draftState.messages.push(action.message);
      });
    case "REMOVE_MESSAGE":
      return immer(state, (draftState) => {
        draftState.messages.splice(action.messageIndex, 1);
      });

    default:
      return state;
  }
};

export const AppContext = React.createContext<{
  state: AppState;
  dispatch: React.Dispatch<AppAction>;
}>({
  state: initialAppstate,
  dispatch: () => null,
});

export const AppContextProvider: React.FC = ({ children }) => {
  const [state, dispatch] = React.useReducer(UIReducer, initialAppstate);

  return <AppContext.Provider value={{ state, dispatch }}>{children}</AppContext.Provider>;
};
