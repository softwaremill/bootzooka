import * as Yup from 'yup';

export const validationSchema = Yup.object({
  currentPassword: Yup.string()
    .min(3, 'At least 3 characters required')
    .required('Required'),
  newPassword: Yup.string()
    .min(3, 'At least 3 characters required')
    .required('Required'),
  repeatedPassword: Yup.string()
    .oneOf([Yup.ref('newPassword')], 'Passwords must match')
    .required('Required'),
});
