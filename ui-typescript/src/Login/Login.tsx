import React, { useCallback, useState } from 'react';
import { Link, Redirect } from 'react-router-dom';
import { login } from "../UserService/UserService";
import { Form, Formik, useField } from "formik";
import * as Yup from 'yup';

type FormValues = {
  loginOrEmail: string;
  password: string;
}

const initialValues: FormValues = { loginOrEmail: '', password: '' };

const validationSchema = Yup.object<FormValues>({
  loginOrEmail: Yup.string()
    .required('Login or Email Required'),
  password: Yup.string()
    .required('Password Required'),
});

type Props = {
  isLoggedIn: boolean;
  onLoggedIn: (apiKey: string) => void;
  notifyError: (msg: string) => void;
}

const Login: React.FC<Props> = (props) => {
  const { notifyError, isLoggedIn, onLoggedIn } = props;
  const [isSubmitting, setSubmitting] = useState(false);

  const handleSubmit = useCallback(async ({ loginOrEmail, password }) => {
    setSubmitting(true);
    (await login({ loginOrEmail, password })).fold({
      left: (error: Error) => {
        notifyError('Incorrect login/email or password!');
        console.error(error);
      },
      right: ({ apiKey }) => onLoggedIn(apiKey)
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
    isLoggedIn ? <Redirect to="/main"/>
      : <div className="Login">
        <h4>Please sign in</h4>
        <Formik initialValues={initialValues}
                onSubmit={handleSubmit}
                validationSchema={validationSchema}>
          <Form className="CommonForm">
            <TextField type="text" name="loginOrEmail" placeholder="Login or Email"/>
            <TextField type="password" name="password" placeholder="Password"/>
            <Link to="/recover-lost-password">Forgot password?</Link>
            <input type="submit" value="Sign in" className="button-primary" disabled={isSubmitting}/>
          </Form>
        </Formik>
      </div>
  );
};

export default Login;
