import React from "react";
import immer from "immer";
import noop from "noop-ts";

export interface UserDetails {
  createdOn: string;
  email: string;
  login: string;
}

export interface UserState {
  apiKey: string | null;
  user: UserDetails | null;
  loggedIn: boolean | null;
}

export const initialUserState: UserState = {
  apiKey: null,
  user: null,
  loggedIn: null,
};

export type UserAction =
  | { type: "SET_API_KEY"; apiKey: string | null }
  | { type: "UPDATE_USER_DATA"; user: Partial<UserDetails> }
  | { type: "LOG_IN"; user: UserDetails }
  | { type: "LOG_OUT" };

const UserReducer = (state: UserState, action: UserAction): UserState => {
  switch (action.type) {
    case "SET_API_KEY":
      return immer(state, (draftState) => {
        draftState.apiKey = action.apiKey;
      });
    case "UPDATE_USER_DATA":
      return immer(state, (draftState) => {
        if (!draftState.user) return;
        draftState.user = { ...draftState.user, ...action.user };
      });
    case "LOG_IN":
      return immer(state, (draftState) => {
        draftState.user = action.user;
        draftState.loggedIn = true;
      });
    case "LOG_OUT":
      return immer(state, (draftState) => {
        draftState.apiKey = null;
        draftState.user = null;
        draftState.loggedIn = false;
      });
  }
};

export const UserContext = React.createContext<{
  state: UserState;
  dispatch: React.Dispatch<UserAction>;
}>({
  state: initialUserState,
  dispatch: noop,
});

export const UserContextProvider: React.FC = ({ children }) => {
  const [state, dispatch] = React.useReducer(UserReducer, initialUserState);

  return <UserContext.Provider value={{ state, dispatch }}>{children}</UserContext.Provider>;
};
