import React from "react";
import { Formik, Form as FormikForm } from "formik";
import * as Yup from "yup";
import userService from "../UserService/UserService";
import Form from "react-bootstrap/Form";
import Button from "react-bootstrap/Button";
import Container from "react-bootstrap/Container";
import { AppContext } from "../AppContext/AppContext";
import Spinner from "react-bootstrap/Spinner";
import { BiArrowFromBottom } from "react-icons/bi";
import { BsExclamationCircle, BsCheck } from "react-icons/bs";
import { usePromise } from "react-use-promise-matcher";
import FormikInput from "../FormikInput/FormikInput";

interface ProfileDetailsParams {
  login: string;
  email: string;
}

const ProfileDetails: React.FC = () => {
  const {
    dispatch,
    state: { apiKey, user },
  } = React.useContext(AppContext);

  const validationSchema: Yup.ObjectSchema<ProfileDetailsParams | undefined> = Yup.object({
    login: Yup.string().min(3, "At least 3 characters required").required("Required"),
    email: Yup.string().email("Correct email address required").required("Required"),
  });

  const [result, send] = usePromise((values: ProfileDetailsParams) =>
    userService
      .changeProfileDetails(apiKey, values)
      .then(() => {
        dispatch({
          type: "SET_USER_DATA",
          user: values,
        });
      })
      .catch((error) => {
        throw new Error(error?.response?.data?.error || error.message);
      })
  );

  return (
    <Container className="py-5">
      <h3>Profile details</h3>
      <Formik<ProfileDetailsParams>
        initialValues={{
          login: user.login || "",
          email: user.email || "",
        }}
        onSubmit={send}
        validationSchema={validationSchema}
      >
        <Form as={FormikForm}>
          <FormikInput name="login" label="Login" />
          <FormikInput name="email" label="Email address" />

          <Button type="submit" disabled={result.isLoading}>
            <BiArrowFromBottom />
            &nbsp;Update profile data
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
                Update success.
              </Form.Text>
            ),
          })}
        </Form>
      </Formik>
    </Container>
  );
};

export default ProfileDetails;
