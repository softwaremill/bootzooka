import type { FC } from 'react';
import logo from 'assets/sml-logo-vertical-rgb-trans.png';

export const Welcome: FC = () => (
  <div className="w-full h-full flex flex-col justify-center items-center">
    <div className="lg:w-full h-[50%] flex flex-col items-center justify-center bg-foreground text-background">
      <h3 className="text-xl mb-2">Hi there!</h3>
      <h1 className="text-3xl mb-3">Welcome to Bootzooka!</h1>
      <p>
        If you are interested in how Bootzooka works,
        <br />
        you can browse the{' '}
        <a href="http://softwaremill.github.io/bootzooka/" target="blank">
          Documentation
        </a>{' '}
        or{' '}
        <a href="https://github.com/softwaremill/bootzooka" target="blank">
          Source code
        </a>
        .
      </p>
    </div>
    <div className="lg:w-full h-[50%] flex flex-col items-center justify-center text-center">
      <p className="fs-3">Brought to you by</p>
      <a
        href="http://softwaremill.com"
        rel="noopener noreferrer"
        target="_blank"
      >
        <img
          src={logo}
          className="w-[300px] h-auto"
          alt="SoftwareMill"
          width="500"
        />
      </a>
    </div>
  </div>
);
