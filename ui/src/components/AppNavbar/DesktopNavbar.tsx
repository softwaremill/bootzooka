import type { FC } from 'react';
import { NavbarProps } from './types';
import {
  NavigationMenu,
  NavigationMenuItem,
  NavigationMenuList,
} from '../ui/navigation-menu';
import { NavLink } from 'react-router';
import { Button } from '../ui/button';

export const DesktopNavbar: FC<NavbarProps> = ({ items }) => (
  <NavigationMenu
    data-testid="desktop-navbar"
    className="hidden lg:max-w-full lg:flex gap-4 items-center justify-between"
  >
    <NavigationMenuList>
      <NavigationMenuItem asChild>
        <NavLink to="/" className="col-start-1 col-end-2 px-2">
          <h1 className="text-3xl font-semibold">Bootzooka </h1>
        </NavLink>
      </NavigationMenuItem>
    </NavigationMenuList>
    <NavigationMenuList className="justify-end gap-x-8 mt-2">
      {items.map((item) => (
        <NavigationMenuItem key={item.id} asChild>
          {'href' in item ? (
            <NavLink to={item.href} className="flex items-center gap-2">
              {item.icon && <item.icon className="w-5 h-5" />}
              {item.label}
            </NavLink>
          ) : (
            <Button
              variant="ghost"
              className="text-md font-normal cursor-pointer"
              onClick={item.onClick}
            >
              {item.icon && <item.icon className="w-5 h-5" />}
              <span>{item.label}</span>
            </Button>
          )}
        </NavigationMenuItem>
      ))}
    </NavigationMenuList>
  </NavigationMenu>
);
