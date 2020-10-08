import React from "react";
import logo from "./logo.svg";
import versionService from "./VersionService/VersionService";
import Footer from './Footer/Footer';
import "./App.scss";


const App: React.FC = () => {
  versionService.getVersion().then(console.log);
  return (
    <div className="App">
      <Footer />
    </div>
  );
};

export default App;
