import React from "react";
import Footer from "./Footer/Footer";
import Top from "./Top/Top";
import ForkMe from "./ForkMe/ForkMe";
import { UserContext } from "./UserContext/UserContext";
import Loader from "./Loader/Loader";
import Routes from "./Routes";
import useApiKeyManager from "./useApiKeyManager";

const Main: React.FC = () => {
  const {
    state: { loggedIn },
  } = React.useContext(UserContext);

  useApiKeyManager();

  if (loggedIn === null) {
    return <Loader />;
  }

  return (
    <>
      <Top />
      <ForkMe>
        <Routes />
      </ForkMe>
      <Footer />
    </>
  );
};

export default Main;
