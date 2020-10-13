import React from "react";
import { Link, Redirect } from "react-router-dom";
import { useFormik } from "formik";
import * as Yup from "yup";
import userService from "../UserService/UserService";
import Form from "react-bootstrap/Form";
import Button from "react-bootstrap/Button";
import Container from "react-bootstrap/Container";
import { AppContext } from "../AppContext/AppContext";
import Spinner from "react-bootstrap/Spinner";
import { BiLogInCircle } from "react-icons/bi";

interface LoginParams {
  loginOrEmail: string;
  password: string;
}

const Login: React.FC = () => {
  const [isLoader, setLoader] = React.useState(false);

  const {
    dispatch,
    state: { loggedIn },
  } = React.useContext(AppContext);

  const validationSchema: Yup.ObjectSchema<LoginParams | undefined> = Yup.object({
    loginOrEmail: Yup.string().required("Required"),
    password: Yup.string().required("Required"),
  });

  const onSubmit = async (values: LoginParams) => {
    setLoader(true);
    try {
      const { apiKey } = await userService.login(values);
      dispatch({ type: "SET_API_KEY", apiKey });
      dispatch({
        type: "ADD_MESSAGE",
        message: { content: "Successfully logged in.", variant: "success" },
      });
    } catch (error) {
      dispatch({
        type: "ADD_MESSAGE",
        message: { content: "Incorrect login/email or password!", variant: "danger" },
      });
      console.error(error);
    } finally {
      setLoader(false);
    }
  };

  const formik = useFormik<LoginParams>({
    initialValues: {
      loginOrEmail: "",
      password: "",
    },
    onSubmit,
    validationSchema,
  });

  const handleSubmit = React.useCallback(
    (e?: React.FormEvent<HTMLElement> | undefined) => {
      formik.handleSubmit(e as React.FormEvent<HTMLFormElement>);
    },
    [formik]
  );

  if (loggedIn) return <Redirect to="/main" />;

  return (
    <Container className="py-5">
      <h3>Please sign in</h3>
      <Form onSubmit={handleSubmit}>
        <Form.Group>
          <Form.Label htmlFor="loginOrEmail">Login or email</Form.Label>
          <Form.Control
            type="text"
            name="loginOrEmail"
            id="loginOrEmail"
            onChange={formik.handleChange}
            onBlur={formik.handleBlur}
            value={formik.values.loginOrEmail}
            isValid={!formik.errors.loginOrEmail && formik.touched.loginOrEmail}
            isInvalid={!!formik.errors.loginOrEmail && formik.touched.loginOrEmail}
          />
          <Form.Control.Feedback type="invalid">{formik.errors.loginOrEmail}</Form.Control.Feedback>
        </Form.Group>

        <Form.Group>
          <Form.Label htmlFor="password">Password</Form.Label>
          <Form.Control
            type="password"
            name="password"
            id="password"
            onChange={formik.handleChange}
            onBlur={formik.handleBlur}
            value={formik.values.password}
            isValid={!formik.errors.password && formik.touched.password}
            isInvalid={!!formik.errors.password && formik.touched.password}
          />
          <Form.Control.Feedback type="invalid">{formik.errors.password}</Form.Control.Feedback>
        </Form.Group>

        <Button type="submit">
          {isLoader ? <Spinner as="span" animation="border" size="sm" role="loader" /> : <BiLogInCircle />}
          &nbsp;Sign In
        </Button>
        <Link className="btn btn-link" to="/recover-lost-password">
          Forgot password?
        </Link>
      </Form>
    </Container>
  );
};

export default Login;
