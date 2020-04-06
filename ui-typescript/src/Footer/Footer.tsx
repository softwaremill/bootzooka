import React, { useEffect, useState } from 'react';
import { Either } from "ts-matches";

type Props = {
  version: Either<Error, string>;
}

const Footer: React.FC<Props> = (props) => {
  const [version, setVersion] = useState<Either<Error, string>>(props.version);


  useEffect(() => {
    setVersion(version);
  });

  return (
    <div className="Footer">
      <p>Bootzooka - application scaffolding by <span><a href="http://softwaremill.com">SoftwareMill</a></span>,
        sources available on <span><a href="https://github.com/softwaremill/bootzooka/">GitHub</a></span>
      </p>
        {version.fold({
          left: e => <p>Version: {e.message}</p>,
          right: v => <p>Version: {v}</p>
        })}
    </div>
  );
};

export default Footer;
