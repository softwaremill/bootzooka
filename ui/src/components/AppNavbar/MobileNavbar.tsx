import type { FC } from 'react';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '../ui/dropdown-menu';
import { Button } from '../ui/button';
import { MenuIcon } from 'lucide-react';
import type { NavbarProps } from './types';
import { NavLink } from 'react-router';

export const MobileNavbar: FC<NavbarProps> = ({ items }) => (
  <nav
    data-testid="mobile-navbar"
    className="w-full flex items-center justify-between lg:hidden"
  >
    <NavLink to="/" className="col-start-1 col-end-2 px-2">
      <h1 className="text-3xl font-semibold">Bootzooka</h1>
    </NavLink>
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button size="icon" variant="ghost">
          <MenuIcon className="w-12 h-12" />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent>
        {items.map((item) => (
          <DropdownMenuItem key={item.id} asChild>
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
          </DropdownMenuItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  </nav>
);
