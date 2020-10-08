import React from "react";
import versionService from "../VersionService/VersionService";
import "./Footer.scss";

const Footer: React.FC = () => {
  const [version, setVersion] = React.useState("");

  React.useEffect(() => {
    const fetchVersion = async () => {
      try {
        const { buildDate, buildSha } = await versionService.getVersion();
        setVersion(`${buildDate}, ${buildSha}`);
      } catch (error) {
        console.error(error);
      }
    };
    fetchVersion();
  }, [setVersion]);

  return (
    <div className="Footer">
      <p>
        Bootzooka - application scaffolding by <a href="http://softwaremill.com">SoftwareMill</a>, sources available on{" "}
        <a href="https://github.com/softwaremill/bootzooka/">GitHub</a>
      </p>
      <p>{version}</p>
    </div>
  );
};

export default Footer;
