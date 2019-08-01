import React from 'react';
import { shallow } from 'enzyme';
import Login from './Login';

const userService = {
  login: jest.fn()
};

const notifyError = jest.fn();

describe('structure', () => {
  it('should contain login input', () => {
    const wrapper = shallow(<Login userService={userService} notifyError={notifyError} />);
    expect(wrapper.find('input[name="loginOrEmail"]').length).toBe(1);
  });

  it('should contain password input', () => {
    const wrapper = shallow(<Login userService={userService} notifyError={notifyError} />);
    expect(wrapper.find('input[name="password"]').length).toBe(1);
  });

  it('should contain sign in button', () => {
    const wrapper = shallow(<Login userService={userService} notifyError={notifyError} />);
    expect(wrapper.find('input[type="submit"]').length).toBe(1);
  });
});

describe('behaviour', () => {
  it('sign in button should initially be disabled', () => {
    const wrapper = shallow(<Login userService={userService} notifyError={notifyError} />);
    const signInButton = wrapper.find('input[type="submit"]');
    expect(signInButton.props().disabled).toBe(true);
  });

  it('an error should appear under empty login input on blur', () => {
    const wrapper = shallow(<Login userService={userService} notifyError={notifyError} />);
    const loginInput = wrapper.find('input[name="loginOrEmail"]');
    loginInput.simulate('blur');
    expect(wrapper.contains(<p className="validation-message" key={0}>login/email is required!</p>)).toBe(true);
  });

  it('an error should appear under empty password input on blur', () => {
    const wrapper = shallow(<Login userService={userService} notifyError={notifyError} />);
    const passwordInput = wrapper.find('input[name="password"]');
    passwordInput.simulate('blur');
    expect(wrapper.contains(<p className="validation-message" key={0}>password is required!</p>)).toBe(true);
  });

  it('should enable sign in button when all inputs are correct', () => {
    const wrapper = shallow(<Login userService={userService} notifyError={notifyError} />);
    const initialState = wrapper.state();
    wrapper.setState({ ...initialState, values: { loginOrEmail: 'mickey', password: 'P4sSW0Rd#1' } });
    const signInButton = wrapper.find('input[type="submit"]');
    expect(signInButton.props().disabled).toBe(false);
  });
});
