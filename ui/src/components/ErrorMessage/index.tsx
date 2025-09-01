interface ErrorMessageProps {
  error: any;
}

export const ErrorMessage: React.FC<ErrorMessageProps> = ({ error }) => (
  <span className="text-sm text-red-500 font-semibold">
    {(
      error?.stack?.error ||
      error?.response?.data?.error ||
      error?.message ||
      error?.error ||
      'Unknown error'
    ).toString()}
  </span>
);
