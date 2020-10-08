import React, { Component } from 'react';
import { Link, Redirect } from 'react-router-dom';
import PropTypes from 'prop-types';

class Login extends Component {
  constructor(props) {
    super(props);
    this.state = {
      values: {
        loginOrEmail: '',
        password: '',
      },
      touchedControls: {
        loginOrEmail: false,
        password: false,
      },
    };
  }

  handleSubmit = async event => {
    event.preventDefault();
    try {
      const { loginOrEmail, password } = this.state.values;
      const { data } = await this.props.userService.login({ loginOrEmail, password });
      await this.props.onLoggedIn(data.apiKey);
    } catch (error) {
      this.props.notifyError('Incorrect login/email or password!');
      console.error(error);
    }
  }

  handleValueChange(key, value) {
    this.setState(state => ({ ...state, values: { ...state.values, [key]: value } }));
  }

  handleBlur(inputName) {
    this.setState(state => ({ ...state, touchedControls: { ...state.touchedControls, [inputName]: true } }));
  }

  isValid() {
    const { loginOrEmail, password } = this.state.values;
    return loginOrEmail.length > 0 && password.length > 0;
  }

  render() {
    return (
      this.props.isLoggedIn ? <Redirect to="/main" />
      :  <div className="Login">
          <h4>Please sign in</h4>
          <form className="CommonForm" onSubmit={this.handleSubmit}>
            <input type="text" name="loginOrEmail" placeholder="Login or email"
              onChange={({ target }) => this.handleValueChange('loginOrEmail', target.value)}
              onBlur={() => this.handleBlur('loginOrEmail')} />
            { this.state.touchedControls.loginOrEmail && this.state.values.loginOrEmail.length < 1 ? <p className="validation-message">login/email is required!</p> : null }
            <input type="password" name="password" placeholder="Password"
              onChange={({ target }) => this.handleValueChange('password', target.value)}
              onBlur={() => this.handleBlur('password')} />
            { this.state.touchedControls.password && this.state.values.password.length < 1 ? <p className="validation-message">password is required!</p> : null }
            <Link to="/recover-lost-password">Forgot password?</Link>
            <input type="submit" value="Sign in" className="button-primary" disabled={!this.isValid()} />
          </form>
        </div>
    );
  }
}

Login.propTypes = {
  isLoggedIn: PropTypes.bool,
  notifyError: PropTypes.func.isRequired,
  userService: PropTypes.shape({
    login: PropTypes.func.isRequired
  }).isRequired,
};

export default Login;
