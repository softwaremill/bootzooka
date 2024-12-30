import React, { useEffect } from "react";
import { UserContext } from "contexts";
import { useGetUser } from "api/apiComponents";

const useLoginOnApiKey = () => {
  const {
    dispatch,
    state: { apiKey },
  } = React.useContext(UserContext);

  const { data, refetch } = useGetUser({ headers: { Authorization: `Bearer ${apiKey}` } }, { retry: 1 });

  useEffect(() => {
    if (!apiKey) return;

    refetch()
      .then((response) => {
        if (response.data) {
          dispatch({ type: "LOG_IN", user: response.data });
        } else {
          dispatch({ type: "LOG_OUT" });
        }
      })
      .catch((error) => {
        dispatch({ type: "LOG_OUT" });
      });
  }, [apiKey, dispatch]);
};

export default useLoginOnApiKey;
