import * as Yup from 'yup';

export const validationSchema = Yup.object({
  login: Yup.string()
    .min(3, 'At least 3 characters required')
    .required('Required'),
  email: Yup.string()
    .email('Valid email address required')
    .required('Required'),
});
