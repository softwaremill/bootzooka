import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { validateEmail, validateLogin } from '../validation/validation';
import { serviceProp } from '../utils/utils';
import UserService from '../UserService/UserService';

class ProfileDetails extends Component {
  constructor(props) {
    super(props);
    const { email, login } = this.props.user;
    this.state = {
      values: {
        login,
        email,
      },
      touchedControls: {
        login: false,
        email: false,
      },
    };

    this.handleSubmit = this.handleSubmit.bind(this);
  }

  async handleSubmit(event) {
    event.preventDefault();
    try {
      const { email, login } = this.state.values;
      await this.props.userService.changeProfileDetails(this.props.apiKey, { login, email });
      this.props.onUserUpdated({ email, login });
      this.props.notifySuccess('Profile details changed!');
    } catch (error) {
      this.props.notifyError('Could not change profile details!');
      console.error(error);
    }
  }

  handleValueChange(key, value) {
    this.setState(state => ({ ...state, values: { ...state.values, [key]: value } }));
  }

  handleBlur(inputName) {
    this.setState(state => ({ ...state, touchedControls: { ...state.touchedControls, [inputName]: true } }));
  }

  getLoginErrors() {
    return this.state.touchedControls.login ? validateLogin(this.state.values.login) : [];
  }

  getEmailErrors() {
    return this.state.touchedControls.email ? validateEmail(this.state.values.email) : [];
  }

  isValid() {
    const { email, login } = this.state.values;
    return validateLogin(login).length === 0 && validateEmail(email).length === 0;
  }

  render() {
    const { email, login } = this.state.values;
    return (
      <div className="ProfileDetails">
        <h4>Profile details</h4>
        <form className="CommonForm" onSubmit={this.handleSubmit}>
          <input type="text" name="login" placeholder="Login" value={login}
            onChange={({ target }) => this.handleValueChange('login', target.value)}
            onBlur={() => this.handleBlur('login')} />
          { this.getLoginErrors().map((errorMsg, idx) => <p className="validation-message" key={idx}>{errorMsg}</p>) }
          <input type="email" name="email" placeholder="Email address" value={email}
            onChange={({ target }) => this.handleValueChange('email', target.value)}
            onBlur={() => this.handleBlur('email')} />
          { this.getEmailErrors().map((errorMsg, idx) => <p className="validation-message" key={idx}>{errorMsg}</p>) }
          <input type="submit" value="Update profile data" className="button-primary" disabled={!this.isValid()} />
        </form>
      </div>
    );
  }
}

ProfileDetails.propTypes = {
  apiKey: PropTypes.string,
  userService: serviceProp(UserService),
  user: PropTypes.shape({
    login: PropTypes.string.isRequired,
    email: PropTypes.string.isRequired,
  }),
  onUserUpdated: PropTypes.func.isRequired,
  notifyError: PropTypes.func.isRequired,
  notifySuccess: PropTypes.func.isRequired,
};

export default ProfileDetails;
