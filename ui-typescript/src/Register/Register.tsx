import React, { useCallback, useState } from 'react';
import { Link, Redirect } from 'react-router-dom';
import { validateEmail, validateLogin, validatePassword } from '../validation/validation';
import { Form, Formik, useField } from "formik";
import * as Yup from "yup";

type FormValues = {
  login: string;
  email: string;
  password: string;
  repeatedPassword: string;
}

type Props = {
  notifyError: (msg: string) => void;
  notifySuccess: (msg: string) => void;
}

const validationSchema = Yup.object<FormValues>({
  login: Yup.string()
    .required('Login required')
    .min(3, 'Login must be at least 3 characters long'),
  email: Yup.string()
    .required('E-mail required')
    .email('E-mail must be correct'),
  password: Yup.string()
    .required('Password Required'),
  repeatedPassword: Yup.string()
    .required('Repeated password required')
    .when("password", {
      is: (value: string) => !!(value && value.length > 0),
      then: Yup.string().oneOf(
        [Yup.ref("password")],
        "Passwords don't match!"
      )
    }),
});

const initialValues = { login: '', email: '', password: '', repeatedPassword: '' };

const Register: React.FC<Props> = (props) => {
  const { notifyError, notifySuccess } = props;

  const [values, setValues] = useState(initialValues);
  const [isRegistered, setRegistered] = useState(false);
  const [isSubmitting, setSubmitting] = useState(false);

  const handleSubmit = useCallback(async () => {
    // event.preventDefault();
    try {
      //const { data: response } = await this.props.userService.registerUser(values);
      //console.log(response.apiKey);
      // TODO save the apiKey in localStorage; read it in the UserService/axios request transformer?
      // remvoe it from localStorage on logout
      setRegistered(true);
      notifySuccess('Successfully registered.');
    } catch (error) {
      props.notifyError('Could not register new user!');
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
    isRegistered ? <Redirect to="/login"/>
      : <div className="Register">
        <h4>Please sign up</h4>
        <Formik initialValues={initialValues}
                onSubmit={handleSubmit}
                validationSchema={validationSchema}>
          {({ dirty }) =>
            <Form className="CommonForm">
              <TextField type="text" name="login" placeholder="Login"/>
              <TextField type="text" name="email" placeholder="Email"/>
              <TextField type="password" name="password" placeholder="Password"/>
              <TextField type="password" name="repeatedPassword" placeholder="Repeated password"/>
              <Link to="/recover-lost-password">Forgot password?</Link>
              <input type="submit" value="Sign in" className="button-primary" disabled={!dirty || isSubmitting}/>
            </Form>
          }
        </Formik>
      </div>
  );
};

export default Register;
