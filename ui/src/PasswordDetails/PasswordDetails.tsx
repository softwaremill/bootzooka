import React from "react";
import { useFormik } from "formik";
import * as Yup from "yup";
import userService from "../UserService/UserService";
import Form from "react-bootstrap/Form";
import Button from "react-bootstrap/Button";
import Container from "react-bootstrap/Container";
import { AppContext } from "../AppContext/AppContext";

interface PasswordDetailsParams {
  currentPassword: string;
  newPassword: string;
  repeatedPassword: string;
}

const ProfileDetails: React.FC = () => {
  const {
    dispatch,
    state: { apiKey },
  } = React.useContext(AppContext);

  const validationSchema: Yup.ObjectSchema<PasswordDetailsParams | undefined> = Yup.object({
    currentPassword: Yup.string().min(3, "At least 3 characters required").required("Required"),
    newPassword: Yup.string().min(3, "At least 3 characters required").required("Required"),
    repeatedPassword: Yup.string()
      .oneOf([Yup.ref("newPassword")], "Passwords must match")
      .required("Required"),
  });

  const onSubmit = async (values: PasswordDetailsParams) => {
    if (!apiKey) return;

    try {
      const { currentPassword, newPassword } = values;
      await userService.changePassword(apiKey, { currentPassword, newPassword });
      dispatch({ type: "ADD_MESSAGE", message: { content: "Password changed!", variant: "success" } });
    } catch (error) {
      const response = error?.response?.data?.error || error.message;
      dispatch({
        type: "ADD_MESSAGE",
        message: { content: `Could not change password! ${response}`, variant: "danger" },
      });
      console.error(error);
    }
  };

  const formik = useFormik<PasswordDetailsParams>({
    initialValues: {
      currentPassword: "",
      newPassword: "",
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

  return (
    <Container className="py-5">
      <h3>Password details</h3>
      <Form onSubmit={handleSubmit}>
        <Form.Group>
          <Form.Label>Current Password</Form.Label>
          <Form.Control
            type="password"
            name="currentPassword"
            onChange={formik.handleChange}
            onBlur={formik.handleBlur}
            value={formik.values.currentPassword}
            isValid={!formik.errors.currentPassword && formik.touched.currentPassword}
            isInvalid={!!formik.errors.currentPassword && formik.touched.currentPassword}
          />
          <Form.Control.Feedback type="invalid">{formik.errors.currentPassword}</Form.Control.Feedback>
        </Form.Group>
        <Form.Group>
          <Form.Label>New password</Form.Label>
          <Form.Control
            type="password"
            name="newPassword"
            onChange={formik.handleChange}
            onBlur={formik.handleBlur}
            value={formik.values.newPassword}
            isValid={!formik.errors.newPassword && formik.touched.newPassword}
            isInvalid={!!formik.errors.newPassword && formik.touched.newPassword}
          />
          <Form.Control.Feedback type="invalid">{formik.errors.newPassword}</Form.Control.Feedback>
        </Form.Group>
        <Form.Group>
          <Form.Label>Repeat new password</Form.Label>
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

        <Button type="submit">Update password</Button>
      </Form>
    </Container>
  );
};

export default ProfileDetails;
