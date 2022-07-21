import React from "react";
import Form from "react-bootstrap/Form";
import { BiReset } from "react-icons/bi";
import { usePromise } from "react-use-promise-matcher";
import { Formik, Form as FormikForm } from "formik";
import * as Yup from "yup";
import { passwordService } from "services";
import { TwoColumnHero, FormikInput, FeedbackButton } from "components";

const validationSchema = Yup.object({
  loginOrEmail: Yup.string().required("Required"),
});

type RecoverLostPasswordParams = Yup.InferType<typeof validationSchema>;

export const RecoverLostPassword: React.FC = () => {
  const [result, send, clear] = usePromise((values: RecoverLostPasswordParams) =>
    passwordService.claimPasswordReset(values)
  );

  return (
    <TwoColumnHero>
      <h3 className="mb-4">Recover lost password</h3>
      <Formik<RecoverLostPasswordParams>
        initialValues={{
          loginOrEmail: "",
        }}
        onSubmit={send}
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
            result={result}
            clear={clear}
            successLabel="Password reset claim success"
          />
        </Form>
      </Formik>
    </TwoColumnHero>
  );
};
