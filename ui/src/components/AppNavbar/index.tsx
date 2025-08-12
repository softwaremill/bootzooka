import { useUserContext } from '@/contexts/UserContext/User.context';
import { useMemo, type FC } from 'react';
import { useApiKeyState } from '@/hooks/auth';
import { usePostUserLogout } from '@/api/apiComponents';
import { useQueryClient } from '@tanstack/react-query';
import {
  GitFork,
  LogInIcon,
  LogOutIcon,
  UserIcon,
  UserRoundPlusIcon,
} from 'lucide-react';
import { NavbarMenuItem } from './types';
import { DesktopNavbar } from './DesktopNavbar';
import { MobileNavbar } from './MobileNavbar';

const COMMON_NAVBAR_ITEMS: NavbarMenuItem[] = [
  {
    id: 'welcome',
    label: 'Welcome',
    href: '/',
  },
  {
    id: 'home',
    label: 'Home',
    href: '/main',
  },
  {
    id: 'fork',
    label: 'Fork',
    href: 'https://github.com/softwaremill/bootzooka',
    icon: GitFork,
  },
];

export const AppNavbar: FC = () => {
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

  const navbarItems: NavbarMenuItem[] = useMemo(
    () =>
      user && apiKey
        ? [
            ...COMMON_NAVBAR_ITEMS,
            {
              id: 'profile',
              label: user.login,
              href: '/profile',
              icon: UserIcon,
            },
            {
              id: 'logout',
              label: 'Logout',
              onClick: () => logout({ body: { apiKey } }),
              icon: LogOutIcon,
            },
          ]
        : [
            ...COMMON_NAVBAR_ITEMS,
            {
              id: 'register',
              label: 'Register',
              href: '/register',
              icon: UserRoundPlusIcon,
            },
            {
              id: 'login',
              label: 'Login',
              href: '/login',
              icon: LogInIcon,
            },
          ],
    [user, apiKey, logout]
  );

  return (
    <>
      <DesktopNavbar items={navbarItems} />
      <MobileNavbar items={navbarItems} />
    </>
  );
};
