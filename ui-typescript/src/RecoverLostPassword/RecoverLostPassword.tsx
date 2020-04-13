import React, { useCallback, useState } from 'react';
import { Redirect } from 'react-router-dom';
import { Form, Formik, useField } from "formik";
import * as Yup from "yup";
import PasswordService from "../PasswordService/PasswordService";

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
    setSubmitting(true);
    (await PasswordService.claimPasswordReset(loginOrEmail)).fold({
      left: (error: Error) => {
        notifyError('Could not claim password reset!');
        console.error(error);
      },
      right: () => {
        notifySuccess('Password reset claim success.');
        setResetComplete(true);
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
    resetComplete ? <Redirect to="/login"/>
      : <div className="RecoverLostPassword">
        <Formik initialValues={{ loginOrEmail: '' }}
                onSubmit={handleSubmit}
                validationSchema={validationSchema}>
          <Form className="CommonForm">
            <TextField type="text" name="loginOrEmail" placeholder="Email or login"/>
            <input type="submit" value="Sign in" className="button-primary" disabled={isSubmitting}/>
          </Form>
        </Formik>
      </div>
  );
};

export default RecoverLostPassword;
