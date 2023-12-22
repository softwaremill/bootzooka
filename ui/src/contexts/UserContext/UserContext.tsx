import React, { ReactNode } from "react";
import { produce } from "immer";

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

const userReducer = (state: UserState, action: UserAction): UserState => {
  switch (action.type) {
    case "SET_API_KEY":
      return produce(state, (draftState) => {
        draftState.apiKey = action.apiKey;
      });

    case "UPDATE_USER_DATA":
      return produce(state, (draftState) => {
        if (!draftState.user) return;
        draftState.user = { ...draftState.user, ...action.user };
      });

    case "LOG_IN":
      return produce(state, (draftState) => {
        draftState.user = action.user;
        draftState.loggedIn = true;
      });

    case "LOG_OUT":
      return produce(state, (draftState) => {
        draftState.apiKey = null;
        draftState.user = null;
        draftState.loggedIn = false;
      });

    default:
      return state;
  }
};

export const UserContext = React.createContext<{
  state: UserState;
  dispatch: React.Dispatch<UserAction>;
}>({
  state: initialUserState,
  dispatch: () => {},
});

interface UserContextProviderProps {
  children: ReactNode;
}

export const UserContextProvider: React.FC<UserContextProviderProps> = ({ children }) => {
  const [state, dispatch] = React.useReducer(userReducer, initialUserState);

  return <UserContext.Provider value={{ state, dispatch }}>{children}</UserContext.Provider>;
};

export const useUserContext = () => React.useContext(UserContext);
