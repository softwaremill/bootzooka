import { useContext, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Form from "react-bootstrap/Form";
import { BiUserPlus } from "react-icons/bi";
import { Formik, Form as FormikForm } from "formik";
import * as Yup from "yup";
import { UserContext } from "contexts";
import { TwoColumnHero, FormikInput, FeedbackButton } from "components";
import { usePostUserRegister } from "api/apiComponents";

const validationSchema = Yup.object({
  login: Yup.string().min(3, "At least 3 characters required").required("Required"),
  email: Yup.string().email("Correct email address required").required("Required"),
  password: Yup.string().min(5, "At least 5 characters required").required("Required"),
  repeatedPassword: Yup.string()
    .oneOf([Yup.ref("password")], "Passwords must match")
    .required("Required"),
});

type RegisterParams = Yup.InferType<typeof validationSchema>;

export const Register = () => {
  const {
    dispatch,
    state: { loggedIn },
  } = useContext(UserContext);

  const navigate = useNavigate();

  const mutation = usePostUserRegister();
  const { isSuccess, data } = mutation;

  useEffect(() => {
    if (isSuccess) {
      const { apiKey } = data;
      dispatch({ type: "SET_API_KEY", apiKey });
    }
  }, [isSuccess, dispatch, data]);

  useEffect(() => {
    if (loggedIn) navigate("/main");
  }, [loggedIn, navigate]);

  return (
    <TwoColumnHero>
      <h3 className="mb-4">Please sign up</h3>
      <Formik<RegisterParams>
        initialValues={{
          login: "",
          email: "",
          password: "",
          repeatedPassword: "",
        }}
        onSubmit={(values) => mutation.mutate({ body: values })}
        validationSchema={validationSchema}
      >
        <Form className="w-75" as={FormikForm}>
          <FormikInput name="login" label="Login" />
          <FormikInput name="email" label="Email address" />
          <FormikInput name="password" label="Password" type="password" />
          <FormikInput name="repeatedPassword" label="Repeat password" type="password" />

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
