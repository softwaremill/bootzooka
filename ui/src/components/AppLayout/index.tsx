import type { FC } from 'react';
import { Outlet } from 'react-router';
import { AppNavbar } from '../AppNavbar';
import { useUserCheck } from '@/hooks/auth';
import { AppFooter } from '../AppFooter';

export const AppLayout: FC = () => {
  useUserCheck();

  return (
    <div className="w-full h-screen max-h-full grid grid-cols-1 lg:grid-cols-[2fr_minmax(800px,1200px)_2fr] grid-rows-[minmax(60px,90px)_12fr_minmax(130px,200px)]">
      <header className="col-start-1 lg:col-start-2 lg:col-end-3 px-8 flex items-center border-b-primary-foreground border-b-2">
        <AppNavbar />
      </header>
      <main className="col-start-1 lg:col-start-2 lg:col-end-3 lg:px-10 py-8">
        <Outlet />
      </main>
      <footer className="col-start-1 lg:col-start-2 lg:col-end-3 lg:px-8 bg-primary-foreground">
        <AppFooter />
      </footer>
    </div>
  );
};
