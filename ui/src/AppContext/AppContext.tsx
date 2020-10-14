import React from "react";
import immer from "immer";

export interface User {
  createdOn: string;
  email: string;
  login: string;
}

export interface AppState {
  apiKey: string | null;
  user: Partial<User>;
  loggedIn: boolean | null;
}

export const initialAppState: AppState = {
  apiKey: null,
  user: {},
  loggedIn: null,
};

type AppAction =
  | { type: "SET_API_KEY"; apiKey: string | null }
  | { type: "SET_USER_DATA"; user: Partial<User> }
  | { type: "SET_LOGGED_IN"; loggedIn: boolean | null };

const AppReducer = (state: AppState, action: AppAction): AppState => {
  switch (action.type) {
    case "SET_API_KEY":
      return immer(state, (draftState) => {
        draftState.apiKey = action.apiKey;
      });
    case "SET_USER_DATA":
      return immer(state, (draftState) => {
        draftState.user = {...draftState.user, ...action.user};
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
  state: initialAppState,
  dispatch: () => null,
});

export const AppContextProvider: React.FC = ({ children }) => {
  const [state, dispatch] = React.useReducer(AppReducer, initialAppState);

  return <AppContext.Provider value={{ state, dispatch }}>{children}</AppContext.Provider>;
};
