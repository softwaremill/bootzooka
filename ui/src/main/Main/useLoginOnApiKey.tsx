import React from "react";
import { UserContext } from "../../contexts/UserContext/UserContext";
import userService from "../../services/UserService/UserService";
import { pipe } from "fp-ts/pipeable";
import { map, some } from "fp-ts/Option";

const useLoginOnApiKey = () => {
  const {
    dispatch,
    state: { apiKey },
  } = React.useContext(UserContext);

  React.useEffect(() => {
    console.log('useLoginOnApiKey');
    pipe(
      apiKey,
      map(key => {
        userService
          .getCurrentUser(key)
          .then((user) => dispatch({ type: "LOG_IN", user: some(user) }))
          .catch(() => dispatch({ type: "LOG_OUT" }));
      })
    )


  }, [apiKey, dispatch]);
};

export default useLoginOnApiKey;
