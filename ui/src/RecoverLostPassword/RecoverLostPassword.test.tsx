import React from 'react';
import { shallow } from 'enzyme';
import RecoverLostPassword from './RecoverLostPassword';

const passwordService = jest.mock('../PasswordService/PasswordService');

const notifyError = jest.fn();
const notifySuccess = jest.fn();

describe('structure', () => {
  it('should contain login input', () => {
    const wrapper = shallow(<RecoverLostPassword passwordService={passwordService}
      notifyError={notifyError} notifySuccess={notifySuccess} />);
    expect(wrapper.find('input[name="loginOrEmail"]').length).toBe(1);
  });

  it('should contain reset password button', () => {
    const wrapper = shallow(<RecoverLostPassword passwordService={passwordService}
      notifyError={notifyError} notifySuccess={notifySuccess} />);
    expect(wrapper.find('input[type="submit"]').length).toBe(1);
  });
});

describe('behaviour', () => {
  it('reset password button should initially be disabled', () => {
    const wrapper = shallow(<RecoverLostPassword passwordService={passwordService}
      notifyError={notifyError} notifySuccess={notifySuccess} />);
    const resetPasswordButton = wrapper.find('input[type="submit"]');
    expect(resetPasswordButton.props().disabled).toBe(true);
  });

  it('an error should appear under empty login input on blur', () => {
    const wrapper = shallow(<RecoverLostPassword passwordService={passwordService}
      notifyError={notifyError} notifySuccess={notifySuccess} />);
    const loginInput = wrapper.find('input[name="loginOrEmail"]');
    loginInput.simulate('blur');
    expect(wrapper.contains(<p className="validation-message" key={0}>login or email address is required!</p>)).toBe(true);
  });

  it('should enable reset password button when login input is valid', () => {
    const wrapper = shallow(<RecoverLostPassword passwordService={passwordService}
      notifyError={notifyError} notifySuccess={notifySuccess} />);
    const initialState = wrapper.state();
    wrapper.setState({ ...initialState, values: { loginOrEmail: 'mickey' } });
    const resetPasswordButton = wrapper.find('input[type="submit"]');
    expect(resetPasswordButton.props().disabled).toBe(false);
  });
});
