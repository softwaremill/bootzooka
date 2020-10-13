import React from "react";
import { Redirect } from "react-router-dom";
import { useFormik } from "formik";
import * as Yup from "yup";
import passwordService from "../PasswordService/PasswordService";
import Form from "react-bootstrap/Form";
import Button from "react-bootstrap/Button";
import Container from "react-bootstrap/Container";
import { AppContext } from "../AppContext/AppContext";
import Spinner from "react-bootstrap/Spinner";
import { BiReset } from "react-icons/bi";

interface RecoverLostPasswordParams {
  loginOrEmail: string;
}

const RecoverLostPassword: React.FC = () => {
  const [isLoader, setLoader] = React.useState(false);
  const [isRecoveryComplete, setRecoveryComplete] = React.useState(false);

  const { dispatch } = React.useContext(AppContext);

  const validationSchema: Yup.ObjectSchema<RecoverLostPasswordParams | undefined> = Yup.object({
    loginOrEmail: Yup.string().required("Required"),
  });

  const onSubmit = async (values: RecoverLostPasswordParams) => {
    setLoader(true);
    try {
      await passwordService.claimPasswordReset(values);
      dispatch({
        type: "ADD_MESSAGE",
        message: { content: "Password reset claim success.", variant: "success" },
      });
      setRecoveryComplete(true);
    } catch (error) {
      const response = error?.response?.data?.error || error.message;
      dispatch({
        type: "ADD_MESSAGE",
        message: { content: `Could not claim password reset! ${response}`, variant: "danger" },
      });
      console.error(error);
    } finally {
      setLoader(false);
    }
  };

  const formik = useFormik<RecoverLostPasswordParams>({
    initialValues: {
      loginOrEmail: "",
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

  if (isRecoveryComplete) return <Redirect to="/login" />;

  return (
    <Container className="py-5">
      <h3>Recover lost password</h3>
      <Form onSubmit={handleSubmit}>
        <Form.Group>
          <Form.Label>Login or email</Form.Label>
          <Form.Control
            type="text"
            name="loginOrEmail"
            onChange={formik.handleChange}
            onBlur={formik.handleBlur}
            value={formik.values.loginOrEmail}
            isValid={!formik.errors.loginOrEmail && formik.touched.loginOrEmail}
            isInvalid={!!formik.errors.loginOrEmail && formik.touched.loginOrEmail}
          />
          <Form.Control.Feedback type="invalid">{formik.errors.loginOrEmail}</Form.Control.Feedback>
        </Form.Group>

        <Button type="submit">
          {isLoader ? <Spinner as="span" animation="border" size="sm" role="loader" /> : <BiReset />}
          &nbsp;Reset password
        </Button>
      </Form>
    </Container>
  );
};

export default RecoverLostPassword;
