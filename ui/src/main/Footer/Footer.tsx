import React from "react";
import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import Spinner from "react-bootstrap/Spinner";
import { versionService } from "services";
import { useQuery } from "react-query";

interface VersionData {
  buildSha: string;
}

export const Footer: React.FC = () => {
  const query = useQuery<VersionData>("version", versionService.getVersion);

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
            {query.isIdle && <></>}
            {query.isLoading && <Spinner animation="border" size="sm" role="loader" />}
            {query.isError && <></>}
            {query.isSuccess && <small className="text-break">Version: {query.data.buildSha}</small>}
          </Col>
        </Row>
      </Container>
    </Container>
  );
};
