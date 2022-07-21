import React from "react";
import { BrowserRouter } from "react-router-dom";
import { Main } from "main";
import { UserContextProvider } from "contexts";

export const App: React.FC = () => (
  <BrowserRouter>
    <UserContextProvider>
      <Main />
    </UserContextProvider>
  </BrowserRouter>
);
