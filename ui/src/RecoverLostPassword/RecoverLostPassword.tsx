import React from "react";
import { Formik, Form as FormikForm } from "formik";
import * as Yup from "yup";
import passwordService from "../PasswordService/PasswordService";
import Form from "react-bootstrap/Form";
import Button from "react-bootstrap/Button";
import Container from "react-bootstrap/Container";
import Spinner from "react-bootstrap/Spinner";
import { BiReset } from "react-icons/bi";
import { BsExclamationCircle, BsCheck } from "react-icons/bs";
import { usePromise } from "react-use-promise-matcher";
import FormikInput from "../FormikInput/FormikInput";

interface RecoverLostPasswordParams {
  loginOrEmail: string;
}

const RecoverLostPassword: React.FC = () => {
  const validationSchema: Yup.ObjectSchema<RecoverLostPasswordParams | undefined> = Yup.object({
    loginOrEmail: Yup.string().required("Required"),
  });

  const [result, send] = usePromise((values: RecoverLostPasswordParams) =>
    passwordService.claimPasswordReset(values).catch((error) => {
      throw new Error(error?.response?.data?.error || error.message);
    })
  );

  return (
    <Container className="py-5">
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

          <Button type="submit">
            <BiReset />
            &nbsp;Reset password
          </Button>
          {result.match({
            Idle: () => <></>,
            Loading: () => (
              <Form.Text muted>
                <Spinner as="span" className="mr-2" animation="border" size="sm" role="loader" />
                Connecting
              </Form.Text>
            ),
            Rejected: (error) => (
              <Form.Text className="text-danger">
                <BsExclamationCircle className="mr-2" />
                {error.toString()}
              </Form.Text>
            ),
            Resolved: () => (
              <Form.Text className="text-success">
                <BsCheck className="mr-2" />
                Password reset claim success.
              </Form.Text>
            ),
          })}
        </Form>
      </Formik>
    </Container>
  );
};

export default RecoverLostPassword;
