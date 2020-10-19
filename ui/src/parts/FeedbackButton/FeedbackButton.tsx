import React from "react";
import Button, { ButtonProps } from "react-bootstrap/Button";
import Spinner from "react-bootstrap/Spinner";
import Form from "react-bootstrap/Form";
import { IconType } from "react-icons";
import { BsExclamationCircle, BsCheck } from "react-icons/bs";
import { PromiseResultShape } from "react-use-promise-matcher";
import ErrorMessage from "../ErrorMessage/ErrorMessage";
import useFormikValuesChanged from "./useFormikValuesChanged";

interface FeedbackButtonProps extends ButtonProps {
  label: string;
  Icon: IconType;
  result: PromiseResultShape<any, any>;
  clear: () => void;
  successLabel?: string;
}

const FeedbackButton: React.FC<FeedbackButtonProps> = ({
  result,
  clear,
  label,
  Icon,
  successLabel = "Success",
  ...buttonProps
}) => {
  useFormikValuesChanged(() => {
    !result.isIdle && clear();
  });

  return result.match({
    Idle: () => (
      <Button {...buttonProps}>
        <Icon />
        &nbsp;{label}
      </Button>
    ),
    Loading: () => (
      <Button {...buttonProps} disabled>
        <Spinner as="span" animation="border" size="sm" role="loader" />
        &nbsp;{label}
      </Button>
    ),
    Rejected: (error) => (
      <>
        <Button {...buttonProps} variant="danger">
          <BsExclamationCircle role="error" />
          &nbsp;{label}
        </Button>
        <Form.Text className="d-inline-block mx-3">
          <ErrorMessage error={error} />
        </Form.Text>
      </>
    ),
    Resolved: () => (
      <>
        <Button {...buttonProps} variant="success">
          <BsCheck role="success" />
          &nbsp;{label}
        </Button>
        <Form.Text className="text-success d-inline-block mx-3">{successLabel}</Form.Text>
      </>
    ),
  });
};

export default FeedbackButton;
