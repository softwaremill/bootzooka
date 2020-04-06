import * as React from 'react';
import './ForkMe.scss';

const ForkMe: React.FC = props =>
  <div className="ForkMe">
    <a href="https://github.com/softwaremill/bootzooka" target="_blank" rel="noopener noreferrer">
      <img src="forkme_orange.png" className="forkMeBadge" alt="fork me" />
    </a>
    { props.children }
  </div>;

const withForkMe = (component: JSX.Element) => <ForkMe>{ component }</ForkMe>;

export default withForkMe;
