import Form from 'react-bootstrap/Form';
import { BiUserPlus } from 'react-icons/bi';
import { Formik, Form as FormikForm } from 'formik';
import { TwoColumnHero, FormikInput, FeedbackButton } from 'components';
import { usePostUserRegister } from 'api/apiComponents';
import { validationSchema } from './Register.validations';
import { initialValues, RegisterParams } from './Register.utils';
import { useApiKeyState } from 'hooks/auth';

export const Register = () => {
  const [, setApiKeyState] = useApiKeyState();

  const mutation = usePostUserRegister({
    onSuccess: ({ apiKey }) => {
      setApiKeyState({ apiKey });
    },
  });

  return (
    <TwoColumnHero>
      <h3 className="mb-4">Please sign up</h3>
      <Formik<RegisterParams>
        initialValues={initialValues}
        onSubmit={(values) => mutation.mutate({ body: values })}
        validationSchema={validationSchema}
      >
        <Form className="w-75" as={FormikForm}>
          <FormikInput name="login" label="Login" />
          <FormikInput name="email" label="Email address" />
          <FormikInput name="password" label="Password" type="password" />
          <FormikInput
            name="repeatedPassword"
            label="Repeat password"
            type="password"
          />

          <FeedbackButton
            className="float-end"
            type="submit"
            label="Create new account"
            variant="dark"
            Icon={BiUserPlus}
            mutation={mutation}
          />
        </Form>
      </Formik>
    </TwoColumnHero>
  );
};
