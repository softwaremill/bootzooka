import React from "react";
import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import Spinner from "react-bootstrap/Spinner";
import { usePromise } from "react-use-promise-matcher";
import { versionService } from "services";

interface VersionData {
  buildSha: string;
}

export const Footer: React.FC = () => {
  const [result, load] = usePromise<VersionData, any, any>(() => versionService.getVersion());

  React.useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <Container fluid className="fixed-bottom bg-dark text-light d-none d-sm-block">
      <Container>
        <Row>
          <Col sm={6} className="py-4">
            <small>
              Bootzooka - application scaffolding by <a href="http://softwaremill.com">SoftwareMill</a>, sources
              available on <a href="https://github.com/softwaremill/bootzooka/">GitHub</a>
            </small>
          </Col>
          <Col sm={6} className="text-end py-4">
            {result.match({
              Idle: () => <></>,
              Loading: () => <Spinner animation="border" size="sm" role="loader" />,
              Rejected: () => <></>,
              Resolved: ({ buildSha }) => <small className="text-break">Build SHA: {buildSha}</small>,
            })}
          </Col>
        </Row>
      </Container>
    </Container>
  );
};
