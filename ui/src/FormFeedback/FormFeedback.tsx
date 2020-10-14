import React from "react";
import Form from "react-bootstrap/Form";
import Spinner from "react-bootstrap/Spinner";
import { BsExclamationCircle, BsCheck } from "react-icons/bs";
import {  PromiseResultShape } from "react-use-promise-matcher";

interface FormFeedbackProps {
  result: PromiseResultShape<any, any>;
}

const FormFeedback: React.FC<FormFeedbackProps> = ({ result, ...buttonProps }) =>
  result.match({
    Idle: () => <></>,
    Loading: () => (
      <Form.Text muted>
        <Spinner as="span" className="mr-2" animation="border" size="sm" role="loader" />
        Connecting...
      </Form.Text>
    ),
    Rejected: (error) => (
      <Form.Text className="text-danger">
        <BsExclamationCircle className="mr-2" />
        {error.toString()}
      </Form.Text>
    ),
    Resolved: () => (
      <Form.Text className="text-success">
        <BsCheck className="mr-2" />
        Done.
      </Form.Text>
    ),
  });

export default FormFeedback;
