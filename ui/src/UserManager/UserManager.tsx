import React from "react";
import { UserContext } from "../UserContext/UserContext";
import userService from "../UserService/UserService";
import { UserContextProvider } from "../UserContext/UserContext";

const UserManager: React.FC = ({ children }) => (
  <UserContextProvider>
    <Manager />
    {children}
  </UserContextProvider>
);

const Manager: React.FC = () => {
  const { apiKey, loggedIn, logIn, logOut, setApiKey } = React.useContext(UserContext);

  const apiKeyRef = React.useRef(apiKey);

  React.useEffect(() => {
    apiKeyRef.current = apiKey;

    if (!apiKey) return;

    userService.getCurrentUser(apiKey).then(logIn).catch(logOut);
  }, [apiKey, logIn, logOut]);

  React.useEffect(() => {
    const storedApiKey = localStorage.getItem("apiKey");

    if (!storedApiKey) return logOut();

    setApiKey(storedApiKey);
  }, [setApiKey, logOut]);

  React.useEffect(() => {
    switch (loggedIn) {
      case true:
        return localStorage.setItem("apiKey", apiKeyRef.current || "");
      case false:
        return localStorage.removeItem("apiKey");
      case null:
      default:
        return;
    }
  }, [loggedIn]);

  return null;
};

export default UserManager;
