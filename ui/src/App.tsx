import React from "react";
import { BrowserRouter } from "react-router-dom";
import Main from "./Main";
import { UserContextProvider } from "./UserContext/UserContext";

const App: React.FC = () => (
  <BrowserRouter>
    <UserContextProvider>
      <Main />
    </UserContextProvider>
  </BrowserRouter>
);

export default App;
