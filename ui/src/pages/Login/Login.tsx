import React from "react";
import { Link, Redirect } from "react-router-dom";
import { Formik, Form as FormikForm } from "formik";
import * as Yup from "yup";
import userService from "../../services/UserService/UserService";
import Form from "react-bootstrap/Form";
import Container from "react-bootstrap/Container";
import { UserContext } from "../../contexts/UserContext/UserContext";
import { BiLogInCircle } from "react-icons/bi";
import { usePromise } from "react-use-promise-matcher";
import FormikInput from "../../parts/FormikInput/FormikInput";
import FeedbackButton from "../../parts/FeedbackButton/FeedbackButton";

interface LoginParams {
  loginOrEmail: string;
  password: string;
}

const Login: React.FC = () => {
  const {
    dispatch,
    state: { loggedIn },
  } = React.useContext(UserContext);

  const validationSchema: Yup.ObjectSchema<LoginParams | undefined> = Yup.object({
    loginOrEmail: Yup.string().required("Required"),
    password: Yup.string().required("Required"),
  });

  const [result, send, clear] = usePromise((values: LoginParams) =>
    userService
      .login(values)
      .then(({ apiKey }) => dispatch({ type: "SET_API_KEY", apiKey }))
      .catch((error) => {
        if (error?.response?.status === 404) throw new Error("Incorrect login/email or password!");

        throw error;
      })
  );

  if (loggedIn) return <Redirect to="/main" />;

  return (
    <Container className="py-5">
      <h3>Please sign in</h3>
      <Formik<LoginParams>
        initialValues={{
          loginOrEmail: "",
          password: "",
        }}
        onSubmit={send}
        validationSchema={validationSchema}
      >
        <Form as={FormikForm}>
          <FormikInput name="loginOrEmail" label="Login or email" />
          <FormikInput name="password" type="password" label="Password" />
          <Link className="float-right" to="/recover-lost-password">
            Forgot password?
          </Link>
          <FeedbackButton type="submit" label="Sign In" Icon={BiLogInCircle} result={result} clear={clear} />{" "}
        </Form>
      </Formik>
    </Container>
  );
};

export default Login;
