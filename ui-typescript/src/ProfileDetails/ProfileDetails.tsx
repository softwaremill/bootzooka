import React, { useCallback, useState } from 'react';
import { changeProfileDetails } from '../UserService/UserService';
import { User } from '../types/Types';
import { Form, Formik, useField } from 'formik';
import { Link } from 'react-router-dom';
import * as Yup from 'yup';

type FormValues = {
  login: string;
  email: string;
}

type Props = {
  user: User;
  notifyError: (msg: string) => void;
  notifySuccess: (msg: string) => void;
  onUserUpdated: (user: User) => void;
  apiKey: string;
}

const validationSchema = Yup.object<FormValues>({
  login: Yup.string()
    .required('Login required'),
  email: Yup.string()
    .required('Email required'),
});

const ProfileDetails: React.FC<Props> = (props) => {
  const { notifyError, notifySuccess, onUserUpdated, apiKey, user } = props;
  const [login, setLogin] = useState(user.login);
  const [email, setEmail] = useState(user.email);
  const [isSubmitting, setSubmitting] = useState(false);

  const handleSubmit = useCallback(async () => {
    (await changeProfileDetails(apiKey, { login, email })).fold({
      left: (error: Error) => {
        notifyError('Could not change profile details!');
        console.error(error);
      },
      right: () => {
        onUserUpdated({ email, login });
        notifySuccess('Profile details changed!');
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
    <div className="ProfileDetails">
      <h4>Profile details</h4>
      <Formik initialValues={{ login, email }}
              onSubmit={handleSubmit}
              validationSchema={validationSchema}>
        {({ dirty }) => (
          <Form className="CommonForm">
            <TextField type="text" name="loginOrEmail" placeholder="Login or Email"/>
            <TextField type="password" name="password" placeholder="Password"/>
            <Link to="/recover-lost-password">Forgot password?</Link>
            <input type="submit" value="Sign in" className="button-primary" disabled={!dirty || isSubmitting}/>
          </Form>
        )}
      </Formik>
    </div>
  );
};

export default ProfileDetails;
