import React from 'react';
import { shallow } from 'enzyme';
import PasswordReset from './PasswordReset';

const resetPassword = jest.fn();
resetPassword.mockReturnValue(Promise.resolve());

const notifyError = jest.fn();
const notifySuccess = jest.fn();

const passwordService = {
  resetPassword
};

const queryParamsString = '?code=blabla';

describe('structure', () => {
  it('should contain newPassword input', () => {
    const wrapper = shallow(<PasswordReset queryParamsString={queryParamsString} passwordService={passwordService} notifyError={notifyError} notifySuccess={notifySuccess} />);
    const newPasswordInput = wrapper.find('input[name="newPassword"]');
    expect(newPasswordInput.length).toBe(1);
  });

  it('should contain repeatedNewPassword input', () => {
    const wrapper = shallow(<PasswordReset queryParamsString={queryParamsString} passwordService={passwordService} notifyError={notifyError} notifySuccess={notifySuccess} />);
    const repeatedNewPasswordInput = wrapper.find('input[name="repeatedNewPassword"]');
    expect(repeatedNewPasswordInput.length).toBe(1);
  });

  it('should contain reset button', () => {
    const wrapper = shallow(<PasswordReset queryParamsString={queryParamsString} passwordService={passwordService} notifyError={notifyError} notifySuccess={notifySuccess} />);
    const resetButton = wrapper.find('input[type="submit"]');
    expect(resetButton.length).toBe(1);
    expect(resetButton.props().disabled).toBe(true);
  });
});

describe('behaviour', () => {
  it('an error should appear under empty newPassword input on blur', () => {
    const wrapper = shallow(<PasswordReset queryParamsString={queryParamsString} passwordService={passwordService} notifyError={notifyError} notifySuccess={notifySuccess} />);
    const newPasswordInput = wrapper.find('input[name="newPassword"]');
    newPasswordInput.simulate('blur');
    expect(wrapper.contains(<p className="validation-message" key={0}>at least 5 characters required!</p>)).toBe(true);
  });

  it('an error should appear under empty repeatedNewPassword input on blur', () => {
    const wrapper = shallow(<PasswordReset queryParamsString={queryParamsString} passwordService={passwordService} notifyError={notifyError} notifySuccess={notifySuccess} />);
    const repeatedNewPasswordInput = wrapper.find('input[name="repeatedNewPassword"]');
    repeatedNewPasswordInput.simulate('blur');
    expect(wrapper.contains(<p className="validation-message" key={0}>at least 5 characters required!</p>)).toBe(true);
  });

  it('an error should appear under repeated password when passwords do not match and repeatedNewPassword input was touched', () => {
    const wrapper = shallow(<PasswordReset queryParamsString={queryParamsString} passwordService={passwordService} notifyError={notifyError} notifySuccess={notifySuccess} />);
    const initialState = wrapper.state();
    wrapper.setState({ touchedControls: { ...initialState.touchedControls, repeatedNewPassword: true}, values: { ...initialState.values, newPassword: 'abcde', repeatedNewPassword: 'abcdefgh' } });
    expect(wrapper.contains(<p className="validation-message" key={0}>passwords don't match!</p>)).toBe(true);
  });

  it('should enable update button when all inputs are correct', () => {
    const wrapper = shallow(<PasswordReset queryParamsString={queryParamsString} passwordService={passwordService} notifyError={notifyError} notifySuccess={notifySuccess} />);
    const initialState = wrapper.state();
    wrapper.setState({ ...initialState, values: { newPassword: 'P4sSW0Rd#1', repeatedNewPassword: 'P4sSW0Rd#1' } });
    const updateButton = wrapper.find('input[type="submit"]');
    expect(updateButton.props().disabled).toBe(false);
  });

  it('should call userService.resetPassword fn when form is submitted', async () => {
    const wrapper = shallow(<PasswordReset queryParamsString={queryParamsString} passwordService={passwordService} notifyError={notifyError} notifySuccess={notifySuccess} />);
    const initialState = wrapper.state();
    wrapper.setState({ ...initialState, values: { newPassword: 'P4sSW0Rd#1', repeatedNewPassword: 'P4sSW0Rd#1' } });
    const form = wrapper.find('form');
    await form.simulate('submit', { preventDefault: jest.fn() });
    expect(resetPassword.mock.calls.length).toBe(1);
  });
});
