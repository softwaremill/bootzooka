import React from "react";
import Container from "react-bootstrap/Container";
import Spinner from "react-bootstrap/Spinner";

const Loader: React.FC = () => (
  <Container className="d-flex flex-column align-items-center justify-content-center vh-100">
    <Spinner animation="border" />
  </Container>
);

export default Loader;
