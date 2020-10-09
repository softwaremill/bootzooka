import React from "react";
import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import versionService from "../VersionService/VersionService";

const Footer: React.FC = () => {
  const [version, setVersion] = React.useState("");

  React.useEffect(() => {
    const fetchVersion = async () => {
      try {
        const { buildDate, buildSha } = await versionService.getVersion();
        setVersion(`${buildDate}, ${buildSha}`);
      } catch (error) {
        console.error(error);
      }
    };
    fetchVersion();
  }, [setVersion]);

  return (
    <Container fluid className="fixed-bottom bg-light">
      <Row>
        <Col sm={6}>
          <p>
            <small>
              Bootzooka - application scaffolding by <a href="http://softwaremill.com">SoftwareMill</a>, sources
              available on <a href="https://github.com/softwaremill/bootzooka/">GitHub</a>
            </small>
          </p>
        </Col>

        <Col sm={6}>
          <p>
            <small>{version}</small>
          </p>
        </Col>
      </Row>
    </Container>
  );
};

export default Footer;
