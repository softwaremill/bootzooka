import React from 'react';

const Welcome = () =>
  <div className="Welcome">
    <h3>Hi there! Welcome to Bootzooka!</h3>
    <p>In this template application you can register as a new user, log in and later manage your user details.</p>
    <p>If you are interested in how Bootzooka works, you can <a href="http://softwaremill.github.io/bootzooka/" target="blank">browse the documentation</a>,
      or the <a href="https://github.com/softwaremill/bootzooka" target="blank">source code</a>.
    </p>
    <div className="Welcome__footer">
      <h4>brought to you by</h4>
      <a href="http://softwaremill.com" rel="noopener noreferrer" target="_blank"><img src="sml_2.png" className="sml-logo" alt="SoftwareMill"/></a>
    </div>
  </div>;

export default Welcome;
