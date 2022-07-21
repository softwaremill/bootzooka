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

export const ProfileDetails: React.FC = () => {
  const {
    dispatch,
    state: { apiKey, user },
  } = React.useContext(UserContext);

  const [result, send, clear] = usePromise((values: ProfileDetailsParams) =>
    userService.changeProfileDetails(apiKey, values).then(() => dispatch({ type: "UPDATE_USER_DATA", user: values }))
  );

  return (
    <Container className="py-5">
      <Row>
        <Col md={9} lg={7} xl={6} className="mx-auto">
          <h3 className="mb-4">Profile details</h3>
          <Formik<ProfileDetailsParams>
            initialValues={{
              login: user?.login || "",
              email: user?.email || "",
            }}
            onSubmit={send}
            validationSchema={validationSchema}
          >
            <Form as={FormikForm}>
              <FormikInput name="login" label="Login" />
              <FormikInput name="email" label="Email address" />

              <FeedbackButton
                className="float-end"
                type="submit"
                label="Update profile data"
                variant="dark"
                Icon={BiArrowFromBottom}
                result={result}
                clear={clear}
                successLabel="Profile details changed"
              />
            </Form>
          </Formik>
        </Col>
      </Row>
    </Container>
  );
};
