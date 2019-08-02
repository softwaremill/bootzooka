import PropTypes from 'prop-types';

export const serviceProp = (serviceClass) =>
  process.env.NODE_ENV === "test" ? PropTypes.object.isRequired : PropTypes.instanceOf(serviceClass).isRequired;
