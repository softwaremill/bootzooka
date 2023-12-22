import React from "react";
import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";
import Image from "react-bootstrap/Image";
import forkMeOrange from "assets/forkme_orange.png";

interface ForkMeProps {
  children?: React.ReactNode;
}

export const ForkMe: React.FC<ForkMeProps> = ({ children }) => (
  <Container className="bg-light" style={{ height: "calc(100% - 56px)" }} fluid>
    <Row className="position-relative h-100">
      {children}
      <a
        style={{ position: "absolute", top: 0, right: 0, width: "unset", padding: 0 }}
        href="https://github.com/softwaremill/bootzooka"
        target="_blank"
        rel="noopener noreferrer"
      >
        <Image src={forkMeOrange} alt="fork me on github" />
      </a>
    </Row>
  </Container>
);
