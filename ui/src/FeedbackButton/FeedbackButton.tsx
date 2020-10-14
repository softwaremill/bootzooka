import React from "react";
import Button, { ButtonProps } from "react-bootstrap/Button";
import Spinner from "react-bootstrap/Spinner";
import OverlayTrigger from "react-bootstrap/OverlayTrigger";
import Form from "react-bootstrap/Form";
import { IconType } from "react-icons";
import { BsExclamationCircle, BsCheck } from "react-icons/bs";
import { PromiseResultShape } from "react-use-promise-matcher";
import useFormikValuesChanged from "./useFormikValuesChanged";

interface FeedbackButtonProps extends ButtonProps {
  label: string;
  Icon: IconType;
  result: PromiseResultShape<any, any>;
  clear: () => void;
}

const FeedbackButton: React.FC<FeedbackButtonProps> = ({ result, clear, label, Icon, ...buttonProps }) => {
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
        <Form.Text className="text-danger">{error.message}</Form.Text>
      </>
    ),
    Resolved: () => (
      <Button {...buttonProps} disabled variant="success">
        <BsCheck role="success" />
        &nbsp;{label}
      </Button>
    ),
  });
};

export default FeedbackButton;
