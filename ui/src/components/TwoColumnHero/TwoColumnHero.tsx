import React from "react";

import Container from "react-bootstrap/Container";
import Col from "react-bootstrap/Col";
import Row from "react-bootstrap/Row";
import Fade from "react-bootstrap/Fade";
import Image from "react-bootstrap/Image";
import logo from "assets/sml-logo-vertical-white-all-trans.png";

interface TwoColumnHeroProps {
  children: React.ReactNode;
}

export const TwoColumnHero: React.FC<TwoColumnHeroProps> = ({ children }) => {
  return (
    <Container className="h-100">
      <Row className="h-100">
        <Col xs={12} xl={6} className="bg-dark d-flex justify-content-center align-items-center">
          <Image src={logo} fluid alt="SoftwareMill logotype" />
        </Col>
        <Col xs={12} xl={6}>
          <Fade className="h-100" appear in>
            <Container className="bg-light d-flex flex-column justify-content-center align-items-center px-5">
              {children}
            </Container>
          </Fade>
        </Col>
      </Row>
    </Container>
  );
};
