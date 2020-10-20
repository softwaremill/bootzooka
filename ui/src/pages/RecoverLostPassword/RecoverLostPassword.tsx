import React from "react";
import { Formik, Form as FormikForm } from "formik";
import * as Yup from "yup";
import passwordService from "../../services/PasswordService/PasswordService";
import Form from "react-bootstrap/Form";
import Container from "react-bootstrap/Container";
import Col from "react-bootstrap/Col";
import Row from "react-bootstrap/Row";
import { BiReset } from "react-icons/bi";
import { usePromise } from "react-use-promise-matcher";
import FormikInput from "../../parts/FormikInput/FormikInput";
import FeedbackButton from "../../parts/FeedbackButton/FeedbackButton";

interface RecoverLostPasswordParams {
  loginOrEmail: string;
}

const validationSchema: Yup.ObjectSchema<RecoverLostPasswordParams | undefined> = Yup.object({
  loginOrEmail: Yup.string().required("Required"),
});

const RecoverLostPassword: React.FC = () => {
  const [result, send, clear] = usePromise((values: RecoverLostPasswordParams) =>
    passwordService.claimPasswordReset(values)
  );

  return (
    <Container className="py-5">
      <Row>
        <Col md={9} lg={7} xl={6} className="mx-auto">
          <h3>Recover lost password</h3>
          <Formik<RecoverLostPasswordParams>
            initialValues={{
              loginOrEmail: "",
            }}
            onSubmit={send}
            validationSchema={validationSchema}
          >
            <Form as={FormikForm}>
              <FormikInput name="loginOrEmail" label="Login or email" />
              <FeedbackButton
                type="submit"
                label="Reset password"
                Icon={BiReset}
                result={result}
                clear={clear}
                successLabel="Password reset claim success"
              />
            </Form>
          </Formik>
        </Col>
      </Row>
    </Container>
  );
};

export default RecoverLostPassword;
