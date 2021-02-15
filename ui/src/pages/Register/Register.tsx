import React from "react";
import { Redirect } from "react-router-dom";
import { Form as FormikForm, Formik } from "formik";
import * as Yup from "yup";
import userService from "../../services/UserService/UserServiceFP";
import Form from "react-bootstrap/Form";
import Container from "react-bootstrap/Container";
import Col from "react-bootstrap/Col";
import Row from "react-bootstrap/Row";
import { UserContext } from "../../contexts/UserContext/UserContext";
import { BiUserPlus } from "react-icons/bi";
import { usePromise } from "react-use-promise-matcher";
import FormikInput from "../../parts/FormikInput/FormikInput";
import FeedbackButton from "../../parts/FeedbackButton/FeedbackButton";
import { pipe } from "fp-ts/pipeable";
import { fold, some } from "fp-ts/Option";

interface RegisterParams {
  login: string;
  email: string;
  password: string;
  repeatedPassword: string;
}

const validationSchema: Yup.ObjectSchema<RegisterParams | undefined> = Yup.object({
  login: Yup.string().min(3, "At least 3 characters required").required("Required"),
  email: Yup.string().email("Correct email address required").required("Required"),
  password: Yup.string().min(5, "At least 5 characters required").required("Required"),
  repeatedPassword: Yup.string()
    .oneOf([Yup.ref("password")], "Passwords must match")
    .required("Required"),
});

const Register: React.FC = () => {
  const {
    dispatch,
    state: { user },
  } = React.useContext(UserContext);

  // TODO: usePromise takes a function that works as a loader function. It's a function that makes an asynchronous computation
  const [result, send, clear] = usePromise(({ login, email, password }: RegisterParams) =>
    userService
      .registerUser({ login, email, password })
      .then((value) => dispatch({ type: "SET_API_KEY", apiKey: some('') }))
  );

  return pipe(
    user,
    fold(
      () => <Container className="py-5">
        <Row>
          <Col md={9} lg={7} xl={6} className="mx-auto">
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
          </Col>
        </Row>
      </Container>,
      _ => <Redirect to="/main" />
    )
  )
};

export default Register;
