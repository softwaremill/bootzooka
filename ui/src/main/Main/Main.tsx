import { useContext } from 'react';
import { UserContext } from 'contexts/UserContext/User.context';
import { Top } from 'main/Top/Top';
import { Footer } from 'main/Footer/Footer';
import { Loader } from 'main/Loader/Loader';
import { ForkMe } from 'main/ForkMe/ForkMe';
import { Routes } from 'main/Routes/Routes';
import useLoginOnApiKey from './useLoginOnApiKey';
import useLocalStorageApiKey from './useLocalStorageApiKey';

export const Main = () => {
  const {
    state: { loggedIn },
  } = useContext(UserContext);

  useLocalStorageApiKey();
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
