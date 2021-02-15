import React from "react";
import { Form as FormikForm, Formik } from "formik";
import * as Yup from "yup";
import userServiceFP from "../../services/UserService/UserServiceFP";
import Form from "react-bootstrap/Form";
import Container from "react-bootstrap/Container";
import Col from "react-bootstrap/Col";
import Row from "react-bootstrap/Row";
import { UserContext } from "../../contexts/UserContext/UserContext";
import { BiArrowFromBottom } from "react-icons/bi";
import { usePromise } from "react-use-promise-matcher";
import FormikInput from "../../parts/FormikInput/FormikInput";
import FeedbackButton from "../../parts/FeedbackButton/FeedbackButton";
import { getOrElse } from 'fp-ts/Option';
import { pipe } from "fp-ts/pipeable";

interface ProfileDetailsParams {
  login: string;
  email: string;
}

const validationSchema: Yup.ObjectSchema<ProfileDetailsParams | undefined> = Yup.object({
  login: Yup.string().min(3, "At least 3 characters required").required("Required"),
  email: Yup.string().email("Correct email address required").required("Required"),
});

const ProfileDetails: React.FC = () => {
  const {
    dispatch,
    state: { user },
  } = React.useContext(UserContext);

  const [result, send, clear] = usePromise((values: ProfileDetailsParams) =>
    userServiceFP.changeProfileDetails(values).then((value) => dispatch({ type: "UPDATE_USER_DATA", user: values }))
  );

  return (
    <Container className="py-5">
      <Row>
        <Col md={9} lg={7} xl={6} className="mx-auto">
          <h3>Profile details</h3>
          <Formik<ProfileDetailsParams>
            initialValues={
              pipe(
                user,
                getOrElse(() => ({ login: '', email: '' })),
              )
            }
            onSubmit={send}
            validationSchema={validationSchema}
          >
            <Form as={FormikForm}>
              <FormikInput name="login" label="Login" />
              <FormikInput name="email" label="Email address" />

              <FeedbackButton
                type="submit"
                label="Update profile data"
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

export default ProfileDetails;
