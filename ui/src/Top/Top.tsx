import React from "react";
import Navbar from "react-bootstrap/Navbar";
import Nav from "react-bootstrap/Nav";
import Button from "react-bootstrap/Button";
import { LinkContainer } from "react-router-bootstrap";
import { AppContext } from "../AppContext/AppContext";
import { BiPowerOff } from "react-icons/bi";

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
      type: "SET_LOGGED_IN",
      loggedIn: false,
    });
    localStorage.removeItem("apiKey");
  };

  return (
    <Navbar variant="dark" bg="dark" sticky="top" collapseOnSelect expand="lg">
      <LinkContainer exact to="/">
        <Navbar.Brand>Bootzooka</Navbar.Brand>
      </LinkContainer>
      <Navbar.Toggle aria-controls="basic-navbar-nav" />
      <Navbar.Collapse id="basic-navbar-nav">
        <Nav className="d-flex flex-grow-1 justify-content-between">
          <LinkContainer exact to="/">
            <Nav.Link>Welcome</Nav.Link>
          </LinkContainer>
          <LinkContainer to="/main">
            <Nav.Link>Home</Nav.Link>
          </LinkContainer>
          <div className="flex-grow-1" />
          {loggedIn ? (
            <>
              <Navbar.Text>Logged in as {user.login}</Navbar.Text>
              <LinkContainer to="/profile">
                <Nav.Link>Profile</Nav.Link>
              </LinkContainer>
              <Button onClick={handleLogOut}>
                <BiPowerOff />
                &nbsp;Logout
              </Button>
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
      </Navbar.Collapse>
    </Navbar>
  );
};

export default Top;
