import React from "react";
import { UserContext } from "../UserContext/UserContext";
import userService from "../UserService/UserService";

const useLocalStoragedApiKey = () => {
  const {
    dispatch,
    state: { apiKey },
  } = React.useContext(UserContext);

  React.useEffect(() => {
    if (!apiKey) return;

    userService
      .getCurrentUser(apiKey)
      .then((user) => dispatch({ type: "LOG_IN", user }))
      .catch(() => dispatch({ type: "LOG_OUT" }));
  }, [apiKey, dispatch]);
};

export default useLocalStoragedApiKey;
