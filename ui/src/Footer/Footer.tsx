import React from "react";
import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import versionService from "../VersionService/VersionService";
import Spinner from "react-bootstrap/Spinner";
import { usePromise } from "react-use-promise-matcher";
import { BsExclamationCircle } from "react-icons/bs";

interface VersionData {
  buildDate: string;
  buildSha: string;
}

const Footer: React.FC = () => {
  const [result, load] = usePromise<VersionData, any, any>(versionService.getVersion);

  React.useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <Container fluid className="fixed-bottom bg-light">
      <Row>
        <Col sm={6} className="py-3">
          <small>
            Bootzooka - application scaffolding by <a href="http://softwaremill.com">SoftwareMill</a>,
            <br /> sources available on <a href="https://github.com/softwaremill/bootzooka/">GitHub</a>
          </small>
        </Col>
        <Col sm={6} className="text-right py-3 d-none d-sm-block">
          {result.match({
            Idle: () => <></>,
            Loading: () => <Spinner animation="border" size="sm" role="loader" />,
            Rejected: (error) => (
              <small className="text-danger">
                <BsExclamationCircle className="mr-2" />
                {error?.response?.data || error?.request || error.message}
              </small>
            ),
            Resolved: ({ buildDate, buildSha }) => (
              <small className="text-break">
                <strong>build&nbsp;date:&nbsp;</strong>
                {buildDate}
                <br />
                <strong>build&nbsp;sha:&nbsp;</strong>
                {buildSha}
              </small>
            ),
          })}
        </Col>
      </Row>
    </Container>
  );
};

export default Footer;
