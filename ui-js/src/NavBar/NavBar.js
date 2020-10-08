import React from 'react';
import { NavLink } from 'react-router-dom';
import PropTypes from 'prop-types';

const NavBar = ({ isLoggedIn, logout, user }) =>
  <div className="NavBar">
    <NavLink className="NavBar__link" activeClassName="NavBar__link--active" to="/" exact>Bootzooka</NavLink>
    <NavLink className="NavBar__link" activeClassName="NavBar__link--active" to="/main">Home</NavLink>
    { isLoggedIn && user ? <NavLink className="NavBar__link" activeClassName="NavBar__link--active" to="/profile">Logged in as { user.login }</NavLink>
      : <NavLink className="NavBar__link" activeClassName="NavBar__link--active" to="/register">Register</NavLink> }
    { isLoggedIn ? <a className="NavBar__link" onClick={logout}>Logout</a>
      : <NavLink className="NavBar__link" activeClassName="NavBar__link--active" to="/login">Login</NavLink> }
  </div>;

NavBar.propTypes = {
  isLoggedIn: PropTypes.bool.isRequired,
  logout: PropTypes.func.isRequired,
  user: PropTypes.shape({
    login: PropTypes.string.isRequired
  }),
};

export default NavBar;
