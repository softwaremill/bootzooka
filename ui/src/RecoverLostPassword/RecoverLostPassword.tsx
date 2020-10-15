import React from "react";
import { Formik, Form as FormikForm } from "formik";
import * as Yup from "yup";
import passwordService from "../PasswordService/PasswordService";
import Form from "react-bootstrap/Form";
import Container from "react-bootstrap/Container";
import { BiReset } from "react-icons/bi";
import { usePromise } from "react-use-promise-matcher";
import FormikInput from "../FormikInput/FormikInput";
import FeedbackButton from "../FeedbackButton/FeedbackButton";

interface RecoverLostPasswordParams {
  loginOrEmail: string;
}

const RecoverLostPassword: React.FC = () => {
  const validationSchema: Yup.ObjectSchema<RecoverLostPasswordParams | undefined> = Yup.object({
    loginOrEmail: Yup.string().required("Required"),
  });

  const [result, send, clear] = usePromise((values: RecoverLostPasswordParams) =>
    passwordService.claimPasswordReset(values)
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
          <FeedbackButton type="submit" label="Reset password" Icon={BiReset} result={result} clear={clear} />
        </Form>
      </Formik>
    </Container>
  );
};

export default RecoverLostPassword;
