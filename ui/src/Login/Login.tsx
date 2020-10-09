import React from "react";
import { Link, Redirect } from "react-router-dom";
import { useFormik, FormikErrors } from "formik";
import userService from "../UserService/UserService";
import Form from "react-bootstrap/Form";
import Button from "react-bootstrap/Button";
import Container from "react-bootstrap/Container";

interface LoginProps {
  isLoggedIn: boolean;
  notifyError: (error: string) => void;
  onLoggedIn: (apiKey: string) => void;
}

interface LoginParams {
  loginOrEmail: string;
  password: string;
}

const Login: React.FC<LoginProps> = ({ isLoggedIn, notifyError, onLoggedIn }) => {
  const validate = ({ loginOrEmail, password }: LoginParams) => {
    const errors: FormikErrors<LoginParams> = {};
    if (!loginOrEmail) errors.loginOrEmail = "Required";
    if (!password) errors.password = "Required";

    return errors;
  };

  const onSubmit = async (values: LoginParams) => {
    try {
      const { apiKey } = await userService.login(values);
      await onLoggedIn(apiKey);
    } catch (error) {
      notifyError("Incorrect login/email or password!");
      console.error(error);
    }
  };

  const formik = useFormik({
    initialValues: {
      loginOrEmail: "",
      password: "",
    },
    onSubmit,
    validate,
  });

  if (isLoggedIn) return <Redirect to="/main" />;

  return (
    <Container className="py-5">
      <h3>Please sign in</h3>
      <Form onSubmit={(e) => formik.handleSubmit(e as React.FormEvent<HTMLFormElement>)}>
        <Form.Group>
          <Form.Label>Login or email</Form.Label>
          <Form.Control
            type="text"
            name="loginOrEmail"
            placeholder="Login or email"
            onChange={formik.handleChange}
            onBlur={formik.handleBlur}
            value={formik.values.loginOrEmail}
            isInvalid={!!formik.errors.loginOrEmail && formik.touched.loginOrEmail}
          />
          <Form.Control.Feedback type="invalid">{formik.errors.loginOrEmail}</Form.Control.Feedback>
        </Form.Group>
        <Form.Group>
          <Form.Label>Passsword</Form.Label>
          <Form.Control
            type="password"
            name="password"
            placeholder="Password"
            onChange={formik.handleChange}
            onBlur={formik.handleBlur}
            value={formik.values.password}
            isInvalid={!!formik.errors.password && formik.touched.password}
          />
          <Form.Control.Feedback type="invalid">{formik.errors.password}</Form.Control.Feedback>
        </Form.Group>
        <Button type="submit">Sign In</Button>
        <Link className="btn btn-link" to="/recover-lost-password">
          Forgot password?
        </Link>
      </Form>
    </Container>
  );
};

export default Login;
