import { Link } from 'react-router';
import Form from 'react-bootstrap/Form';
import { BiLogInCircle } from 'react-icons/bi';
import { Formik, Form as FormikForm } from 'formik';
import * as Yup from 'yup';
import { TwoColumnHero, FormikInput, FeedbackButton } from 'components';
import { validationSchema } from './Login.validations';
import { useApiKeyState } from 'hooks/auth';
import { useGetUser, usePostUserLogin } from 'api/apiComponents';
import { useEffect } from 'react';
import { useUserContext } from 'contexts/UserContext/User.context';

export type LoginParams = Yup.InferType<typeof validationSchema>;

export const Login = () => {
  const [apiKeyState, setApiKeyState] = useApiKeyState();

  const { dispatch } = useUserContext();

  const apiKey = apiKeyState?.apiKey;

  const mutation = usePostUserLogin({
    onSuccess: ({ apiKey }) => {
      setApiKeyState({ apiKey });
    },
  });

  const { data: user, isSuccess } = useGetUser(
    apiKey ? { headers: { Authorization: `Bearer ${apiKey}` } } : {},
    {
      enabled: Boolean(apiKey),
      retry: false,
    }
  );

  useEffect(() => {
    if (isSuccess) {
      dispatch({ type: 'LOG_IN', user });
    }
  }, [user, dispatch, isSuccess]);

  return (
    <TwoColumnHero>
      <h3 className="mb-4">Please sign in</h3>
      <Formik<LoginParams>
        initialValues={{ loginOrEmail: '', password: '' }}
        onSubmit={(values) => mutation.mutateAsync({ body: values })}
        validationSchema={validationSchema}
      >
        <Form className="w-75" as={FormikForm}>
          <FormikInput name="loginOrEmail" label="Login or email" />
          <FormikInput name="password" type="password" label="Password" />
          <div className="d-flex justify-content-between align-items-center">
            <Link className="text-muted" to="/recover-lost-password">
              Forgot password?
            </Link>
            <FeedbackButton
              className="float-end"
              type="submit"
              label="Sign In"
              variant="dark"
              Icon={BiLogInCircle}
              mutation={mutation}
            />
          </div>
        </Form>
      </Formik>
    </TwoColumnHero>
  );
};
