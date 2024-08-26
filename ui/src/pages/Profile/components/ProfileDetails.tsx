import React from "react";
import Form from "react-bootstrap/Form";
import Container from "react-bootstrap/Container";
import Col from "react-bootstrap/Col";
import Row from "react-bootstrap/Row";
import { BiArrowFromBottom } from "react-icons/bi";
import { Formik, Form as FormikForm } from "formik";
import * as Yup from "yup";
import { UserContext } from "contexts";
import { userService } from "services";
import { FormikInput, FeedbackButton } from "components";
import { useMutation } from "react-query";

const validationSchema = Yup.object({
  login: Yup.string().min(3, "At least 3 characters required").required("Required"),
  email: Yup.string().email("Correct email address required").required("Required"),
});

export type ProfileDetailsParams = Yup.InferType<typeof validationSchema>;

type Props = {
  onChangeProfileDetails(apiKey: string, payload: ProfileDetailsParams): Promise<void>;
};

export const ProfileDetails: React.FC<Props> = ({ onChangeProfileDetails }) => {
  const {
    dispatch,
    state: { apiKey, user },
  } = React.useContext(UserContext);

  const mutation = useMutation(
    ({ values, apiKeyValue }: { values: ProfileDetailsParams; apiKeyValue: string }) =>
      onChangeProfileDetails(apiKeyValue, values),
    {
      onSuccess: (_, { values }) => {
        dispatch({ type: "UPDATE_USER_DATA", user: values });
      },
    },
  );

  return (
    <Container className="py-5">
      <Row>
        <Col md={9} lg={7} xl={6} className="mx-auto">
          {apiKey ? (
            <>
              <h3 className="mb-4">Profile details</h3>
              <Formik<ProfileDetailsParams>
                initialValues={{
                  login: user?.login || "",
                  email: user?.email || "",
                }}
                onSubmit={(values) => mutation.mutate({ values, apiKeyValue: apiKey })}
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
