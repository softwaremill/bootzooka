import * as Yup from 'yup';

export const validationSchema = Yup.object({
  login: Yup.string()
    .min(3, 'At least 3 characters required')
    .required('Required'),
  email: Yup.string()
    .email('Valid email address required')
    .required('Required'),
  password: Yup.string()
    .min(5, 'At least 5 characters required')
    .required('Required'),
  repeatedPassword: Yup.string()
    .oneOf([Yup.ref('password')], 'Passwords must match')
    .required('Required'),
});
