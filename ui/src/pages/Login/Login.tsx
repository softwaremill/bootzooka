import React, { useContext, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import Form from "react-bootstrap/Form";
import { BiLogInCircle } from "react-icons/bi";
import { Formik, Form as FormikForm } from "formik";
import * as Yup from "yup";
import { UserContext } from "contexts";
import { TwoColumnHero, FormikInput, FeedbackButton } from "components";
import { useMutation } from "@tanstack/react-query";
import {usePostUserLogin} from "../../api/apiComponents";

const validationSchema = Yup.object({
  loginOrEmail: Yup.string().required("Required"),
  password: Yup.string().required("Required"),
});

export type LoginParams = Yup.InferType<typeof validationSchema>;

type Props = {};

export const Login: React.FC<Props> = ({}) => {
  const {
    dispatch,
    state: { loggedIn },
  } = useContext(UserContext);

  const navigate = useNavigate();

  const mutation = usePostUserLogin();
  const login = mutation.mutateAsync;


  useEffect(() => {
    if (loggedIn) navigate("/main");
  }, [loggedIn, navigate]);

  return (
    <TwoColumnHero>
      <h3 className="mb-4">Please sign in</h3>
      <Formik<LoginParams>
        initialValues={{
          loginOrEmail: "",
          password: "",
        }}
        onSubmit={(values) => login({body: values})}
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
