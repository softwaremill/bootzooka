import React from "react";
import { BrowserRouter } from "react-router-dom";
import Layout from "./Layout";
import UserManager from "./UserManager/UserManager";

const App: React.FC = () => (
  <UserManager>
    <BrowserRouter>
      <Layout />
    </BrowserRouter>
  </UserManager>
);

export default App;
