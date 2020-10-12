import React from "react";
import { BrowserRouter as Router, Switch, Route } from "react-router-dom";
import Footer from "./Footer/Footer";
import Top from "./Top/Top";
import Welcome from "./Welcome/Welcome";
import Login from "./Login/Login";
import Register from "./Register/Register";
import SecretMain from "./SecretMain/SecretMain";
import ProfileDetails from "./ProfileDetails/ProfileDetails";
import PasswordDetails from "./PasswordDetails/PasswordDetails";
import RecoverLostPassword from "./RecoverLostPassword/RecoverLostPassword";
import PasswordReset from "./PasswordReset/PasswordReset";
import NotFound from "./NotFound/NotFound";
import ForkMe from "./ForkMe/ForkMe";
import { AppContext } from "./AppContext/AppContext";
import userService from "./UserService/UserService";
import ProtectedRoute from "./ProtectedRoute/ProtectedRoute";
import Loader from "./Loader/Loader";

const App: React.FC = () => {
  const { dispatch, state } = React.useContext(AppContext);
  const { apiKey, loggedIn } = state;

  React.useEffect(() => {
    const getUserData = async () => {
      if (!apiKey) {
        return;
      }

      try {
        const user = await userService.getCurrentUser(apiKey);
        localStorage.setItem("apiKey", apiKey);
        dispatch({
          type: "SET_USER_DATA",
          user,
        });
        dispatch({
          type: "SET_LOGGED_IN",
          loggedIn: true,
        });
      } catch (error) {
        const response = error?.response?.data?.error || error?.message || "Unknown error";
        dispatch({
          type: "ADD_MESSAGE",
          message: { content: `Could not log in! ${response}`, variant: "danger" },
        });
        dispatch({
          type: "SET_LOGGED_IN",
          loggedIn: false,
        });
        console.error(error);
      }
    };

    getUserData();
  }, [apiKey, dispatch]);

  React.useEffect(() => {
    const storedApiKey = localStorage.getItem("apiKey");

    if (!storedApiKey) {
      dispatch({
        type: "SET_LOGGED_IN",
        loggedIn: false,
      });
      return;
    }

    dispatch({
      type: "SET_API_KEY",
      apiKey: storedApiKey,
    });
  }, [dispatch]);

  if (loggedIn === null) {
    return <Loader />;
  }

  return (
    <Router>
      <Top />
      <ForkMe>
        <Switch>
          <Route exact path="/">
            <Welcome />
          </Route>
          <Route path="/login">
            <Login />
          </Route>
          <Route path="/register">
            <Register />
          </Route>
          <Route path="/recover-lost-password">
            <RecoverLostPassword />
          </Route>
          <Route path="/password-reset">
            <PasswordReset />
          </Route>

          <ProtectedRoute path="/main">
            <SecretMain />
          </ProtectedRoute>
          <ProtectedRoute path="/profile">
            <ProfileDetails />
            <PasswordDetails />
          </ProtectedRoute>

          <Route>
            <NotFound />
          </Route>
        </Switch>
      </ForkMe>
      <Footer />
    </Router>
  );
};

export default App;
