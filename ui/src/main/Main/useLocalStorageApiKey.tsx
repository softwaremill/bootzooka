import { useContext, useEffect, useRef } from 'react';
import { UserContext } from 'contexts/UserContext/User.context';

const useLocalStorageApiKey = () => {
  const {
    dispatch,
    state: { apiKey, loggedIn },
  } = useContext(UserContext);

  const apiKeyRef = useRef(apiKey);

  useEffect(() => {
    apiKeyRef.current = apiKey;
  }, [apiKey, dispatch]);

  useEffect(() => {
    const storedApiKey = localStorage.getItem('apiKey');

    if (!storedApiKey) return dispatch({ type: 'LOG_OUT' });

    dispatch({ type: 'SET_API_KEY', apiKey: storedApiKey });
  }, [dispatch]);

  useEffect(() => {
    switch (loggedIn) {
      case true:
        return localStorage.setItem('apiKey', apiKeyRef.current || '');
      case false:
        return localStorage.removeItem('apiKey');
      case null:
      default:
        return;
    }
  }, [loggedIn]);
};

export default useLocalStorageApiKey;
