import * as Yup from 'yup';

export const validationSchema = Yup.object({
  loginOrEmail: Yup.string().required('Required'),
});
