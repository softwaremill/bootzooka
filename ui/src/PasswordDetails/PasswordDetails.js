import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { validatePassword } from '../validation/validation';

class PasswordDetails extends Component {
  constructor(props) {
    super(props);
    this.state = {
      values: {
        currentPassword: '',
        newPassword: '',
        repeatedNewPassword: ''
      },
      touchedControls: {
        currentPassword: false,
        newPassword: false,
        repeatedNewPassword: false
      },
    };

    this.handleSubmit = this.handleSubmit.bind(this);
  }

  async handleSubmit(event) {
    event.preventDefault();
    try {
      const { currentPassword, newPassword } = this.state.values;
      await this.props.userService.changePassword(this.props.apiKey, { currentPassword, newPassword });
      this.props.notifySuccess('Password changed!');
      this.setState({
        values: {
          currentPassword: '',
          newPassword: '',
          repeatedNewPassword: ''
        },
        touchedControls: {
          currentPassword: false,
          newPassword: false,
          repeatedNewPassword: false
        },
      });
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
    const { currentPassword, newPassword, repeatedNewPassword } = this.state.values;
    return currentPassword.length > 0
    && validatePassword(newPassword).length === 0
    && validatePassword(repeatedNewPassword).length === 0
    && this.passwordEntriesMatch();
  }

  render() {
    const { currentPassword, newPassword, repeatedNewPassword } = this.state.values;
    return (
      <div className="PasswordDetails">
        <h4>Password details</h4>
        <form className="CommonForm" onSubmit={this.handleSubmit.bind(this)}>
          <input type="password" name="currentPassword" placeholder="Current password" value={currentPassword}
            onChange={({ target }) => this.handleValueChange('currentPassword', target.value)}
            onBlur={() => this.handleBlur('currentPassword')} />
          { this.state.touchedControls.currentPassword && this.state.values.currentPassword.length < 1 ? <p className="validation-message">current password is required!</p> : null }
          <input type="password" name="newPassword" placeholder="New password" value={newPassword}
            onChange={({ target }) => this.handleValueChange('newPassword', target.value)}
            onBlur={() => this.handleBlur('newPassword')} />
          { this.getPasswordErrors('newPassword').map((errorMsg, idx) => <p className="validation-message" key={idx}>{errorMsg}</p>) }
          <input type="password" name="repeatedNewPassword" placeholder="Repeat new password" value={repeatedNewPassword}
            onChange={({ target }) => this.handleValueChange('repeatedNewPassword', target.value)}
            onBlur={() => this.handleBlur('repeatedNewPassword')} />
          { this.getPasswordErrors('repeatedNewPassword').map((errorMsg, idx) => <p className="validation-message" key={idx}>{errorMsg}</p>) }
          { this.state.touchedControls.repeatedNewPassword && !this.passwordEntriesMatch() ? <p className="validation-message">passwords don't match!</p> : null }
          <input type="submit" value="Update password" className="button-primary" disabled={!this.isValid()} />
        </form>
      </div>
    );
  }
}

PasswordDetails.propTypes = {
  apiKey: PropTypes.string,
  userService: PropTypes.shape({
    changePassword: PropTypes.func.isRequired
  }).isRequired,
  notifyError: PropTypes.func.isRequired,
  notifySuccess: PropTypes.func.isRequired,
};

export default PasswordDetails;
