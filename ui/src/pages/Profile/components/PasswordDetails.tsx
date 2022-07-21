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
  currentPassword: Yup.string().min(3, "At least 3 characters required").required("Required"),
  newPassword: Yup.string().min(3, "At least 3 characters required").required("Required"),
  repeatedPassword: Yup.string()
    .oneOf([Yup.ref("newPassword")], "Passwords must match")
    .required("Required"),
});

type PasswordDetailsParams = Yup.InferType<typeof validationSchema>;

export const PasswordDetails: React.FC = () => {
  const {
    state: { apiKey },
  } = React.useContext(UserContext);

  const [result, send, clear] = usePromise(({ currentPassword, newPassword }: PasswordDetailsParams) =>
    userService.changePassword(apiKey, { currentPassword, newPassword })
  );

  return (
    <Container className="py-5">
      <Row>
        <Col md={9} lg={7} xl={6} className="mx-auto">
          <h3 className="mb-4">Password details</h3>
          <Formik<PasswordDetailsParams>
            initialValues={{
              currentPassword: "",
              newPassword: "",
              repeatedPassword: "",
            }}
            onSubmit={send}
            validationSchema={validationSchema}
          >
            <Form as={FormikForm}>
              <FormikInput name="currentPassword" label="Current password" type="password" />
              <FormikInput name="newPassword" label="New password" type="password" />
              <FormikInput name="repeatedPassword" label="Repeat new password" type="password" />

              <FeedbackButton
                className="float-end"
                type="submit"
                label="Update password"
                variant="dark"
                Icon={BiArrowFromBottom}
                result={result}
                clear={clear}
                successLabel="Password changed"
              />
            </Form>
          </Formik>
        </Col>
      </Row>
    </Container>
  );
};
