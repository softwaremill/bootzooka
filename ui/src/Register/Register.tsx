import React from "react";
import { Redirect } from "react-router-dom";
import { Formik, Form as FormikForm } from "formik";
import * as Yup from "yup";
import userService from "../UserService/UserService";
import Form from "react-bootstrap/Form";
import Container from "react-bootstrap/Container";
import { UserContext } from "../UserContext/UserContext";
import { BiUserPlus } from "react-icons/bi";
import { usePromise } from "react-use-promise-matcher";
import FormikInput from "../FormikInput/FormikInput";
import FeedbackButton from "../FeedbackButton/FeedbackButton";

interface RegisterParams {
  login: string;
  email: string;
  password: string;
  repeatedPassword: string;
}

const Register: React.FC = () => {
  const {
    dispatch,
    state: { loggedIn },
  } = React.useContext(UserContext);

  const validationSchema: Yup.ObjectSchema<RegisterParams | undefined> = Yup.object({
    login: Yup.string().min(3, "At least 3 characters required").required("Required"),
    email: Yup.string().email("Correct email address required").required("Required"),
    password: Yup.string().min(5, "At least 5 characters required").required("Required"),
    repeatedPassword: Yup.string()
      .oneOf([Yup.ref("password")], "Passwords must match")
      .required("Required"),
  });

  const [result, send, clear] = usePromise(({ login, email, password }: RegisterParams) =>
    userService.registerUser({ login, email, password }).then(({ apiKey }) => dispatch({ type: "SET_API_KEY", apiKey }))
  );

  if (loggedIn) return <Redirect to="/main" />;

  return (
    <Container className="py-5">
      <h3>Please sign up</h3>
      <Formik<RegisterParams>
        initialValues={{
          login: "",
          email: "",
          password: "",
          repeatedPassword: "",
        }}
        onSubmit={send}
        validationSchema={validationSchema}
      >
        <Form as={FormikForm}>
          <FormikInput name="login" label="Login" />
          <FormikInput name="email" label="Email address" />
          <FormikInput name="password" label="Password" type="password" />
          <FormikInput name="repeatedPassword" label="Repeat password" type="password" />

          <FeedbackButton type="submit" label="Register" Icon={BiUserPlus} result={result} clear={clear} />
        </Form>
      </Formik>
    </Container>
  );
};

export default Register;
