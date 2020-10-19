import React from "react";
import Container from "react-bootstrap/Container";

const SecretMain: React.FC = () => (
  <>
    <Container className="my-5 text-center">
      <h3>Shhhh, this is a secret place.</h3>
      <p>You've just logged in. Congrats!</p>
    </Container>
  </>
);

export default SecretMain;
