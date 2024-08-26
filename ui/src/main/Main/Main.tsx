import React from "react";
import useLoginOnApiKey from "./useLoginOnApiKey";
import useLocalStoragedApiKey from "./useLocalStoragedApiKey";
import { UserContext } from "contexts";
import { Top, ForkMe, Routes, Footer, Loader } from "../";
import { logout, getCurrentUser, getVersion } from "services";

export const Main: React.FC = () => {
  const {
    state: { loggedIn },
  } = React.useContext(UserContext);

  useLocalStoragedApiKey();
  useLoginOnApiKey(getCurrentUser);

  if (loggedIn === null) {
    return <Loader />;
  }

  return (
    <>
      <Top onLogout={logout} />
      <ForkMe>
        <Routes />
      </ForkMe>
      <Footer onGetVersion={getVersion} />
    </>
  );
};
