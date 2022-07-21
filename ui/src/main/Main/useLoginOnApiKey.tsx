import React from "react";
import { UserContext } from "contexts";
import { userService } from "services";

const useLoginOnApiKey = () => {
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

export default useLoginOnApiKey;
