import { useEffect } from 'react';
import Form from 'react-bootstrap/Form';
import Container from 'react-bootstrap/Container';
import Col from 'react-bootstrap/Col';
import Row from 'react-bootstrap/Row';
import { BiArrowFromBottom } from 'react-icons/bi';
import { Formik, Form as FormikForm } from 'formik';
import * as Yup from 'yup';
import { FormikInput, FeedbackButton } from 'components';
import { usePostUser } from 'api/apiComponents';
import { validationSchema } from './ProfileDetails.validations';
import { useUserContext } from 'contexts/UserContext/User.context';

export type ProfileDetailsParams = Yup.InferType<typeof validationSchema>;

export const ProfileDetails = () => {
  const {
    dispatch,
    state: { user },
  } = useUserContext();

  const mutation = usePostUser();
  const { data, isSuccess } = mutation;

  useEffect(() => {
    if (isSuccess) {
      dispatch({ type: 'UPDATE_USER_DATA', user: data });
    }
  }, [isSuccess, dispatch, data]);

  return (
    <Container className="py-5">
      <Row>
        <Col md={9} lg={7} xl={6} className="mx-auto">
          {user ? (
            <>
              <h3 className="mb-4">Profile details</h3>
              <Formik<ProfileDetailsParams>
                initialValues={{
                  login: user.login || '',
                  email: user.email || '',
                }}
                onSubmit={(values) => mutation.mutate({ body: values })}
                validationSchema={validationSchema}
              >
                <Form as={FormikForm}>
                  <FormikInput name="login" label="Login" />
                  <FormikInput name="email" label="Email address" />

                  <FeedbackButton
                    className="float-end"
                    type="submit"
                    label="Update profile data"
                    variant="dark"
                    Icon={BiArrowFromBottom}
                    mutation={mutation}
                    successLabel="Profile details changed"
                  />
                </Form>
              </Formik>
            </>
          ) : (
            <h3 className="mb-4">Profile details not available.</h3>
          )}
        </Col>
      </Row>
    </Container>
  );
};
