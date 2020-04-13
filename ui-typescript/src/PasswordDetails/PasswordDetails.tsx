import React, { useCallback, useState } from 'react';
import { Form, Formik, useField } from "formik";
import { Link } from "react-router-dom";
import * as Yup from "yup";

type FormValues = {
  currentPassword: string;
  newPassword: string;
  repeatedNewPassword: string;
}

type Props = {
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
  const { notifyError, notifySuccess } = props;

  const [isSubmitting, setSubmitting] = useState(false);
  const [values, setValues] = useState<FormValues>(initialValues);

  const handleSubmit = useCallback(async event => {
    event.preventDefault();
    try {
      const { currentPassword, newPassword } = values;
      //await changePassword(this.props.apiKey, { currentPassword, newPassword });
      notifySuccess('Password changed!');
      setValues({ currentPassword: '', newPassword: '', repeatedNewPassword: '' });
    } catch (error) {
      notifyError('Could not change password!');
      console.error(error);
    }
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

export default PasswordDetails;
