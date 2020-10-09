import React from "react";
import { BrowserRouter as Router, Switch, Route } from "react-router-dom";
import Footer from "./Footer/Footer";
import Top from "./Top/Top";
import Welcome from "./Welcome/Welcome";
import Login from "./Login/Login";
import Register from "./Register/Register";
import { AppContext } from "./AppContext/AppContext";
import userService from "./UserService/UserService";

const App: React.FC = () => {
  const { dispatch, state } = React.useContext(AppContext);
  const { apiKey } = state;

  React.useEffect(() => {
    const getUserData = async () => {
      if (!apiKey) return;

      try {
        const user = await userService.getCurrentUser(apiKey);
        localStorage.setItem("apiKey", apiKey);
        dispatch({
          type: "SET_USER_DATA",
          user,
        });
      } catch (error) {
        const response = error?.response?.data?.error || error?.message || "Unknown error";
        dispatch({
          type: "ADD_MESSAGE",
          message: { content: `Could not register new user! ${response}`, variant: "danger" },
        });
        console.error(error);
      }
    };

    getUserData();
  }, [apiKey, dispatch]);

  React.useEffect(() => {
    const storedApiKey = localStorage.getItem("apiKey");

    if (!storedApiKey) return;

    dispatch({
      type: "SET_API_KEY",
      apiKey: storedApiKey,
    });
  }, [dispatch]);

  return (
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
  );
};

export default App;
