import React from "react";
import Navbar from "react-bootstrap/Navbar";
import Nav from "react-bootstrap/Nav";
import Container from "react-bootstrap/Container";
import { Link } from "react-router-dom";
import { UserContext } from "../../contexts/UserContext/UserContext";
import { BiPowerOff, BiHappy } from "react-icons/bi";

const Top: React.FC = () => {
  const {
    state: { user, loggedIn },
    dispatch,
  } = React.useContext(UserContext);

  const handleLogOut = () => dispatch({ type: "LOG_OUT" });

  return (
    <Navbar variant="dark" bg="dark" sticky="top" collapseOnSelect expand="lg">
      <Container>
        <Link to="/">
          <Navbar.Brand>Bootzooka</Navbar.Brand>
        </Link>
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="d-flex flex-grow-1 justify-content-between">
            <Link to="/">
              <Nav.Link as="span">Welcome</Nav.Link>
            </Link>
            <Link to="/main">
              <Nav.Link as="span">Home</Nav.Link>
            </Link>
            <div className="flex-grow-1" />
            {loggedIn ? (
              <>
                <Link to="/profile">
                  <Nav.Link className="text-right" as="span">
                    <BiHappy />
                    &nbsp;{user?.login}
                  </Nav.Link>
                </Link>{" "}
                <Nav.Link className="text-right" as="span" onClick={handleLogOut}>
                  <BiPowerOff />
                  &nbsp;Logout
                </Nav.Link>
              </>
            ) : (
              <>
                <Link to="/register">
                  <Nav.Link className="text-right" as="span">
                    Register
                  </Nav.Link>
                </Link>
                <Link to="/login">
                  <Nav.Link className="text-right" as="span">
                    Login
                  </Nav.Link>
                </Link>
              </>
            )}
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
};

export default Top;
