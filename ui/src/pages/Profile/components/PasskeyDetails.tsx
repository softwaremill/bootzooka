import React from "react";
import Form from "react-bootstrap/Form";
import Container from "react-bootstrap/Container";
import Col from "react-bootstrap/Col";
import Row from "react-bootstrap/Row";
import { BiArrowFromBottom } from "react-icons/bi";
import { usePromise } from "react-use-promise-matcher";
import { Formik, Form as FormikForm } from "formik";
import * as Yup from "yup";
import { UserContext } from "contexts";
import { userService } from "services";
import { FormikInput, FeedbackButton } from "components";

const validationSchema = Yup.object({
  login: Yup.string().min(3, "At least 3 characters required").required("Required"),
  email: Yup.string().email("Correct email address required").required("Required"),
});

type ProfileDetailsParams = Yup.InferType<typeof validationSchema>;

export const PasskeyDetails: React.FC = () => {
  const {
    dispatch,
    state: { apiKey, user },
  } = React.useContext(UserContext);

  const [result, send, clear] = usePromise((values: ProfileDetailsParams) =>
    userService.registerPasskey(apiKey, {}).then(() => dispatch({ type: "PASSKEY_REGISTERED" }))
  );

  return (
    <Container className="py-5">
      <Row>
        <Col md={9} lg={7} xl={6} className="mx-auto">
          <h3 className="mb-4">Passkey details</h3>
          <Formik<ProfileDetailsParams>
            initialValues={{
              login: user?.login || "",
              email: user?.email || "",
            }}
            onSubmit={send}
            validationSchema={validationSchema}
          >
            <Form as={FormikForm}>
              <FeedbackButton
                className="float-end"
                type="submit"
                label="Register passkey"
                variant="dark"
                Icon={BiArrowFromBottom}
                result={result}
                clear={clear}
                successLabel="Passkey Registered"
              />
            </Form>
          </Formik>
        </Col>
      </Row>
    </Container>
  );
};
