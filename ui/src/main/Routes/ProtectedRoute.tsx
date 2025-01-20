import { useContext } from 'react';
import { useLocation, Outlet, Navigate } from 'react-router';
import { UserContext } from 'contexts/UserContext/User.context';

export const ProtectedRoute: React.FC = () => {
  const {
    state: { loggedIn },
  } = useContext(UserContext);
  const location = useLocation();

  return loggedIn ? (
    <Outlet />
  ) : (
    <Navigate to="/login" state={{ from: location }} replace />
  );
};
