import React from "react";
import Navbar from "react-bootstrap/Navbar";
import Nav from "react-bootstrap/Nav";
import Button from "react-bootstrap/Button";
import { LinkContainer } from "react-router-bootstrap";
import "./Top.scss";

interface NavBarProps {
  isLoggedIn: boolean;
  logout: () => void;
  user: { login: string };
}

const Top: React.FC<NavBarProps> = ({ isLoggedIn, logout, user }) => (
  <Navbar variant="dark" bg="dark" sticky="top" className="justify-content-between">
    <Nav >
      <LinkContainer exact to="/">
        <Nav.Link>Bootzooka</Nav.Link>
      </LinkContainer>
      <LinkContainer exact to="/main">
        <Nav.Link>Home</Nav.Link>
      </LinkContainer>
    </Nav>
    <Nav>
      {isLoggedIn && user ? (
        <LinkContainer to="/profile">
          <Nav.Link>Logged in as {user.login}</Nav.Link>
        </LinkContainer>
      ) : (
        <LinkContainer to="/register">
          <Nav.Link>Register</Nav.Link>
        </LinkContainer>
      )}
      {isLoggedIn ? (
        <Button onClick={logout}>Logout</Button>
      ) : (
        <LinkContainer to="/login">
          <Nav.Link>Login</Nav.Link>
        </LinkContainer>
      )}
    </Nav>
  </Navbar>
);

export default Top;
