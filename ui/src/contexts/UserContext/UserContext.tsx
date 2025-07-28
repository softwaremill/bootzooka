import { ReactNode, useReducer } from 'react';
import { produce } from 'immer';
import { initialUserState } from './UserContext.constants';
import { UserContext } from './User.context';

export interface UserDetails {
  createdOn: string;
  email: string;
  login: string;
}

export interface UserState {
  user: UserDetails | null;
}

export type UserAction =
  | { type: 'UPDATE_USER_DATA'; user: Partial<UserDetails> }
  | { type: 'LOG_IN'; user: UserDetails }
  | { type: 'LOG_OUT' };

const userReducer = (state: UserState, action: UserAction): UserState => {
  switch (action.type) {
    case 'UPDATE_USER_DATA':
      return produce(state, (draftState) => {
        if (!draftState.user) return;
        draftState.user = { ...draftState.user, ...action.user };
      });

    case 'LOG_IN':
      return produce(state, (draftState) => {
        draftState.user = action.user;
      });

    case 'LOG_OUT':
      return produce(state, (draftState) => {
        draftState.user = null;
      });

    default:
      return state;
  }
};

interface UserContextProviderProps {
  children: ReactNode;
}

export const UserContextProvider: React.FC<UserContextProviderProps> = ({
  children,
}) => {
  const [state, dispatch] = useReducer(userReducer, initialUserState);

  return (
    <UserContext.Provider value={{ state, dispatch }}>
      {children}
    </UserContext.Provider>
  );
};
