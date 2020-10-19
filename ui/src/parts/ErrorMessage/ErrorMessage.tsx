import React from "react";

interface ErrorMessageProps {
  error: any;
}

const ErrorMessage: React.FC<ErrorMessageProps> = ({ error }) => (
  <span className="text-danger">{(error?.response?.data?.error || error?.message || "Unknown error").toString()}</span>
);

export default ErrorMessage;
