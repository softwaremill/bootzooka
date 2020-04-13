import React from 'react';
import { Route, Redirect } from 'react-router-dom';

const ProtectedRoute: React.FC = ({ isLoggedIn, ...rest }: any) =>
  isLoggedIn ? <Route {...rest} /> : <Redirect to="/login" />;

export default ProtectedRoute;
