import React from "react";
import { BrowserRouter as Router, Switch, Route } from "react-router-dom";
import Footer from "./Footer/Footer";
import Top from "./Top/Top";
import Welcome from "./Welcome/Welcome";
import Container from "react-bootstrap/Container";

const App: React.FC = () => {
  return (
    <div className="App">
      <Router>
        <Top user={{ login: "qwe" }} isLoggedIn={true} logout={() => {}} />
        <Container>
          <Switch>
            <Route exact path="/" component={Welcome} />
          </Switch>
        </Container>
        <Footer />
      </Router>
    </div>
  );
};

export default App;
