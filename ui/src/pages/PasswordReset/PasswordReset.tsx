import React from "react";
import { Formik, Form as FormikForm } from "formik";
import * as Yup from "yup";
import Form from "react-bootstrap/Form";
import Container from "react-bootstrap/Container";
import Col from "react-bootstrap/Col";
import Row from "react-bootstrap/Row";
import { BiArrowFromBottom } from "react-icons/bi";
import { passwordService } from "services";
import { FormikInput, FeedbackButton } from "components";
import { useMutation } from "react-query";

const validationSchema = Yup.object({
  password: Yup.string().min(3, "At least 3 characters required").required("Required"),
  repeatedPassword: Yup.string()
    .oneOf([Yup.ref("password")], "Passwords must match")
    .required("Required"),
});

type PasswordResetParams = Yup.InferType<typeof validationSchema>;

export const PasswordReset: React.FC = () => {
  const code = new URLSearchParams(window.location.search).get("code") || "";
  const mutation = useMutation(({ password }: PasswordResetParams) =>
    passwordService.resetPassword({ password, code }),
  );

  return (
    <Container className="py-5">
      <Row>
        <Col md={9} lg={7} xl={6} className="mx-auto">
          <h3>Password details</h3>
          <Formik<PasswordResetParams>
            initialValues={{
              password: "",
              repeatedPassword: "",
            }}
            onSubmit={(values) => mutation.mutate(values)}
            validationSchema={validationSchema}
          >
            <Form as={FormikForm}>
              <FormikInput name="password" label="New password" type="password" />
              <FormikInput name="repeatedPassword" label="Repeat new password" type="password" />

              <FeedbackButton
                type="submit"
                label="Update password"
                Icon={BiArrowFromBottom}
                mutation={mutation}
                successLabel="Password changed"
              />
            </Form>
          </Formik>
        </Col>
      </Row>
    </Container>
  );
};
