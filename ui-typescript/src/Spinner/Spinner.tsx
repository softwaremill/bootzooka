import React from 'react';
import './Spinner.scss';

const Spinner: React.FC = () =>
  <div className="Spinner">
    <img src="loading.gif" alt="loading" />
  </div>;

export default Spinner;
