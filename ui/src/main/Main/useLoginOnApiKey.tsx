import { useContext, useEffect } from 'react';
import { UserContext } from 'contexts/UserContext/User.context';
import { useGetUser } from 'api/apiComponents';

const useLoginOnApiKey = () => {
  const {
    dispatch,
    state: { apiKey },
  } = useContext(UserContext);

  const result = useGetUser(
    { headers: { Authorization: `Bearer ${apiKey}` } },
    { retry: 1 }
  );

  useEffect(() => {
    if (!apiKey) return;

    result
      ?.refetch()
      .then((response) => {
        if (response.data) {
          dispatch({ type: 'LOG_IN', user: response.data });
        } else {
          dispatch({ type: 'LOG_OUT' });
        }
      })
      .catch(() => {
        dispatch({ type: 'LOG_OUT' });
      });
  }, [apiKey, dispatch, result]);
};

export default useLoginOnApiKey;
