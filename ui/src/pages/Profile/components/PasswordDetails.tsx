import Form from 'react-bootstrap/Form';
import Container from 'react-bootstrap/Container';
import Col from 'react-bootstrap/Col';
import Row from 'react-bootstrap/Row';
import { BiArrowFromBottom } from 'react-icons/bi';
import { Formik, Form as FormikForm } from 'formik';
import * as Yup from 'yup';
import { FormikInput, FeedbackButton } from 'components';
import { usePostUserChangepassword } from 'api/apiComponents';
import { validationSchema } from './PasswordDetails.validations';
import { useApiKeyState } from 'hooks/auth';

type PasswordDetailsParams = Yup.InferType<typeof validationSchema>;

export const PasswordDetails = () => {
  const [storageApiKeyState, setStorageApiKeyState] = useApiKeyState();

  const mutation = usePostUserChangepassword({
    onSuccess: ({ apiKey: newApiKey }) => {
      setStorageApiKeyState({ apiKey: newApiKey });
    },
  });

  const apiKey = storageApiKeyState?.apiKey;

  return (
    <Container className="py-5">
      <Row>
        <Col md={9} lg={7} xl={6} className="mx-auto">
          {apiKey ? (
            <>
              <h3 className="mb-4">Password details</h3>
              <Formik<PasswordDetailsParams>
                initialValues={{
                  currentPassword: '',
                  newPassword: '',
                  repeatedPassword: '',
                }}
                onSubmit={(values) =>
                  mutation.mutate({
                    body: values,
                  })
                }
                validationSchema={validationSchema}
              >
                <Form as={FormikForm}>
                  <FormikInput
                    name="currentPassword"
                    label="Current password"
                    type="password"
                  />
                  <FormikInput
                    name="newPassword"
                    label="New password"
                    type="password"
                  />
                  <FormikInput
                    name="repeatedPassword"
                    label="Repeat new password"
                    type="password"
                  />

                  <FeedbackButton
                    className="float-end"
                    type="submit"
                    label="Update password"
                    variant="dark"
                    Icon={BiArrowFromBottom}
                    mutation={mutation}
                    successLabel="Password changed"
                  />
                </Form>
              </Formik>
            </>
          ) : (
            <h3 className="mb-4">Password details not available.</h3>
          )}
        </Col>
      </Row>
    </Container>
  );
};
