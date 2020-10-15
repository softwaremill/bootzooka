import React from "react";
import Footer from "../layout/Footer/Footer";
import Top from "../layout/Top/Top";
import ForkMe from "../layout/ForkMe/ForkMe";
import { UserContext } from "../contexts/UserContext/UserContext";
import Loader from "../layout/Loader/Loader";
import Routes from "../Routes/Routes";
import useLoginOnApiKey from "./useLoginOnApiKey";
import useLocalStoragedApiKey from "./useLocalStoragedApiKey";

const Main: React.FC = () => {
  const {
    state: { loggedIn },
  } = React.useContext(UserContext);

  useLocalStoragedApiKey();
  useLoginOnApiKey();

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
