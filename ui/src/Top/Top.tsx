import React from "react";
import Navbar from "react-bootstrap/Navbar";
import Nav from "react-bootstrap/Nav";
import Button from "react-bootstrap/Button";
import { LinkContainer } from "react-router-bootstrap";
import { AppContext } from "../AppContext/AppContext";

const Top: React.FC = () => {
  const {
    dispatch,
    state: { user, loggedIn },
  } = React.useContext(AppContext);

  const handleLogOut = () => {
    dispatch({
      type: "SET_API_KEY",
      apiKey: null,
    });
    dispatch({
      type: "SET_USER_DATA",
      user: null,
    });
    dispatch({
      type: "SET_LOGGED_IN",
      loggedIn: false,
    });
    localStorage.removeItem("apiKey");
  };

  return (
    <Navbar variant="dark" bg="secondary" sticky="top" className="justify-content-between">
      <Nav>
        <LinkContainer exact to="/">
          <Nav.Link>Bootzooka</Nav.Link>
        </LinkContainer>
        <LinkContainer to="/main">
          <Nav.Link>Home</Nav.Link>
        </LinkContainer>
      </Nav>
      <Nav>
        {loggedIn && user ? (
          <>
            <LinkContainer to="/profile">
              <Nav.Link>Logged in as {user.login}</Nav.Link>
            </LinkContainer>
            <Button onClick={handleLogOut}>Logout</Button>
          </>
        ) : (
          <>
            <LinkContainer to="/register">
              <Nav.Link>Register</Nav.Link>
            </LinkContainer>
            <LinkContainer to="/login">
              <Nav.Link>Login</Nav.Link>
            </LinkContainer>
          </>
        )}
      </Nav>
    </Navbar>
  );
};

export default Top;
