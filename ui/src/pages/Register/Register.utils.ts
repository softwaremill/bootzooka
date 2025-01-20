import * as Yup from 'yup';
import { validationSchema } from './Register.validations';

export const initialValues = {
  login: '',
  email: '',
  password: '',
  repeatedPassword: '',
};

export type RegisterParams = Yup.InferType<typeof validationSchema>;
