import type { FC } from 'react';
import { NavLink, Outlet } from 'react-router';

export const AppLayout: FC = () => (
  <div className="w-full h-screen max-h-full grid grid-cols-1 lg:grid-cols-[2fr_minmax(800px,1200px)_2fr] grid-rows-[minmax(60px,_1fr)_12fr_minmax(60px,_1fr)]">
    <header className="col-start-1 lg:col-start-2 lg:col-end-3 bg-secondary-foreground text-background flex items-center justify-between px-8">
      <NavLink to="/">
        <h1 className="text-4xl">Bootzooka</h1>
      </NavLink>
    </header>
    <main className="col-start-1 lg:col-start-2 lg:col-end-3 bg-primary-foreground">
      <Outlet />
    </main>
    <footer className="col-start-1 lg:col-start-2 lg:col-end-3 bg-secondary-foreground text-background flex items-center justify-between px-8">
      Footer
    </footer>
  </div>
);
