import React, { useCallback, useState } from 'react';
import { Form, Formik, useField } from "formik";
import { Link } from "react-router-dom";
import * as Yup from "yup";
import { changePassword } from "../UserService/UserService";

type FormValues = {
  currentPassword: string;
  newPassword: string;
  repeatedNewPassword: string;
}

type Props = {
  apiKey: string;
  notifyError: (msg: string) => void;
  notifySuccess: (msg: string) => void;
}

const validationSchema = Yup.object<FormValues>({
  currentPassword: Yup.string()
    .required('Current password required'),
  newPassword: Yup.string()
    .required('New password Required')
    .min(5, "New password must be at least 5 characters long"),
  repeatedNewPassword: Yup.string()
    .required('Repeated password required')
    .when("newPassword", {
      is: (value: string) => !!(value && value.length > 0),
      then: Yup.string().oneOf(
        [Yup.ref("newPassword")],
        "Passwords don't match!"
      )
    }),
});

const initialValues: FormValues = {
  currentPassword: '',
  newPassword: '',
  repeatedNewPassword: ''
};

const PasswordDetails: React.FC<Props> = (props) => {
  const { notifyError, notifySuccess, apiKey } = props;
  const [isSubmitting, setSubmitting] = useState(false);

  const handleSubmit = useCallback(async ({ currentPassword, newPassword }) => {
    (await changePassword(apiKey, { currentPassword, newPassword })).fold({
      left: (error: Error) => {
        notifyError('Could not change password!');
        console.error(error);
      },
      right: () => {
        notifySuccess('Password changed!');
      }
    });
  }, []);

  const TextField = ({ label, ...props }: any) => {
    const [field, meta] = useField(props);
    return (
      <>
        <label htmlFor={props.id || props.name}>{label}</label>
        <input {...field} {...props} />
        {meta.touched && meta.error ? <div className="validation-message">{meta.error}</div> : null}
      </>
    );
  };

  return (
    <div className="PasswordDetails">
      <h4>Password details</h4>
      <Formik initialValues={initialValues}
              onSubmit={handleSubmit}
              validationSchema={validationSchema}>
        <Form className="CommonForm">
          <TextField type="password" name="currentPassword" placeholder="Current password"/>
          <TextField type="password" name="newPassword" placeholder="New password"/>
          <TextField type="password" name="repeatedNewPassword" placeholder="Repeated new password"/>
          <input type="submit" value="update password" className="button-primary" disabled={isSubmitting}/>
        </Form>
      </Formik>
    </div>
  );
};

export default PasswordDetails;
