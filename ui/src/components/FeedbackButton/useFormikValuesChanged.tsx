import React from "react";
import { useFormikContext } from "formik";

const useFormikValuesChanged = (onChange: () => void) => {
  const { values } = useFormikContext();
  const onChangeRef = React.useRef(onChange);

  React.useEffect(() => {
    onChangeRef.current = onChange;
  }, [onChange]);

  React.useEffect(() => {
    onChangeRef.current();
  }, [values]);
};

export default useFormikValuesChanged;
