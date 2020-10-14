import React from "react";
import logo from "./sml-logo-vertical-rgb-trans.png";
import Image from "react-bootstrap/Image";
import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";

const Welcome: React.FC = () => (
  <>
    <Container fluid className="py-5 bg-primary text-light text-center">
      <Row>
        <Container>
          <h3>Hi there!</h3>
          <h1>Welcome to Bootzooka!</h1>
          <p>In this template application you can register as a new user, log in and later manage your user details.</p>
          <p>
            If you are interested in how Bootzooka works, you can browse the{" "}
            <a href="http://softwaremill.github.io/bootzooka/" className="btn btn-outline-light" target="blank">
              Documentation
            </a>{" "}
            or{" "}
            <a href="https://github.com/softwaremill/bootzooka" className="btn btn-outline-light" target="blank">
              Source code
            </a>{" "}
            .
          </p>
        </Container>
      </Row>
    </Container>
    <Container className="py-5 text-center">
      <p>brought to you by</p>
      <a href="http://softwaremill.com" rel="noopener noreferrer" target="_blank">
        <Image fluid src={logo} alt="SoftwareMill" width="300" />
      </a>
    </Container>
  </>
);

export default Welcome;
