import React, { useCallback, useState } from 'react';
import { Link, Redirect } from 'react-router-dom';
import { login } from "../UserService/UserService";

type Props = {
  isLoggedIn: boolean;
  onLoggedIn: (apiKey: string) => void;
  notifyError: (msg: string) => void;
}

const Login: React.FC<Props> = (props) => {
  const { notifyError, isLoggedIn, onLoggedIn } = props;

  const [values, setValues] = useState({ loginOrEmail: '', password: '' });
  const [touchedControls, setTouchedControls] = useState({ loginOrEmail: false, password: false });

  const handleSubmit = useCallback(async event => {
    event.preventDefault();
    try {
      const { data } = await login({ loginOrEmail: values.loginOrEmail, password: values.password });
      await onLoggedIn(data.apiKey);
    } catch (error) {
      notifyError('Incorrect login/email or password!');
      console.error(error);
    }
  }, [values]);

  const handleValueChange = (key: string, value: string) => {
    setValues({ ...values, [key]: value });
  };

  const handleBlur = (inputName: string) => {
    setTouchedControls({ ...touchedControls, [inputName]: true })
  };

  const isValid = () => values.loginOrEmail.length > 0 && values.password.length > 0;

  return (
    isLoggedIn ? <Redirect to="/main"/>
      : <div className="Login">
        <h4>Please sign in</h4>
        <form className="CommonForm" onSubmit={handleSubmit}>
          <input type="text" name="loginOrEmail" placeholder="Login or email"
                 onChange={({ target }) => handleValueChange('loginOrEmail', target.value)}
                 onBlur={() => handleBlur('loginOrEmail')}/>
          {touchedControls.loginOrEmail && values.loginOrEmail.length < 1 ?
            <p className="validation-message">login/email is required!</p> : null}
          <input type="password" name="password" placeholder="Password"
                 onChange={({ target }) => handleValueChange('password', target.value)}
                 onBlur={() => handleBlur('password')}/>
          {touchedControls.password && values.password.length < 1 ?
            <p className="validation-message">password is required!</p> : null}
          <Link to="/recover-lost-password">Forgot password?</Link>
          <input type="submit" value="Sign in" className="button-primary" disabled={!isValid()}/>
        </form>
      </div>
  );
};

export default Login;
