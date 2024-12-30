import React, { useEffect } from "react";
import Navbar from "react-bootstrap/Navbar";
import Nav from "react-bootstrap/Nav";
import Container from "react-bootstrap/Container";
import { Link } from "react-router-dom";
import { BiPowerOff, BiHappy } from "react-icons/bi";
import { UserContext } from "contexts";
import { usePostUserLogout } from "api/apiComponents";

type Props = {};

export const Top: React.FC<Props> = ({}) => {
  const {
    state: { user, loggedIn, apiKey },
    dispatch,
  } = React.useContext(UserContext);

  const { mutateAsync: logout, isSuccess } = usePostUserLogout();

  useEffect(() => {
    if (isSuccess) {
      dispatch({ type: "LOG_OUT" });
    }
  }, [isSuccess, dispatch]);

  return (
    <Navbar variant="dark" bg="dark" sticky="top" collapseOnSelect expand="lg">
      <Container>
        <Navbar.Brand as={Link} to="/">
          Bootzooka
        </Navbar.Brand>
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="d-flex flex-grow-1 justify-content-between">
            <Nav.Link as={Link} to="/">
              Welcome
            </Nav.Link>
            <Nav.Link as={Link} to="/main">
              Home
            </Nav.Link>
            <div className="flex-grow-1" />
            {loggedIn && apiKey !== null ? (
              <>
                <Nav.Link as={Link} to="/profile" className="text-lg-end">
                  <BiHappy />
                  &nbsp;{user?.login}
                </Nav.Link>{" "}
                <Nav.Link className="text-lg-end" onClick={() => logout({ body: { apiKey } })}>
                  <BiPowerOff />
                  &nbsp;Logout
                </Nav.Link>
              </>
            ) : (
              <>
                <Nav.Link as={Link} to="/register" className="text-lg-end">
                  Register
                </Nav.Link>
                <Nav.Link as={Link} to="/login" className="text-lg-end">
                  Login
                </Nav.Link>
              </>
            )}
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
};
