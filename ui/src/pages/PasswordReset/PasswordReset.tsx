import React from "react";
import { Formik, Form as FormikForm } from "formik";
import * as Yup from "yup";
import passwordService from "../../services/PasswordService/PasswordService";
import Form from "react-bootstrap/Form";
import Container from "react-bootstrap/Container";
import { BiArrowFromBottom } from "react-icons/bi";
import { usePromise } from "react-use-promise-matcher";
import FormikInput from "../../parts/FormikInput/FormikInput";
import FeedbackButton from "../../parts/FeedbackButton/FeedbackButton";

interface PasswordResetParams {
  password: string;
  repeatedPassword: string;
}

const ProfileDetails: React.FC = () => {
  const validationSchema: Yup.ObjectSchema<PasswordResetParams | undefined> = Yup.object({
    password: Yup.string().min(3, "At least 3 characters required").required("Required"),
    repeatedPassword: Yup.string()
      .oneOf([Yup.ref("password")], "Passwords must match")
      .required("Required"),
  });

  const code = new URLSearchParams(window.location.search).get("code") || "";

  const [result, send, clear] = usePromise(({ password }: PasswordResetParams) =>
    passwordService.resetPassword({ password, code })
  );

  return (
    <Container className="py-5">
      <h3>Password details</h3>
      <Formik<PasswordResetParams>
        initialValues={{
          password: "",
          repeatedPassword: "",
        }}
        onSubmit={send}
        validationSchema={validationSchema}
      >
        <Form as={FormikForm}>
          <FormikInput name="password" label="New password" type="password" />
          <FormikInput name="repeatedPassword" label="Repeat new password" type="password" />

          <FeedbackButton
            type="submit"
            label="Update password"
            Icon={BiArrowFromBottom}
            result={result}
            clear={clear}
            successLabel="Password changed"
          />
        </Form>
      </Formik>
    </Container>
  );
};

export default ProfileDetails;
