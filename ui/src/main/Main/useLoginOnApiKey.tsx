import React from "react";
import { UserContext } from "contexts";
import { UserDetails } from "services";

const useLoginOnApiKey = (getCurrentUser: (apiKey: string) => Promise<UserDetails>) => {
  const {
    dispatch,
    state: { apiKey },
  } = React.useContext(UserContext);

  React.useEffect(() => {
    if (!apiKey) return;

    getCurrentUser(apiKey)
      .then((user) => dispatch({ type: "LOG_IN", user }))
      .catch(() => dispatch({ type: "LOG_OUT" }));
  }, [apiKey, dispatch, getCurrentUser]);
};

export default useLoginOnApiKey;
