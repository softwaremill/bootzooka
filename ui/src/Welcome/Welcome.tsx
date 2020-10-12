import React from "react";
import logo from "./sml-logo-vertical-rgb-trans.png";
import Image from "react-bootstrap/Image";
import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";

const Welcome: React.FC = () => (
  <>
    <Container fluid className="py-5 bg-dark text-light text-center">
      <Row>
        <Container>
          <h3>Hi there!</h3>
          <h1>Welcome to Bootzooka!</h1>
          <p>In this template application you can register as a new user, log in and later manage your user details.</p>
          <p>
            If you are interested in how Bootzooka works, you can{" "}
            <a href="http://softwaremill.github.io/bootzooka/" target="blank">
              browse the documentation
            </a>
            , or the{" "}
            <a href="https://github.com/softwaremill/bootzooka" target="blank">
              source code
            </a>
            .
          </p>
        </Container>
      </Row>
    </Container>
    <Container className="py-5 text-center">
      <p>brought to you by</p>
      <a href="http://softwaremill.com" rel="noopener noreferrer" target="_blank">
        <Image fluid style={{ maxWidth: "20em" }} src={logo} alt="SoftwareMill" />
      </a>
    </Container>
  </>
);

export default Welcome;
