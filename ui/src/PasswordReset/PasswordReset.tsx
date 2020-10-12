import React from "react";
import { Redirect } from "react-router-dom";
import { useFormik } from "formik";
import * as Yup from "yup";
import passwordService from "../PasswordService/PasswordService";
import Form from "react-bootstrap/Form";
import Button from "react-bootstrap/Button";
import Container from "react-bootstrap/Container";
import { AppContext } from "../AppContext/AppContext";

interface PasswordResetParams {
  password: string;
  repeatedPassword: string;
}

const ProfileDetails: React.FC = () => {
  const [isResetComplete, setResetComplete] = React.useState(false);
  const { dispatch } = React.useContext(AppContext);

  const validationSchema: Yup.ObjectSchema<PasswordResetParams | undefined> = Yup.object({
    password: Yup.string().min(3, "At least 3 characters required").required("Required"),
    repeatedPassword: Yup.string()
      .oneOf([Yup.ref("password")], "Passwords must match")
      .required("Required"),
  });

  const onSubmit = async (values: PasswordResetParams) => {
    try {
      const { password } = values;
      const urlParams = new URLSearchParams(window.location.search);
      const code = urlParams.get("code");

      if (!code) throw new Error("URL Code not provided");

      await passwordService.resetPassword({ password, code });
      dispatch({ type: "ADD_MESSAGE", message: { content: "Password changed!", variant: "success" } });
      setResetComplete(true);
    } catch (error) {
      const response = error?.response?.data?.error || error.message;
      dispatch({
        type: "ADD_MESSAGE",
        message: { content: `Could not change password! ${response}`, variant: "danger" },
      });
      console.error(error);
    }
  };

  const formik = useFormik<PasswordResetParams>({
    initialValues: {
      password: "",
      repeatedPassword: "",
    },
    onSubmit,
    validationSchema,
  });

  const handleSubmit = React.useCallback(
    (e?: React.FormEvent<HTMLElement> | undefined) => {
      try {
        formik.handleSubmit(e as React.FormEvent<HTMLFormElement>);
      } catch (e) {
        console.error(e);
      }
    },
    [formik]
  );

  if (isResetComplete) return <Redirect to="/login" />;

  return (
    <Container className="py-5">
      <h3>Password details</h3>
      <Form onSubmit={handleSubmit}>
        <Form.Group>
          <Form.Label>New password</Form.Label>
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
