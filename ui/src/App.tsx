import React from "react";
import { BrowserRouter } from "react-router-dom";
import Main from "./main/Main/Main";
import { UserContextProvider } from "./contexts/UserContext/UserContext";

const App: React.FC = () => (
  <BrowserRouter>
    <UserContextProvider>
      <Main />
    </UserContextProvider>
  </BrowserRouter>
);

export default App;
