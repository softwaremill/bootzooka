import React, { Component } from 'react';
import { Link, Redirect } from 'react-router-dom';
import PropTypes from 'prop-types';

class Login extends Component {
  constructor(props) {
    super(props);
    this.state = {
      values: {
        login: '',
        password: '',
        rememberMe: false,
      },
      touchedControls: {
        login: false,
        password: false,
      },
      isLoggedIn: false,
    };

    this.handleSubmit = this.handleSubmit.bind(this);
  }

  async handleSubmit(event) {
    event.preventDefault();
    try {
      const { login, password, rememberMe } = this.state.values;
      const { data: userData } = await this.props.userService.login({ login, password, rememberMe });
      this.props.onLoggedIn(userData);
      this.setState({ isLoggedIn: true });
    } catch (error) {
      this.props.notifyError('Incorrect login or password!');
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
    const { login, password } = this.state.values;
    return login.length > 0 && password.length > 0;
  }

  render() {
    return (
      this.state.isLoggedIn ? <Redirect to="/main" />
      :  <div className="Login">
          <h4>Please sign in</h4>
          <form className="CommonForm" onSubmit={this.handleSubmit}>
            <input type="text" name="login" placeholder="Login"
              onChange={({ target }) => this.handleValueChange('login', target.value)}
              onBlur={() => this.handleBlur('login')} />
            { this.state.touchedControls.login && this.state.values.login.length < 1 ? <p className="validation-message">login is required!</p> : null }
            <input type="password" name="password" placeholder="Password"
              onChange={({ target }) => this.handleValueChange('password', target.value)}
              onBlur={() => this.handleBlur('password')} />
            { this.state.touchedControls.password && this.state.values.password.length < 1 ? <p className="validation-message">password is required!</p> : null }
            <Link to="/recover-lost-password">Forgot password?</Link>
            <label><input type="checkbox" checked={this.state.values.rememberMe} onChange={({ target }) => this.handleValueChange('rememberMe', target.checked)} /> Remember me</label>
            <input type="submit" value="Sign in" className="button-primary" disabled={!this.isValid()} />
          </form>
        </div>
    );
  }
}

Login.propTypes = {
  userService: PropTypes.shape({
    login: PropTypes.func.isRequired
  }).isRequired,
  notifyError: PropTypes.func.isRequired,
};

export default Login;
