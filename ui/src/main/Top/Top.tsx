import React from "react";
import Navbar from "react-bootstrap/Navbar";
import Nav from "react-bootstrap/Nav";
import Container from "react-bootstrap/Container";
import { LinkContainer } from "react-router-bootstrap";
import { UserContext } from "../../contexts/UserContext/UserContext";
import { BiHappy, BiPowerOff } from "react-icons/bi";
import { pipe } from "fp-ts/pipeable";

const Top: React.FC = () => {
  const {
    user,
    dispatch
  } = React.useContext(UserContext);

  const handleLogOut = () => dispatch({ type: "LOG_OUT" });

  return (
    <Navbar variant="dark" bg="dark" sticky="top" collapseOnSelect expand="lg">
      <Container>
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
            {pipe(
              user,
              () => (
                <>
                  <LinkContainer to="/register">
                    <Nav.Link className="text-right">Register</Nav.Link>
                  </LinkContainer>
                  <LinkContainer to="/login">
                    <Nav.Link className="text-right">Login</Nav.Link>
                  </LinkContainer>
                </>),
              u => (
                <>
                  <LinkContainer to="/profile">
                    <Nav.Link className="text-right">
                      <BiHappy />
                      &nbsp;{}
                    </Nav.Link>
                  </LinkContainer>{" "}
                  <Nav.Link className="text-right" onClick={handleLogOut}>
                    <BiPowerOff />
                    &nbsp;Logout
                  </Nav.Link>
                </>))
            }
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
};

export default Top;
