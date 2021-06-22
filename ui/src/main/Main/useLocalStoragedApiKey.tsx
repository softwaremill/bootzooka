import React from "react";
import { UserContext } from "../../contexts/UserContext/UserContext";
import {fold, fromNullable, map, some} from "fp-ts/Option";
import { pipe } from "fp-ts/pipeable";

const useLocalStorageApiKey = () => {
  const {
    dispatch,
    user,
  } = React.useContext(UserContext);

  React.useEffect(() => {
    pipe(
      user,
      map(({ apiKey }) => apiKey),
      fold(
        () => localStorage.removeItem('apiKey'),
        key => localStorage.setItem('apiKey', key),
      )
    )
  }, [user]);

  React.useEffect(() => {
    pipe(
      fromNullable(localStorage.getItem("apiKey")),
      fold(
        () => dispatch({ type: 'LOG_OUT' }),
        apiKey => dispatch({ type: 'SET_LOGIN_STATUS_UNKNOWN', apiKey })
      )
    );
  }, [dispatch]);

};

export default useLocalStoragedApiKey;
