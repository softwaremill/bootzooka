import React, { Component } from 'react';
import { Redirect } from 'react-router-dom';
import PropTypes from 'prop-types';
import { validatePassword } from '../validation/validation';
import * as queryString from 'query-string';
import PasswordService from '../PasswordService/PasswordService';
import { serviceProp } from '../utils/utils';

class PasswordReset extends Component {
  constructor(props) {
    super(props);
    this.state = {
      values: {
        newPassword: '',
        repeatedNewPassword: ''
      },
      touchedControls: {
        newPassword: false,
        repeatedNewPassword: false
      },
      passwordChanged: false
    };

    this.handleSubmit = this.handleSubmit.bind(this);
  }

  async handleSubmit(event) {
    event.preventDefault();
    const { code } = queryString.parse(this.props.queryParamsString);
    try {
      const { newPassword: password } = this.state.values;
      await this.props.passwordService.resetPassword({ code, password });
      this.props.notifySuccess('Password changed!');
      this.setState({ passwordChanged: true });
    } catch (error) {
      this.props.notifyError('Could not change password!');
      console.error(error);
    }
  }

  handleValueChange(key, value) {
    this.setState(state => ({ ...state, values: { ...state.values, [key]: value } }));
  }

  handleBlur(inputName) {
    this.setState(state => ({ ...state, touchedControls: { ...state.touchedControls, [inputName]: true } }));
  }

  getPasswordErrors(inputName) {
    return this.state.touchedControls[inputName] ? validatePassword(this.state.values[inputName]) : [];
  }

  passwordEntriesMatch() {
    return this.state.values.newPassword === this.state.values.repeatedNewPassword;
  }

  isValid() {
    const { newPassword, repeatedNewPassword } = this.state.values;
    return validatePassword(newPassword).length === 0
    && validatePassword(repeatedNewPassword).length === 0
    && this.passwordEntriesMatch();
  }

  render() {
    const { newPassword, repeatedNewPassword } = this.state.values;
    return (
      this.state.passwordChanged ? <Redirect to="/login" />
      : <div className="PasswordReset">
          <h4>Reset password</h4>
          <form className="CommonForm" onSubmit={this.handleSubmit.bind(this)}>
            <input type="password" name="newPassword" placeholder="New password" value={newPassword}
              onChange={({ target }) => this.handleValueChange('newPassword', target.value)}
              onBlur={() => this.handleBlur('newPassword')} />
            { this.getPasswordErrors('newPassword').map((errorMsg, idx) => <p className="validation-message" key={idx}>{errorMsg}</p>) }
            <input type="password" name="repeatedNewPassword" placeholder="Repeat new password" value={repeatedNewPassword}
              onChange={({ target }) => this.handleValueChange('repeatedNewPassword', target.value)}
              onBlur={() => this.handleBlur('repeatedNewPassword')} />
            { this.getPasswordErrors('repeatedNewPassword').map((errorMsg, idx) => <p className="validation-message" key={idx}>{errorMsg}</p>) }
            { this.state.touchedControls.repeatedNewPassword && !this.passwordEntriesMatch() ? <p className="validation-message">passwords don't match!</p> : null }
            <input type="submit" value="Reset password" className="button-primary" disabled={!this.isValid()} />
          </form>
        </div>
    );
  }
}

PasswordReset.propTypes = {
  passwordService: serviceProp(PasswordService),
  queryParamsString: PropTypes.string.isRequired,
};

export default PasswordReset;
