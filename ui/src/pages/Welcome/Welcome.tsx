import React from "react";
import logo from "./sml-logo-vertical-rgb-trans.png";
import Image from "react-bootstrap/Image";
import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";
import { Link } from "react-router-dom";

const Welcome: React.FC = () => (
  <>
    <Container fluid className="py-5 bg-primary text-light text-center">
      <Row>
        <Container>
          <h3>Hi there!</h3>
          <h1>Welcome to Bootzooka!</h1>
          <p>
            In this template application you can{" "}
            <Link to="/register" className="btn btn-outline-light">
              Register
            </Link>{" "}
            as a new user,
            <br />
            <Link to="/login" className="btn btn-outline-light">
              Login
            </Link>{" "}
            and later manage your user details.
          </p>
        </Container>
      </Row>
    </Container>
    <Container className="py-5 text-center">
      <p>brought to you by</p>
      <a href="http://softwaremill.com" rel="noopener noreferrer" target="_blank">
        <Image fluid src={logo} alt="SoftwareMill" width="300" />
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
        </a>{" "}
        .
      </p>
    </Container>
  </>
);

export default Welcome;
