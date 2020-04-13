import React, { useCallback, useState } from 'react';
import { Link, Redirect } from 'react-router-dom';
import * as queryString from 'query-string';
import PasswordService from '../PasswordService/PasswordService';
import { Form, Formik, useField } from "formik";
import * as Yup from "yup";

type FormValues = {
  newPassword: string;
  repeatedPassword: string;
}

const validationSchema = Yup.object<FormValues>({
  newPassword: Yup.string()
    .required('New password Required')
    .min(5, "New password must be at least 5 characters long"),
  repeatedPassword: Yup.string()
    .required('Repeated password required')
    .when("newPassword", {
      is: (value: string) => !!(value && value.length > 0),
      then: Yup.string().oneOf(
        [Yup.ref("newPassword")],
        "Passwords don't match!"
      )
    }),
});

type Props = {
  notifyError: (msg: string) => void;
  notifySuccess: (msg: string) => void;
  queryParamsString: string;
}

const PasswordReset: React.FC<Props> = (props) => {
  const { notifySuccess, notifyError, queryParamsString } = props;
  const [passwordChanged, setPasswordChanged] = useState(false);
  const [isSubmitting, setSubmitting] = useState(false);

  const handleSubmit = useCallback(async (event) => {
    const { code } = queryString.parse(queryParamsString);

    setSubmitting(true);
    (await PasswordService.resetPassword("", event.newPassword)).fold({
      left: (error: Error) => {
        notifyError('Could not change password!');
        console.error(error);
      },
      right: () => {
        notifySuccess('Password changed!');
        setPasswordChanged(true);
      }
    });
    setSubmitting(false);
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
    passwordChanged ? <Redirect to="/login"/>
      : <div className="PasswordReset">
        <h4>Reset password</h4>
        <Formik initialValues={{ newPassword: '', repeatedPassword: '' }}
                onSubmit={handleSubmit}
                validationSchema={validationSchema}>
          {({ dirty }) => (
            <Form className="CommonForm">
              <TextField type="password" name="currentPassword" placeholder="Current password"/>
              <TextField type="password" name="newPassword" placeholder="New password"/>
              <TextField type="password" name="repeatedNewPassword" placeholder="Repeated new password"/>
              <Link to="/recover-lost-password">Forgot password?</Link>
              <input type="submit" value="Sign in" className="button-primary" disabled={!dirty || isSubmitting}/>
            </Form>
          )}
        </Formik>
      </div>
  );
};

export default PasswordReset;
