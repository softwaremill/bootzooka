import React from "react";
import { BrowserRouter as Router, Switch, Route } from "react-router-dom";
import Footer from "./Footer/Footer";
import NavBar from "./NavBar/NavBar";
import Welcome from "./Welcome/Welcome";
import "./App.scss";

const App: React.FC = () => {
  return (
    <div className="App">
      <Router>
        <NavBar user={{ login: "qwe" }} isLoggedIn={true} logout={() => {}} />
        <Switch>
          <Route exact path="/" component={Welcome} />
        </Switch>
        <Footer />
      </Router>
    </div>
  );
};

export default App;
