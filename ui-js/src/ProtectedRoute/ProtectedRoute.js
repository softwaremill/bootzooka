import React from 'react';
import { Route, Redirect } from 'react-router-dom';

const ProtectedRoute = ({ isLoggedIn, ...rest }) =>
  isLoggedIn ? <Route {...rest} /> : <Redirect to="/login" />;

export default ProtectedRoute;
