import { useEffect, useRef } from 'react';
import { useFormikContext } from 'formik';

export const useFormikValuesChanged = (onChange: () => void) => {
  const { values } = useFormikContext();
  const onChangeRef = useRef(onChange);

  useEffect(() => {
    onChangeRef.current = onChange;
  }, [onChange]);

  useEffect(() => {
    onChangeRef.current();
  }, [values]);
};
