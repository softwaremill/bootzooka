interface ErrorMessageProps {
  error: any;
}

export const ErrorMessage: React.FC<ErrorMessageProps> = ({ error }) => (
  <span className="text-danger">
    {(
      error?.response?.data?.error ||
      error?.message ||
      'Unknown error'
    ).toString()}
  </span>
);
