const EMAIL_REGEXP = /^(([^<>()[\].,;:\s@"]+(\.[^<>()[\].,;:\s@"]+)*)|(".+"))@(([^<>()[\].,;:\s@"]+.)+[^<>()[\].,;:\s@"]{2,})$/i;

export function validateEmail(email) {
  const errors = [];

  if (!EMAIL_REGEXP.test(email)) {
    errors.push('this doesn\'t look like a valid email');
  }

  return errors;
}

export function validatePassword(password) {
  const errors = [];

  if (password.length < 5) {
    errors.push('at least 5 characters required!')
  }

  return errors;
}

export function validateLogin(login) {
  const errors = [];

  if (login.length < 3) {
    errors.push('at least 3 characters required!');
  }

  return errors;
}
