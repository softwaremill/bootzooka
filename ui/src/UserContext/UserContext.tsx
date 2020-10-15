import React from "react";
import immer from "immer";

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

type UserAction =
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

    default:
      return state;
  }
};

interface UserInterface extends UserState {
  logIn: (user: UserDetails) => void;
  logOut: () => void;
  setApiKey: (apiKey: string) => void;
  updateUserData: (user: Partial<UserDetails>) => void;
}

export const UserContext = React.createContext<UserInterface>({
  ...initialUserState,
  logIn: () => {},
  logOut: () => {},
  setApiKey: () => {},
  updateUserData: () => {},
});

export const UserContextProvider: React.FC = ({ children }) => {
  const [state, dispatch] = React.useReducer(UserReducer, initialUserState);

  const logIn = React.useCallback((user) => dispatch({ type: "LOG_IN", user }), [dispatch]);
  const logOut = React.useCallback(() => dispatch({ type: "LOG_OUT" }), [dispatch]);
  const setApiKey = React.useCallback((apiKey) => dispatch({ type: "SET_API_KEY", apiKey }), [dispatch]);
  const updateUserData = React.useCallback((user) => dispatch({ type: "UPDATE_USER_DATA", user }), [dispatch]);

  const value = {
    ...state,
    logIn,
    logOut,
    updateUserData,
    setApiKey,
  };

  return <UserContext.Provider value={value}>{children}</UserContext.Provider>;
};
