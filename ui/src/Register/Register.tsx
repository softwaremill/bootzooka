import React from "react";
import { Redirect } from "react-router-dom";
import { useFormik } from "formik";
import * as Yup from "yup";
import userService from "../UserService/UserService";
import Form from "react-bootstrap/Form";
import Button from "react-bootstrap/Button";
import Container from "react-bootstrap/Container";
import { AppContext } from "../AppContext/AppContext";

interface RegisterParams {
  login: string;
  email: string;
  password: string;
  repeatedPassword: string;
}

const Register: React.FC = () => {
  const [isRegistered, setRegistered] = React.useState(false);
  const { dispatch } = React.useContext(AppContext);

  const validationSchema: Yup.ObjectSchema<RegisterParams | undefined> = Yup.object({
    login: Yup.string().min(3, "At least 3 characters required").required("Required"),
    email: Yup.string().email("Correct email address required").required("Required"),
    password: Yup.string().min(5, "At least 5 characters required").required("Required"),
    repeatedPassword: Yup.string()
      .oneOf([Yup.ref("password")], "Passwords must match")
      .required("Required"),
  });

  const onSubmit = async (values: RegisterParams) => {
    try {
      const { login, email, password } = values;
      const { apiKey } = await userService.registerUser({ login, email, password });
      dispatch({
        type: "SET_API_KEY",
        apiKey,
      });
      dispatch({ type: "ADD_MESSAGE", message: { content: "Successfully registered.", variant: "success" } });
      setRegistered(true);
    } catch (error) {
      const response = error?.response?.data?.error || error.message;
      dispatch({
        type: "ADD_MESSAGE",
        message: { content: `Could not register new user! ${response}`, variant: "danger" },
      });
      console.error(error);
    }
  };

  const formik = useFormik<RegisterParams>({
    initialValues: {
      login: "",
      email: "",
      password: "",
      repeatedPassword: "",
    },
    onSubmit,
    validationSchema,
  });

  const handleSubmit = (e?: React.FormEvent<HTMLElement> | undefined) => {
    try {
      formik.handleSubmit(e as React.FormEvent<HTMLFormElement>);
    } catch (e) {
      console.error(e);
    }
  };

  if (isRegistered) return <Redirect to="/login" />;

  return (
    <Container className="py-5">
      <h3>Please sign up</h3>
      <Form onSubmit={handleSubmit}>
        <Form.Group>
          <Form.Label>Login</Form.Label>
          <Form.Control
            type="text"
            name="login"
            onChange={formik.handleChange}
            onBlur={formik.handleBlur}
            value={formik.values.login}
            isValid={!formik.errors.login && formik.touched.login}
            isInvalid={!!formik.errors.login && formik.touched.login}
          />
          <Form.Control.Feedback type="invalid">{formik.errors.login}</Form.Control.Feedback>
        </Form.Group>

        <Form.Group>
          <Form.Label>Email address</Form.Label>
          <Form.Control
            type="text"
            name="email"
            onChange={formik.handleChange}
            onBlur={formik.handleBlur}
            value={formik.values.email}
            isValid={!formik.errors.email && formik.touched.email}
            isInvalid={!!formik.errors.email && formik.touched.email}
          />
          <Form.Control.Feedback type="invalid">{formik.errors.email}</Form.Control.Feedback>
        </Form.Group>

        <Form.Group>
          <Form.Label>Passsword</Form.Label>
          <Form.Control
            type="password"
            name="password"
            onChange={formik.handleChange}
            onBlur={formik.handleBlur}
            value={formik.values.password}
            isValid={!formik.errors.password && formik.touched.password}
            isInvalid={!!formik.errors.password && formik.touched.password}
          />
          <Form.Control.Feedback type="invalid">{formik.errors.password}</Form.Control.Feedback>
        </Form.Group>

        <Form.Group>
          <Form.Label>Repeat Passsword</Form.Label>
          <Form.Control
            type="password"
            name="repeatedPassword"
            onChange={formik.handleChange}
            onBlur={formik.handleBlur}
            value={formik.values.repeatedPassword}
            isValid={!formik.errors.repeatedPassword && formik.touched.repeatedPassword}
            isInvalid={!!formik.errors.repeatedPassword && formik.touched.repeatedPassword}
          />
          <Form.Control.Feedback type="invalid">{formik.errors.repeatedPassword}</Form.Control.Feedback>
        </Form.Group>

        <Button type="submit">Register</Button>
      </Form>
    </Container>
  );
};

export default Register;
