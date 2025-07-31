import { useLocation, Outlet, Navigate } from 'react-router';
import { useUserContext } from 'contexts/UserContext/User.context';

export const ProtectedRoute: React.FC = () => {
  const {
    state: { user },
  } = useUserContext();
  const location = useLocation();

  return user ? (
    <Outlet />
  ) : (
    <Navigate to="/login" state={{ from: location }} replace />
  );
};
