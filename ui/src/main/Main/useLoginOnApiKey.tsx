import React from "react";
import { UserContext } from "contexts";
import {useGetUser} from "../../api/apiComponents";

const useLoginOnApiKey = () => {
  const {
    dispatch,
    state: { apiKey },
  } = React.useContext(UserContext);

  const {} = useGetUser({});

};

export default useLoginOnApiKey;
