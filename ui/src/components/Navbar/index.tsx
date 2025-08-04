import { useUserContext } from 'contexts/UserContext/User.context';
import type { FC } from 'react';
import {
  NavigationMenu,
  NavigationMenuItem,
  NavigationMenuList,
} from '../ui/navigation-menu';
import { useApiKeyState } from '@/hooks/auth';
import { NavLink } from 'react-router';
import { Button } from '../ui/button';
import { usePostUserLogout } from '@/api/apiComponents';
import { useQueryClient } from '@tanstack/react-query';

export const Navbar: FC = () => {
  const {
    state: { user },
    dispatch,
  } = useUserContext();
  const [apiKeyState, setApiKeyState] = useApiKeyState();
  const client = useQueryClient();

  const apiKey = apiKeyState?.apiKey;

  const { mutateAsync: logout } = usePostUserLogout({
    onSuccess: () => {
      setApiKeyState(null);
      client.clear();
      dispatch({ type: 'LOG_OUT' });
    },
  });

  return (
    <NavigationMenu className="w-full items-center grid grid-cols-[auto_1fr_8fr] gap-4">
      <NavigationMenuList className="col-start-1 col-end-2">
        <NavigationMenuItem asChild>
          <NavLink to="/" className="col-start-1 col-end-2 px-2">
            <h1 className="text-4xl">Bootzooka</h1>
          </NavLink>
        </NavigationMenuItem>
      </NavigationMenuList>
      <NavigationMenuList className="col-start-2 col-end-3 gap-x-4 mt-2">
        <NavigationMenuItem asChild>
          <NavLink to="/">Welcome</NavLink>
        </NavigationMenuItem>
        <NavigationMenuItem asChild>
          <NavLink to="/main">Home</NavLink>
        </NavigationMenuItem>
      </NavigationMenuList>
      <NavigationMenuList className="col-start-3 col-end-4 flex justify-end gap-x-4 mt-2">
        {user && apiKey ? (
          <>
            <NavigationMenuItem asChild>
              <NavLink to="/profile">{user.login}</NavLink>
            </NavigationMenuItem>
            <NavigationMenuItem>
              <Button
                size="sm"
                variant="ghost"
                className="text-md font-normal cursor-pointer"
                onClick={() => logout({ body: { apiKey } })}
              >
                Logout
              </Button>
            </NavigationMenuItem>
          </>
        ) : (
          <>
            <NavigationMenuItem asChild>
              <NavLink to="/register">Register</NavLink>
            </NavigationMenuItem>
            <NavigationMenuItem asChild>
              <NavLink to="/login">Login</NavLink>
            </NavigationMenuItem>
          </>
        )}
      </NavigationMenuList>
    </NavigationMenu>
  );
};
