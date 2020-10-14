import React from "react";
import Alert from "react-bootstrap/Alert";
import Spinner from "react-bootstrap/Spinner";
import { BsExclamationCircle, BsCheck } from "react-icons/bs";
import { PromiseResultShape } from "react-use-promise-matcher";
import useFormikValuesChanged from "./useFormikValuesChanged";

interface FormFeedbackProps {
  result: PromiseResultShape<any, any>;
  clear: () => void;
}

const FormFeedback: React.FC<FormFeedbackProps> = ({ result, clear }) => {
  useFormikValuesChanged(() => {
    !result.isIdle && clear();
  });

  return result.match({
    Idle: () => <></>,
    Loading: () => (
      <Alert className="my-3" variant="info">
        <Spinner as="span" className="mr-2" animation="border" size="sm" role="loader" />
        Connecting...
      </Alert>
    ),
    Rejected: (error) => (
      <Alert className="my-3" variant="danger">
        <BsExclamationCircle className="mr-2" />
        {error.toString()}
      </Alert>
    ),
    Resolved: () => (
      <Alert className="my-3" variant="success">
        <BsCheck className="mr-2" />
        Done.
      </Alert>
    ),
  });
};

export default FormFeedback;
