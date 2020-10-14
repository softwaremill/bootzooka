import React from "react";
import { Field, FieldProps } from "formik";
import Form from "react-bootstrap/Form";

interface FormikInputProps {
  type?: string;
  name: string;
  label: string;
}

const FormikInput: React.FC<FormikInputProps> = ({ type = "text", name, label }) => (
  <Field name={name}>
    {({ field, meta }: FieldProps<string>) => (
      <Form.Group>
        <Form.Label htmlFor={name}>{label}</Form.Label>
        <Form.Control
          id={name}
          type={type}
          isValid={!meta.error && meta.touched}
          isInvalid={!!meta.error && meta.touched}
          {...field}
        />
        <Form.Control.Feedback type="invalid">{meta.error}</Form.Control.Feedback>
      </Form.Group>
    )}
  </Field>
);

export default FormikInput;
