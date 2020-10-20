import React from "react";
import { UserContext } from "../../contexts/UserContext/UserContext";

const useLocalStoragedApiKey = () => {
  const {
    dispatch,
    state: { apiKey, loggedIn },
  } = React.useContext(UserContext);

  const apiKeyRef = React.useRef(apiKey);

  React.useEffect(() => {
    apiKeyRef.current = apiKey;
  }, [apiKey, dispatch]);

  React.useEffect(() => {
    const storedApiKey = localStorage.getItem("apiKey");

    if (!storedApiKey) return dispatch({ type: "LOG_OUT" });

    dispatch({ type: "SET_API_KEY", apiKey: storedApiKey });
  }, [dispatch]);

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
};

export default useLocalStoragedApiKey;
