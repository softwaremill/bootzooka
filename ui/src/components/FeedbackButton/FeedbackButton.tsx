import React from "react";
import Button, { ButtonProps } from "react-bootstrap/Button";
import Spinner from "react-bootstrap/Spinner";
import Form from "react-bootstrap/Form";
import { IconType } from "react-icons";
import { BsExclamationCircle, BsCheck } from "react-icons/bs";
import useFormikValuesChanged from "./useFormikValuesChanged";
import { ErrorMessage } from "../";
import { UseMutationResult } from "react-query";

interface FeedbackButtonProps<TMutationData> extends ButtonProps {
  label: string;
  Icon: IconType;
  mutation: UseMutationResult<TMutationData, any, any, any>;
  successLabel?: string;
}

export const FeedbackButton = <TMutationData,>({
  mutation,
  label,
  Icon,
  successLabel = "Success",
  ...buttonProps
}: FeedbackButtonProps<TMutationData>): React.ReactElement => {
  useFormikValuesChanged(() => {
    !mutation.isIdle && mutation.reset();
  });

  if (mutation.isLoading) {
    return (
      <Button {...buttonProps} disabled>
        <Spinner as="span" animation="border" size="sm" role="loader" />
        &nbsp;{label}
      </Button>
    );
  }

  if (mutation.isError) {
    return (
      <div>
        <Button {...buttonProps} variant="danger">
          <BsExclamationCircle role="error" />
          &nbsp;{label}
        </Button>
        <Form.Text className="d-inline-block mx-3">
          <ErrorMessage error={mutation.error} />
        </Form.Text>
      </div>
    );
  }

  if (mutation.isSuccess) {
    return (
      <div>
        <Button {...buttonProps} variant="success">
          <BsCheck role="success" />
          &nbsp;{label}
        </Button>
        <Form.Text className="text-success d-inline-block mx-3">{successLabel}</Form.Text>
      </div>
    );
  }

  return (
    <Button {...buttonProps}>
      <Icon />
      &nbsp;{label}
    </Button>
  );
};
