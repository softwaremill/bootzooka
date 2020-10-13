import React from "react";
import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import versionService from "../VersionService/VersionService";
import Notifications from "../Notifications/Notifications";
import Spinner from "react-bootstrap/Spinner";

interface VersionData {
  buildDate: string;
  buildSha: string;
}

const Footer: React.FC = () => {
  const [version, setVersion] = React.useState<VersionData>();
  const [isLoader, setLoader] = React.useState(false);

  React.useEffect(() => {
    const fetchVersion = async () => {
      setLoader(true);
      try {
        const data = await versionService.getVersion();
        setVersion(data);
      } catch (error) {
        console.error(error);
      } finally {
        setLoader(false);
      }
    };
    fetchVersion();
  }, [setVersion]);

  return (
    <Container fluid className="fixed-bottom">
      <Row>
        <Notifications />
      </Row>
      <Row className="bg-light">
        <Col sm={6} className="py-3">
          <small>
            Bootzooka - application scaffolding by <a href="http://softwaremill.com">SoftwareMill</a>,
            <br /> sources available on <a href="https://github.com/softwaremill/bootzooka/">GitHub</a>
          </small>
        </Col>
        <Col sm={6} className="text-right py-3 d-none d-sm-block">
          <small className="text-break">
            <strong>build&nbsp;date:&nbsp;</strong>
            {isLoader ? <Spinner animation="border" size="sm" role="loader" /> : version?.buildDate}
            <br />
            <strong>build&nbsp;sha:&nbsp;</strong>
            {isLoader ? <Spinner animation="border" size="sm" role="loader" /> : version?.buildSha}
          </small>
        </Col>
      </Row>
    </Container>
  );
};

export default Footer;
