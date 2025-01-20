import Form from 'react-bootstrap/Form';
import { BiReset } from 'react-icons/bi';
import { Formik, Form as FormikForm } from 'formik';
import * as Yup from 'yup';
import { TwoColumnHero, FormikInput, FeedbackButton } from 'components';
import { usePostPasswordresetForgot } from 'api/apiComponents';
import { validationSchema } from './RecoverLostPassword.validations';

export type RecoverLostPasswordParams = Yup.InferType<typeof validationSchema>;

export const RecoverLostPassword = () => {
  const mutation = usePostPasswordresetForgot();

  return (
    <TwoColumnHero>
      <h3 className="mb-4">Recover lost password</h3>
      <Formik<RecoverLostPasswordParams>
        initialValues={{
          loginOrEmail: '',
        }}
        onSubmit={(values) => mutation.mutate({ body: values })}
        validationSchema={validationSchema}
      >
        <Form className="w-75" as={FormikForm}>
          <FormikInput name="loginOrEmail" label="Login or email" />
          <FeedbackButton
            className="float-end"
            type="submit"
            label="Reset password"
            variant="dark"
            Icon={BiReset}
            mutation={mutation}
            successLabel="Password reset claim success"
          />
        </Form>
      </Formik>
    </TwoColumnHero>
  );
};
