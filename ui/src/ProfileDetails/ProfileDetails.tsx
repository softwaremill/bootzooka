import React from "react";
import { useFormik } from "formik";
import * as Yup from "yup";
import userService from "../UserService/UserService";
import Form from "react-bootstrap/Form";
import Button from "react-bootstrap/Button";
import Container from "react-bootstrap/Container";
import { AppContext } from "../AppContext/AppContext";

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

  const onSubmit = async (values: ProfileDetailsParams) => {
    if (!apiKey || !user) return;

    try {
      await userService.changeProfileDetails(apiKey, values);
      dispatch({
        type: "SET_USER_DATA",
        user: { ...user, ...values },
      });
      dispatch({ type: "ADD_MESSAGE", message: { content: "Profile details changed!", variant: "success" } });
    } catch (error) {
      const response = error?.response?.data?.error || error.message;
      dispatch({
        type: "ADD_MESSAGE",
        message: { content: `Could not change profile details! ${response}`, variant: "danger" },
      });
      console.error(error);
    }
  };

  const formik = useFormik<ProfileDetailsParams>({
    initialValues: {
      login: user?.login || "",
      email: user?.email || "",
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

  return (
    <Container className="py-5">
      <h3>Profile details</h3>
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

        <Button type="submit">Update profile data</Button>
      </Form>
    </Container>
  );
};

export default ProfileDetails;
