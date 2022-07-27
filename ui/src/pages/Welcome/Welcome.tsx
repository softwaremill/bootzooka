import React from "react";
import { Link } from "react-router-dom";
import Image from "react-bootstrap/Image";
import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";
import Fade from "react-bootstrap/Fade";
import logo from "assets/sml-logo-vertical-white-all-trans.png";

export const Welcome: React.FC = () => (
  <>
    <Container fluid className="py-5 bg-light text-dark">
      <Row className="h-100">
        <Fade appear in>
          <Container className="d-flex flex-column justify-content-center align-items-center">
            <h3>Hi there!</h3>
            <h1>Welcome to Bootzooka!</h1>
            <p className="mt-3 px-4">
              In this template application you can{" "}
              <Link to="/register" className="link-dark">
                Register
              </Link>{" "}
              as a new user,{" "}
              <Link to="/login" className="link-dark">
                Login
              </Link>{" "}
              and later manage your user details.
            </p>
          </Container>
        </Fade>
      </Row>
    </Container>
    <Container className="py-5 bg-dark text-light text-center">
      <Row className="h-100">
        <Fade appear in>
          <Container className="d-flex flex-column justify-content-start align-items-center">
            <p className="fs-3">Brought to you by</p>
            <a href="http://softwaremill.com" rel="noopener noreferrer" target="_blank">
              <Image fluid src={logo} alt="SoftwareMill" width="500" />
            </a>
            <p>
              If you are interested in how Bootzooka works,
              <br />
              you can browse the{" "}
              <a href="http://softwaremill.github.io/bootzooka/" target="blank">
                Documentation
              </a>{" "}
              or{" "}
              <a href="https://github.com/softwaremill/bootzooka" target="blank">
                Source code
              </a>
              .
            </p>
          </Container>
        </Fade>
      </Row>
    </Container>
  </>
);
