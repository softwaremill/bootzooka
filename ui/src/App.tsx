import React from "react";
import { BrowserRouter } from "react-router-dom";
import Main from "./main/Main/Main";
import {initialUserState, UserReducer, UserContext, matchUserState} from "./contexts/UserContext/UserContext";
import {some} from "fp-ts/Option";

const App: React.FC = () => {
  const [state, dispatch] = React.useReducer(UserReducer, initialUserState);

  return (
    <BrowserRouter>
      {matchUserState({
        LoggedIn: userLoggedIn => (<UserContext.Provider value={ {user: some(userLoggedIn), dispatch }}>
          <Main />
        </UserContext.Provider>),
        LoggedOut: () => <h1>logged out</h1>,
        Unknown: () => <h1>unknown</h1>,
        Initial: () => <h1>initial</h1>
      })}
    </BrowserRouter>
  );
}

export default App;
