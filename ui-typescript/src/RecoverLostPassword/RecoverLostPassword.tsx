import React, { useCallback, useState } from 'react';
import { Link, Redirect } from 'react-router-dom';
import { Form, Formik, useField } from "formik";
import * as Yup from "yup";

type Props = {
  notifySuccess: (msg: string) => void;
  notifyError: (msg: string) => void;
}

const validationSchema = Yup.object<{ loginOrEmail: string }>({
  loginOrEmail: Yup.string()
    .required('Email or login required')
});
const RecoverLostPassword: React.FC<Props> = props => {
  const { notifySuccess, notifyError } = props;
  const [loginOrEmail, setLoginOrEmail] = useState('');
  const [resetComplete, setResetComplete] = useState(false);
  const [isSubmitting, setSubmitting] = useState(false);

  const handleSubmit = useCallback(async () => {
    //event.preventDefault();
    try {
      setSubmitting(true);
      //await passwordService.claimPasswordReset({ loginOrEmail });
      setResetComplete(true);
      setSubmitting(false);
      notifySuccess('Password reset claim success.');
    } catch (error) {
      notifyError('Could not claim password reset!');
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
    resetComplete ? <Redirect to="/login"/>
      : <div className="RecoverLostPassword">
        <Formik initialValues={{ loginOrEmail: '' }}
                onSubmit={handleSubmit}
                validationSchema={validationSchema}>
          {({ dirty }) =>
            <Form className="CommonForm">
              <TextField type="text" name="loginOrEmail" placeholder="Email or login"/>
              <input type="submit" value="Sign in" className="button-primary" disabled={!dirty || isSubmitting}/>
            </Form>
          }
        </Formik>
        <form className="CommonForm">
          <input type="text" name="loginOrEmail" placeholder="Email address or login"/>
          {loginOrEmail && loginOrEmail.length < 1 ?
            <p className="validation-message">login or email address is required!</p> : null}
          <input type="submit" value="Reset password" className="button-primary"/>
        </form>
      </div>
  );
};

export default RecoverLostPassword;
