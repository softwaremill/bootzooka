import React from "react";
import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import versionService from "../../services/VersionService/VersionService";
import Spinner from "react-bootstrap/Spinner";
import { usePromise } from "react-use-promise-matcher";
import { BsExclamationCircle } from "react-icons/bs";
import ErrorMessage from "../../parts/ErrorMessage/ErrorMessage";

interface VersionData {
  buildDate: string;
  buildSha: string;
}

const Footer: React.FC = () => {
  const [result, load] = usePromise<VersionData, any, any>(() => versionService.getVersion());

  React.useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <Container fluid className="fixed-bottom bg-light text-muted d-none d-sm-block">
      <Row>
        <Container>
          <Row>
            <Col sm={6} className="py-3">
              <small>
                Bootzooka - application scaffolding by <a href="http://softwaremill.com">SoftwareMill</a>,
                <br /> sources available on <a href="https://github.com/softwaremill/bootzooka/">GitHub</a>
              </small>
            </Col>
            <Col sm={6} className="text-right py-3">
              {result.match({
                Idle: () => <></>,
                Loading: () => <Spinner animation="border" size="sm" role="loader" />,
                Rejected: (error) => (
                  <small>
                    <BsExclamationCircle className="text-danger mr-2" />
                    <ErrorMessage error={error} />
                  </small>
                ),
                Resolved: ({ buildDate, buildSha }) => (
                  <small className="text-break">
                    {buildDate}
                    <br />
                    {buildSha}
                  </small>
                ),
              })}
            </Col>
          </Row>
        </Container>
      </Row>
    </Container>
  );
};

export default Footer;
