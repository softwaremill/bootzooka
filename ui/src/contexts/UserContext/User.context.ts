import { createContext, useContext } from 'react';
import { UserAction, UserState } from './UserContext';
import { initialUserState } from './UserContext.constants';

export const UserContext = createContext<{
  state: UserState;
  dispatch: React.Dispatch<UserAction>;
}>({
  state: initialUserState,
  dispatch: () => {},
});

export const useUserContext = () => useContext(UserContext);
