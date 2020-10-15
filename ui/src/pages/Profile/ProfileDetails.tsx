import React from "react";
import { Formik, Form as FormikForm } from "formik";
import * as Yup from "yup";
import userService from "../../services/UserService/UserService";
import Form from "react-bootstrap/Form";
import Container from "react-bootstrap/Container";
import { UserContext } from "../../contexts/UserContext/UserContext";
import { BiArrowFromBottom } from "react-icons/bi";
import { usePromise } from "react-use-promise-matcher";
import FormikInput from "../../parts/FormikInput/FormikInput";
import FeedbackButton from "../../parts/FeedbackButton/FeedbackButton";

interface ProfileDetailsParams {
  login: string;
  email: string;
}

const ProfileDetails: React.FC = () => {
  const {
    dispatch,
    state: { apiKey, user },
  } = React.useContext(UserContext);

  const validationSchema: Yup.ObjectSchema<ProfileDetailsParams | undefined> = Yup.object({
    login: Yup.string().min(3, "At least 3 characters required").required("Required"),
    email: Yup.string().email("Correct email address required").required("Required"),
  });

  const [result, send, clear] = usePromise((values: ProfileDetailsParams) =>
    userService.changeProfileDetails(apiKey, values).then(() => dispatch({ type: "UPDATE_USER_DATA", user: values }))
  );

  return (
    <Container className="py-5">
      <h3>Profile details</h3>
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
            type="submit"
            label="Update profile data"
            Icon={BiArrowFromBottom}
            result={result}
            clear={clear}
          />
        </Form>
      </Formik>
    </Container>
  );
};

export default ProfileDetails;
