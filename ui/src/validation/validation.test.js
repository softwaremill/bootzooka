import { validateEmail, validatePassword, validateLogin } from './validation';

describe('validateEmail', () => {
  it('should return an empty list when email is valid', () => {
    const email = 'test@test.com';
    const validationResult = validateEmail(email);
    expect(validationResult.length).toBe(0);
  });

  it('should return a list containing error message when email is invalid', () => {
    const email = 'totallyNotAnEmail';
    const validationResult = validateEmail(email);
    expect(validationResult.length).toBe(1);
    expect(validationResult[0]).toEqual('this doesn\'t look like a valid email');
  });
});

describe('validatePassword', () => {
  it('should return an empty list when password is valid', () => {
    const password = 'P4sSW0Rd#1';
    const validationResult = validatePassword(password);
    expect(validationResult.length).toBe(0);
  });

  it('should return a list containing error message when password is too short', () => {
    const password = 'abc';
    const validationResult = validatePassword(password);
    expect(validationResult.length).toBe(1);
    expect(validationResult[0]).toEqual('at least 5 characters required!');
  });
});

describe('validateLogin', () => {
  it('should return an empty list when login is valid', () => {
    const login = 'JFK';
    const validationResult = validateLogin(login);
    expect(validationResult.length).toBe(0);
  });

  it('should return a list containing error message when login is too short', () => {
    const login = 'MU';
    const validationResult = validateLogin(login);
    expect(validationResult.length).toBe(1);
    expect(validationResult[0]).toEqual('at least 3 characters required!');
  });
});
