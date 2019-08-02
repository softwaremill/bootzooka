import React from 'react';
import ReactDOM from 'react-dom';
import { BrowserRouter } from 'react-router-dom';
import './index.css';
import App from './App';
import PasswordService from './PasswordService/PasswordService';
import UserService from './UserService/UserService';
import VersionService from './VersionService/VersionService';

const userService = new UserService();
const passwordService = new PasswordService();
const versionService = new VersionService();

ReactDOM.render(
  <BrowserRouter>
    <App
      passwordService={passwordService}
      userService={userService}
      versionService={versionService}
    />
  </BrowserRouter>,
  document.getElementById('root')
);
