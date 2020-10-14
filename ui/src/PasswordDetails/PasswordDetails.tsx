import React from "react";
import { Formik, Form as FormikForm } from "formik";
import * as Yup from "yup";
import userService from "../UserService/UserService";
import Form from "react-bootstrap/Form";
import Button from "react-bootstrap/Button";
import Container from "react-bootstrap/Container";
import { AppContext } from "../AppContext/AppContext";
import { BiArrowFromBottom } from "react-icons/bi";
import { usePromise } from "react-use-promise-matcher";
import FormikInput from "../FormikInput/FormikInput";
import FormFeedback from "../FormFeedback/FormFeedback";

interface PasswordDetailsParams {
  currentPassword: string;
  newPassword: string;
  repeatedPassword: string;
}

const ProfileDetails: React.FC = () => {
  const {
    state: { apiKey },
  } = React.useContext(AppContext);

  const validationSchema: Yup.ObjectSchema<PasswordDetailsParams | undefined> = Yup.object({
    currentPassword: Yup.string().min(3, "At least 3 characters required").required("Required"),
    newPassword: Yup.string().min(3, "At least 3 characters required").required("Required"),
    repeatedPassword: Yup.string()
      .oneOf([Yup.ref("newPassword")], "Passwords must match")
      .required("Required"),
  });

  const [result, send] = usePromise(({ currentPassword, newPassword }: PasswordDetailsParams) =>
    userService.changePassword(apiKey, { currentPassword, newPassword }).catch((error) => {
      throw new Error(error?.response?.data?.error || error.message);
    })
  );

  return (
    <Container className="py-5">
      <h3>Password details</h3>
      <Formik<PasswordDetailsParams>
        initialValues={{
          currentPassword: "",
          newPassword: "",
          repeatedPassword: "",
        }}
        onSubmit={send}
        validationSchema={validationSchema}
      >
        <Form as={FormikForm}>
          <FormikInput name="currentPassword" label="Current password" type="password" />
          <FormikInput name="newPassword" label="New password" type="password" />
          <FormikInput name="repeatedPassword" label="Repeat new password" type="password" />

          <Button type="submit">
            <BiArrowFromBottom />
            &nbsp;Update password
          </Button>
          <FormFeedback result={result} />
        </Form>
      </Formik>
    </Container>
  );
};

export default ProfileDetails;
