import React from 'react';
import { Route, Redirect } from 'react-router-dom';

type Props = {
  isLoggedIn: boolean;
  [key: string]: any
}

const ProtectedRoute: React.FC<Props> = ({ isLoggedIn, ...rest }) =>
  isLoggedIn ? <Route {...rest} /> : <Redirect to="/login" />;

export default ProtectedRoute;
