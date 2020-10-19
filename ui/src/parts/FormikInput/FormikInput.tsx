import React from "react";
import { Field, FieldProps } from "formik";
import Form from "react-bootstrap/Form";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";

interface FormikInputProps {
  type?: string;
  name: string;
  label: string;
}

const FormikInput: React.FC<FormikInputProps> = ({ type = "text", name, label }) => (
  <Field name={name}>
    {({ field, meta }: FieldProps<string>) => (
      <Form.Group as={Row}>
        <Form.Label column sm={3} htmlFor={name}>
          {label}
        </Form.Label>
        <Col sm={9}>
          <Form.Control
            id={name}
            type={type}
            isValid={!meta.error && meta.touched}
            isInvalid={!!meta.error && meta.touched}
            {...field}
          />
          <Form.Control.Feedback type="invalid" className="text-right">
            {meta.error}
          </Form.Control.Feedback>
        </Col>
      </Form.Group>
    )}
  </Field>
);

export default FormikInput;
