import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import Spinner from "react-bootstrap/Spinner";
import { useGetAdminVersion } from "api/apiComponents";

export const Footer = () => {
  const mutation = useGetAdminVersion({});

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
            {mutation.isPending && <></>}
            {mutation.isLoading && <Spinner animation="border" size="sm" role="loader" />}
            {mutation.isError && <></>}
            {mutation.isSuccess && <small className="text-break">Version: {mutation.data.buildSha}</small>}
          </Col>
        </Row>
      </Container>
    </Container>
  );
};
