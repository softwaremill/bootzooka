import React from "react";
import immer from "immer";
import noop from "noop-ts";
import { ApiKey, UserDetails } from "../../services/UserService/UserServiceFP";
import { none, Option } from "fp-ts/es6/Option";
import {some} from "fp-ts/Option";

type LoginState = "logged_in" | "logged_out" | "unknown" | "initial";

interface UserLoginState<T extends LoginState> {
  tag: T;
}

interface UserLoggedIn extends UserLoginState<"logged_in"> {
  apiKey: ApiKey;
  user: UserDetails;
}

interface UserLoginStateUnknown extends UserLoginState<"unknown">{
  apiKey: ApiKey;
}

type UserLoggedOut = UserLoginState<"logged_out">;

type InitialLoginState = UserLoginState<"initial">;

export type UserState = UserLoggedIn | UserLoggedOut | UserLoginStateUnknown | InitialLoginState;

const isUserLoggedIn = (state: UserState): state is UserLoggedIn => "apiKey" in state && "user" in state;
const isUserLoginStateUnknown = (state: UserState): state is UserLoginStateUnknown => "apiKey" in state && state.tag === "unknown";
const isUserLoggedOut = (state: UserState): state is UserLoggedOut => "tag" in state && state.tag === "logged_out";
const isUserLoginStateInitial = (state: UserState): state is InitialLoginState => "tag" in state && state.tag === "initial";

interface UserStateMatcher<T> {
  LoggedIn: (state: UserLoggedIn) => T;
  LoggedOut: (state: UserLoggedOut) => T;
  Unknown: (state: UserLoginStateUnknown) => T;
  Initial: (state: InitialLoginState) => T;
}

export const matchUserState = <T extends any>(matcher: UserStateMatcher<T>) => (state: UserState): T => {
  if(isUserLoggedIn(state)) {
    return matcher.LoggedIn(state);
  }
  if(isUserLoginStateUnknown(state)) {
    return matcher.Unknown(state);
  }
  if(isUserLoginStateInitial(state)) {
    return matcher.Initial(state);
  }
  return matcher.LoggedOut(state);
};


export const initialUserState: UserState = { tag: "initial" };

export type UserAction =
  | { type: "SET_LOGIN_STATUS_UNKNOWN"; apiKey: ApiKey }
  | { type: "UPDATE_USER_DATA"; user: Partial<UserDetails> }
  | { type: "LOG_IN"; payload: UserLoggedIn }
  | { type: "LOG_OUT" };

export const UserReducer = (state: UserState, action: UserAction): UserState => {
  switch (action.type) {
    case "SET_LOGIN_STATUS_UNKNOWN":
      return immer(state, () => {
        const userState: UserState = { tag: "unknown", apiKey: action.apiKey };
        return userState;
      });
    case "UPDATE_USER_DATA":
      return immer(state, (draftState) => {
        if (isUserLoggedIn(draftState)) {
          return ({ ...draftState, user: {
              ...draftState.user,
              ...action.user
            }})
        } else {
          return state;
        }
      });
    case "LOG_IN":
      return immer(state, (draftState) => {
        return { user: action.payload.user, apiKey: action.payload.apiKey, tag: "logged_in" };
      });
    case "LOG_OUT":
      return immer(state, (draftState) => {
        return { tag: "logged_out"};
      });
  }
};

type UserContextData = Omit<UserLoggedIn, "tag">;
export const UserContext = React.createContext<{
  user: Option<UserContextData>;
  dispatch: React.Dispatch<UserAction>,
}>({
  user: none,
  dispatch: noop,
});
