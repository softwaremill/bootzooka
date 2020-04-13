import React from 'react';
import { Either } from 'ts-matches';
import './Footer.scss';
import { Version } from "../types/Types";

type Props = {
  version: Either<Error, Version>;
}

const Footer: React.FC<Props> = (props) => {
  return (
    <div className="Footer">
      <p>Bootzooka - application scaffolding by <span><a href="http://softwaremill.com">SoftwareMill</a></span>,
        sources available on <span><a href="https://github.com/softwaremill/bootzooka/">GitHub</a></span>
      </p>
      {props.version.fold({
        left: () => <p>App version: unknown</p>,
        right: version => <p>App version: {version.buildDate}, {version.buildSha}</p>
      })}
    </div>
  );
};

export default Footer;
