import React from "react";
import Navbar from "react-bootstrap/Navbar";
import Nav from "react-bootstrap/Nav";
import Container from "react-bootstrap/Container";
import { LinkContainer } from "react-router-bootstrap";
import { BiPowerOff, BiHappy } from "react-icons/bi";
import { UserContext } from "contexts";

export const Top: React.FC = () => {
  const {
    state: { user, loggedIn },
    dispatch,
  } = React.useContext(UserContext);

  const handleLogOut = () => dispatch({ type: "LOG_OUT" });

  return (
    <Navbar variant="dark" bg="dark" sticky="top" collapseOnSelect expand="lg">
      <Container>
        <LinkContainer to="/">
          <Navbar.Brand>Bootzooka</Navbar.Brand>
        </LinkContainer>
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="d-flex flex-grow-1 justify-content-between">
            <LinkContainer to="/">
              <Nav.Link>Welcome</Nav.Link>
            </LinkContainer>
            <LinkContainer to="/main">
              <Nav.Link>Home</Nav.Link>
            </LinkContainer>
            <div className="flex-grow-1" />
            {loggedIn ? (
              <>
                <LinkContainer to="/profile">
                  <Nav.Link className="text-lg-end">
                    <BiHappy />
                    &nbsp;{user?.login}
                  </Nav.Link>
                </LinkContainer>{" "}
                <Nav.Link className="text-lg-end" onClick={handleLogOut}>
                  <BiPowerOff />
                  &nbsp;Logout
                </Nav.Link>
              </>
            ) : (
              <>
                <LinkContainer to="/register">
                  <Nav.Link className="text-lg-end">Register</Nav.Link>
                </LinkContainer>
                <LinkContainer to="/login">
                  <Nav.Link className="text-lg-end">Login</Nav.Link>
                </LinkContainer>
              </>
            )}
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
};
