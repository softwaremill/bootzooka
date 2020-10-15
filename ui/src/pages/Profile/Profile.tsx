import React from "react";
import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";
import ProfileDetails from "./ProfileDetails";
import PasswordDetails from "./PasswordDetails";

const Profile: React.FC = () => (
  <Container>
    <Row>
      <ProfileDetails />
      <PasswordDetails />
    </Row>
  </Container>
);

export default Profile;
