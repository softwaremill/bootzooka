import { Top } from 'main/Top/Top';
import { Footer } from 'main/Footer/Footer';
import { ForkMe } from 'main/ForkMe/ForkMe';
import { Routes } from 'main/Routes/Routes';
import { useUserCheck } from 'hooks/auth';

export const Main = () => {
  useUserCheck();

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
