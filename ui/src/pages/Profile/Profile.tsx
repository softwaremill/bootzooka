import React from "react";
import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";
import { ProfileDetails } from "./components/ProfileDetails";
import { PasswordDetails } from "./components/PasswordDetails";
import { changePassword, changeProfileDetails } from "services";

export const Profile: React.FC = () => (
  <Container>
    <Row>
      <ProfileDetails onChangeProfileDetails={changeProfileDetails} />
      <PasswordDetails onChangePassword={changePassword} />
    </Row>
  </Container>
);
