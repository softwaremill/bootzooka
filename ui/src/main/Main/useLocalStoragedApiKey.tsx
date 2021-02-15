import React from "react";
import { UserContext } from "../../contexts/UserContext/UserContext";
import { fold, fromNullable, some } from "fp-ts/Option";
import { pipe } from "fp-ts/pipeable";

const useLocalStoragedApiKey = () => {
  const {
    dispatch,
    state: { user, apiKey },
  } = React.useContext(UserContext);

  const apiKeyRef = React.useRef(apiKey);

  React.useEffect(() => {
    apiKeyRef.current = apiKey;
  }, [apiKey, dispatch]);

  React.useEffect(() => {

    pipe(
      fromNullable(localStorage.getItem("apiKey")),
      fold(
        () => dispatch({ type: 'LOG_OUT' }),
        key => dispatch({ type: 'SET_API_KEY', apiKey: some(key) })
      )
    );
  }, [dispatch]);

  React.useEffect(() => {
    pipe(apiKeyRef.current,
      fold(
        () => localStorage.removeItem('apiKey'),
        key => localStorage.setItem('apiKey', key),
      )
    )
  }, [user]);
};

export default useLocalStoragedApiKey;
