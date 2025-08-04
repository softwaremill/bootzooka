import type { FC } from 'react';
import { Outlet } from 'react-router';
import { Navbar } from '../Navbar';
import { useUserCheck } from '@/hooks/auth';
import { AppFooter } from '../AppFooter';

export const AppLayout: FC = () => {
  useUserCheck();

  return (
    <div className="w-full h-screen max-h-full grid grid-cols-1 lg:grid-cols-[2fr_minmax(800px,1200px)_2fr] grid-rows-[minmax(60px,90px)_12fr_minmax(130px,200px)]">
      <header className="col-start-1 lg:col-start-2 lg:col-end-3 px-8 flex items-center">
        <Navbar />
      </header>
      <main className="col-start-1 lg:col-start-2 lg:col-end-3 bg-primary-foreground">
        <Outlet />
      </main>
      <footer className="col-start-1 lg:col-start-2 lg:col-end-3 bg-secondary-foreground text-background px-8">
        <AppFooter />
      </footer>
    </div>
  );
};
