import React from "react";
import { BrowserRouter as Router, Switch, Route } from "react-router-dom";
import Footer from "./Footer/Footer";
import Top from "./Top/Top";
import Welcome from "./Welcome/Welcome";
import Login from "./Login/Login";
import Register from "./Register/Register";
import { AppContextProvider } from "./AppContext/AppContext";

const App: React.FC = () => {
  return (
    <AppContextProvider>
      <Router>
        <Top user={{ login: "qwe" }} isLoggedIn={false} logout={() => {}} />
        <Switch>
          <Route exact path="/" component={Welcome} />
          <Route path="/login">
            <Login onLoggedIn={(q) => console.log(q)} isLoggedIn={false} />
          </Route>
          <Route path="/register">
            <Register />
          </Route>
        </Switch>
        <Footer />
      </Router>
    </AppContextProvider>
  );
};

export default App;
