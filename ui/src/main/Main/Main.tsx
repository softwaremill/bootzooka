import React from "react";
import Footer from "../Footer/Footer";
import Top from "../Top/Top";
import ForkMe from "../ForkMe/ForkMe";
import { UserContext } from "../../contexts/UserContext/UserContext";
import Routes from "../Routes/Routes";
import useLoginOnApiKey from "./useLoginOnApiKey";
import useLocalStoragedApiKey from "./useLocalStoragedApiKey";

const Main: React.FC = () => {
  const {
    user,
  } = React.useContext(UserContext);

  useLocalStoragedApiKey();
  useLoginOnApiKey();

  return <>
    <Top />
    <ForkMe>
      <Routes />
    </ForkMe>
    <Footer />
  </>
};

export default Main;
