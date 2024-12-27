import React, {useEffect} from "react";
import { UserContext } from "contexts";
import {useGetUser} from "../../api/apiComponents";

const useLoginOnApiKey = () => {
  const {
    dispatch,
    state: { apiKey },
  } = React.useContext(UserContext);


  const { data } = useGetUser({} );

  useEffect(() => {
    if (!apiKey) return;

    if (data) {
      dispatch({ type: "LOG_IN", user: data });
    }
    // else {
    //   dispatch({type: "LOG_OUT"});
    // }
  }, [apiKey, data, dispatch]);

};

export default useLoginOnApiKey;
