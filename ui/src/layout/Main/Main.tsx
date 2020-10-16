import React from "react";
import Footer from "../Footer/Footer";
import Top from "../Top/Top";
import ForkMe from "../ForkMe/ForkMe";
import { UserContext } from "../../contexts/UserContext/UserContext";
import Loader from "../Loader/Loader";
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
