import { useContext, useEffect } from 'react';
import { UserContext } from 'contexts/UserContext/User.context';
import { useGetUser } from 'api/apiComponents';

const useLoginOnApiKey = () => {
  const {
    dispatch,
    state: { apiKey },
  } = useContext(UserContext);

  const { data, isSuccess, isError } = useGetUser(
    { headers: { Authorization: `Bearer ${apiKey}` } },
    {
      retry: 1,
      enabled: Boolean(apiKey)
    }
  );

  useEffect(() => {
    if (isSuccess && data) {
      dispatch({ type: 'LOG_IN', user: data });
    }
  }, [isSuccess, data, dispatch]);

  useEffect(() => {
    if (isError || (isSuccess && !data)) {
      dispatch({ type: 'LOG_OUT' });
    }
  }, [isError, isSuccess, data, dispatch]);
};

export default useLoginOnApiKey;
