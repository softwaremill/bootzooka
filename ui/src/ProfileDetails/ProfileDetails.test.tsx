import React from 'react';
import { shallow } from 'enzyme';
import ProfileDetails from './ProfileDetails';

const changeProfileDetails = jest.fn();
changeProfileDetails.mockReturnValue(Promise.resolve());

const userService = {
  changeProfileDetails
};

const notifyError = jest.fn();
const notifySuccess = jest.fn();

const mockUser = {
  email: 'dude@du.de',
  login: 'dude'
};

describe('structure', () => {
  it('should contain login input with a value of user\'s login', () => {
    const wrapper = shallow(<ProfileDetails userService={userService} user={mockUser} onUserUpdated={jest.fn()}
      notifyError={notifyError} notifySuccess={notifySuccess} />);
    const loginInput = wrapper.find('input[name="login"]');
    expect(loginInput.length).toBe(1);
    expect(loginInput.props().value).toEqual(mockUser.login);
  });

  it('should contain email input with a value of user\'s email', () => {
    const wrapper = shallow(<ProfileDetails userService={userService} user={mockUser} onUserUpdated={jest.fn()}
      notifyError={notifyError} notifySuccess={notifySuccess} />);
    const emailInput = wrapper.find('input[name="email"]');
    expect(emailInput.length).toBe(1);
    expect(emailInput.props().value).toEqual(mockUser.email);
  });

  it('should contain update button', () => {
    const wrapper = shallow(<ProfileDetails userService={userService} user={mockUser} onUserUpdated={jest.fn()}
      notifyError={notifyError} notifySuccess={notifySuccess} />);
    const updateButton = wrapper.find('input[type="submit"]');
    expect(updateButton.length).toBe(1);
    expect(updateButton.props().disabled).toBe(false);
  });
});

describe('behaviour', () => {
  it('an error should appear under empty login input on blur', () => {
    const wrapper = shallow(<ProfileDetails userService={userService} user={{ email: 'dude@du.de', login: '' }} onUserUpdated={jest.fn()}
      notifyError={notifyError} notifySuccess={notifySuccess} />);
    const loginInput = wrapper.find('input[name="login"]');
    loginInput.simulate('blur');
    expect(wrapper.contains(<p className="validation-message" key={0}>at least 3 characters required!</p>)).toBe(true);
  });

  it('an error should appear under empty email input on blur', () => {
    const wrapper = shallow(<ProfileDetails userService={userService} user={{ email: '', login: 'dude' }} onUserUpdated={jest.fn()}
      notifyError={notifyError} notifySuccess={notifySuccess} />);
    const emailInput = wrapper.find('input[name="email"]');
    emailInput.simulate('blur');
    expect(wrapper.contains(<p className="validation-message" key={0}>this doesn't look like a valid email</p>)).toBe(true);
  });

  it('update button should be disabled when login is incorrect', () => {
    const wrapper = shallow(<ProfileDetails userService={userService} user={{ email: 'dude@du.de', login: '' }} onUserUpdated={jest.fn()}
      notifyError={notifyError} notifySuccess={notifySuccess} />);
    const updateButton = wrapper.find('input[type="submit"]');
    expect(updateButton.props().disabled).toBe(true);
  });

  it('update button should be disabled when email is incorrect', () => {
    const wrapper = shallow(<ProfileDetails userService={userService} user={{ email: '', login: 'dude' }} onUserUpdated={jest.fn()}
      notifyError={notifyError} notifySuccess={notifySuccess} />);
    const updateButton = wrapper.find('input[type="submit"]');
    expect(updateButton.props().disabled).toBe(true);
  });

  it('should call onUserUpdated fn when update button is clicked', async () => {
    const onUserUpdated = jest.fn();
    const wrapper = shallow(<ProfileDetails userService={userService} user={mockUser} onUserUpdated={onUserUpdated}
      notifyError={notifyError} notifySuccess={notifySuccess} />);
    const form = wrapper.find('form');
    await form.simulate('submit', { preventDefault: jest.fn() });
    expect(onUserUpdated.mock.calls.length).toBe(1);
  });
});
