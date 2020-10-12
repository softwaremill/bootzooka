import React from "react";
import immer from "immer";

export interface Message {
  content: string;
  variant: "success" | "danger" | "warning" | "info";
}

export interface User {
  createdOn: string;
  email: string;
  login: string;
}

interface AppState {
  messages: Message[];
  apiKey: string | null;
  user: User | null;
  loggedIn: boolean | null;
}

const initialAppstate: AppState = {
  messages: [],
  apiKey: null,
  user: null,
  loggedIn: null,
};

type AppAction =
  | { type: "ADD_MESSAGE"; message: Message }
  | { type: "REMOVE_MESSAGE"; messageIndex: number }
  | { type: "SET_API_KEY"; apiKey: string | null }
  | { type: "SET_USER_DATA"; user: User | null }
  | { type: "SET_LOGGED_IN"; loggedIn: boolean | null };

const AppReducer = (state: AppState, action: AppAction): AppState => {
  switch (action.type) {
    case "ADD_MESSAGE":
      return immer(state, (draftState) => {
        draftState.messages.push(action.message);
      });
    case "REMOVE_MESSAGE":
      return immer(state, (draftState) => {
        draftState.messages.splice(action.messageIndex, 1);
      });
    case "SET_API_KEY":
      return immer(state, (draftState) => {
        draftState.apiKey = action.apiKey;
      });
    case "SET_USER_DATA":
      return immer(state, (draftState) => {
        draftState.user = action.user;
      });
    case "SET_LOGGED_IN":
      return immer(state, (draftState) => {
        draftState.loggedIn = action.loggedIn;
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
  const [state, dispatch] = React.useReducer(AppReducer, initialAppstate);

  return <AppContext.Provider value={{ state, dispatch }}>{children}</AppContext.Provider>;
};
