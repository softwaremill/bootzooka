import React from "react";
import { UserContext } from "../../contexts/UserContext/UserContext";
import userService from "../../services/UserService/UserService";
import { pipe } from "fp-ts/pipeable";
import { map, some } from "fp-ts/Option";

const useLoginOnApiKey = () => {
  const {
    dispatch,
    user,
  } = React.useContext(UserContext);

  React.useEffect(() => {
    console.log('useLoginOnApiKey');
    pipe(
      user,
      map(({ apiKey }) => {
        userService
          .getCurrentUser(apiKey)
          .then((user) => dispatch({ type: "LOG_IN", payload: { apiKey, tag: "logged_in", user: user } }))
          .catch(() => dispatch({ type: "LOG_OUT" }));
      })
    )


  }, [user, dispatch]);
};

export default useLoginOnApiKey;
