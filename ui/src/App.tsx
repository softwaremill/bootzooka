import React from "react";
import { BrowserRouter as Router, Switch, Route } from "react-router-dom";
import Footer from "./Footer/Footer";
import Top from "./Top/Top";
import Welcome from "./Welcome/Welcome";
import Login from "./Login/Login";

const App: React.FC = () => {
  return (
    <Router>
      <Top user={{ login: "qwe" }} isLoggedIn={false} logout={() => {}} />

      <Switch>
        <Route exact path="/" component={Welcome} />
        <Route path="/login">
          <Login onLoggedIn={(q) => console.log(q)} notifyError={(q) => console.log(q)} isLoggedIn={false} />
        </Route>
      </Switch>

      <Footer />
    </Router>
  );
};

export default App;
