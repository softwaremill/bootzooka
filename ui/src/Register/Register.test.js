import React from 'react';
import { shallow } from 'enzyme';
import Register from './Register';

const userService = {
  registerUser: jest.fn()
};

const notifyError = jest.fn();
const notifySuccess = jest.fn();

describe('structure', () => {
  it('should contain login input', () => {
    const wrapper = shallow(<Register userService={userService}
      notifyError={notifyError} notifySuccess={notifySuccess} />);
    expect(wrapper.find('input[name="login"]').length).toBe(1);
  });

  it('should contain email input', () => {
    const wrapper = shallow(<Register userService={userService}
      notifyError={notifyError} notifySuccess={notifySuccess} />);
    expect(wrapper.find('input[name="email"]').length).toBe(1);
  });

  it('should contain password input', () => {
    const wrapper = shallow(<Register userService={userService}
      notifyError={notifyError} notifySuccess={notifySuccess} />);
    expect(wrapper.find('input[name="password"]').length).toBe(1);
  });

  it('should contain repeatedPassword input', () => {
    const wrapper = shallow(<Register userService={userService}
      notifyError={notifyError} notifySuccess={notifySuccess} />);
    expect(wrapper.find('input[name="repeatedPassword"]').length).toBe(1);
  });

  it('should contain register button', () => {
    const wrapper = shallow(<Register userService={userService}
      notifyError={notifyError} notifySuccess={notifySuccess} />);
    expect(wrapper.find('input[type="submit"]').length).toBe(1);
  });
});

describe('behaviour', () => {
  it('register button should initially be disabled', () => {
    const wrapper = shallow(<Register userService={userService}
      notifyError={notifyError} notifySuccess={notifySuccess} />);
    const registerButton = wrapper.find('input[type="submit"]');
    expect(registerButton.props().disabled).toBe(true);
  });

  it('an error should appear under empty login input on blur', () => {
    const wrapper = shallow(<Register userService={userService}
      notifyError={notifyError} notifySuccess={notifySuccess} />);
    const loginInput = wrapper.find('input[name="login"]');
    loginInput.simulate('blur');
    expect(wrapper.contains(<p className="validation-message" key={0}>at least 3 characters required!</p>)).toBe(true);
  });

  it('an error should appear under empty password input on blur', () => {
    const wrapper = shallow(<Register userService={userService}
      notifyError={notifyError} notifySuccess={notifySuccess} />);
    const passwordInput = wrapper.find('input[name="password"]');
    passwordInput.simulate('blur');
    expect(wrapper.contains(<p className="validation-message" key={0}>at least 5 characters required!</p>)).toBe(true);
  });

  it('an error should appear under empty repeated password input on blur', () => {
    const wrapper = shallow(<Register userService={userService}
      notifyError={notifyError} notifySuccess={notifySuccess} />);
    const repeatedPasswordInput = wrapper.find('input[name="repeatedPassword"]');
    repeatedPasswordInput.simulate('blur');
    expect(wrapper.contains(<p className="validation-message" key={0}>at least 5 characters required!</p>)).toBe(true);
  });

  it('an error should appear under repeated password when passwords do not match and repeatedPassword input was touched', () => {
    const wrapper = shallow(<Register userService={userService}
      notifyError={notifyError} notifySuccess={notifySuccess} />);
    const initialState = wrapper.state();
    wrapper.setState({ touchedControls: { ...initialState.touchedControls, repeatedPassword: true}, values: { ...initialState.values, password: 'abcde', repeatedPassword: 'abcdefgh' } });
    expect(wrapper.contains(<p className="validation-message" key={0}>passwords don't match!</p>)).toBe(true);
  });

  it('should enable register button when all inputs are correct', () => {
    const wrapper = shallow(<Register userService={userService}
      notifyError={notifyError} notifySuccess={notifySuccess} />);
    const initialState = wrapper.state();
    wrapper.setState({ ...initialState, values: { login: 'mickey', email: 'mickey@mou.se', password: 'P4sSW0Rd#1', repeatedPassword: 'P4sSW0Rd#1' } });
    const registerButton = wrapper.find('input[type="submit"]');
    expect(registerButton.props().disabled).toBe(false);
  });
});
