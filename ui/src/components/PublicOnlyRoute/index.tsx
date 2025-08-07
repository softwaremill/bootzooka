import { useUserContext } from 'contexts/UserContext/User.context';
import { type FC, useEffect } from 'react';
import { Outlet, useNavigate } from 'react-router';

export const PublicOnlyRoute: FC = () => {
  const {
    state: { user },
  } = useUserContext();

  const navigate = useNavigate();

  useEffect(() => {
    if (user) {
      navigate('/main');
    }
  }, [user, navigate]);

  return user ? null : <Outlet />;
};
