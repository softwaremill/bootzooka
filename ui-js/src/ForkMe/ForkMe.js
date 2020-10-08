import React from 'react';

const ForkMe = ({ children }) =>
  <div className="ForkMe">
    <a href="https://github.com/softwaremill/bootzooka" target="_blank" rel="noopener noreferrer">
      <img src="forkme_orange.png" className="forkMeBadge" alt="fork me" />
    </a>
    { children }
  </div>;

const withForkMe = component => <ForkMe>{ component }</ForkMe>;

export default withForkMe;
