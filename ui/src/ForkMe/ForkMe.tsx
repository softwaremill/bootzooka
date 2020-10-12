import React from "react";
import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";
import Image from "react-bootstrap/Image";
import forkMeOrange from "./forkme_orange.png";

const ForkMe: React.FC = ({ children }) => (
  <Container fluid>
    <Row className="position-relative">
      {children}
      <a href="https://github.com/softwaremill/bootzooka" target="_blank" rel="noopener noreferrer">
        <Image style={{ position: "absolute", top: 0, right: 0 }} src={forkMeOrange} alt="fork me on github" />
      </a>
    </Row>
  </Container>
);

export default ForkMe;
