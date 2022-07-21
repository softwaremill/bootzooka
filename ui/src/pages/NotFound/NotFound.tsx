import React from "react";
import { Link } from "react-router-dom";
import Container from "react-bootstrap/Container";

export const NotFound: React.FC = () => (
  <Container className="py-5 text-center">
    <h1>Ooops!</h1>
    <h3>You shouldn't be here for sure :)</h3>
    <div>Please choose one of the locations below:</div>
    <ul className="list-group">
      <li>
        <Link to="/">Home page</Link>
      </li>
      <li>
        <a href="http://softwaremill.com">SoftwareMill - our company Home Page</a>
      </li>
      <li>
        <a href="https://github.com/softwaremill/bootzooka/">Bootzooka on GitHub</a>
      </li>
    </ul>
  </Container>
);
