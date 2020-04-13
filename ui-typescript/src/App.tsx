import React, { useCallback, useEffect, useState } from 'react';
import { Route, Switch } from 'react-router-dom';
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import Login from './Login/Login';
import NavBar from './NavBar/NavBar';
import NotFound from './NotFound/NotFound';
import ProtectedRoute from './ProtectedRoute/ProtectedRoute';
import RecoverLostPassword from './RecoverLostPassword/RecoverLostPassword';
import Register from './Register/Register';
import Spinner from './Spinner/Spinner';
import Welcome from './Welcome/Welcome';
import withForkMe from './ForkMe/ForkMe';
import SecretMain from './SecretMain/SecretMain';
// import ProfileDetails from './ProfileDetails/ProfileDetails';
// import PasswordDetails from './PasswordDetails/PasswordDetails';
import Footer from './Footer/Footer';
// import PasswordReset from './PasswordReset/PasswordReset';
import PasswordService from './PasswordService/PasswordService';
import { login, registerUser, getCurrentUser } from './UserService/UserService';
import { getAppVersion } from './VersionService/VersionService';
import { User } from './types/Types';
import { Either, Right } from 'ts-matches';

const App: React.FC = () => {
  // const { passwordService, userService, versionService } = props;
  const [apiKey, setApiKey] = useState('');
  const [isLoggedIn, setLoggedIn] = useState(false);
  const [user, setUser] = useState<User>({ email: '', login: '' });
  const [isLoadingAuthInfo, setLoadingAuthInfo] = useState(true);
  const [version, setVersion] = useState<Either<Error, string>>(Right.of(''));

  useEffect(() => {
    (async () => {
      const apiKey = window.localStorage.getItem('apiKey');
      if (apiKey) {
        try {
          const { data: user } = await getCurrentUser(apiKey);
          setUser(user);
          setLoggedIn(true);
          setLoadingAuthInfo(false);
        } catch (err) {
          window.console.error(err);
          window.localStorage.removeItem('apiKey');
          setLoadingAuthInfo(false);
        }
      }
      const v = await getAppVersion();
      setVersion(v);
      setLoadingAuthInfo(false);
    })();

  }, []);

  const updateUserInfo = (user: User) => {
    setUser(user);
  };

  const onLoggedIn = useCallback(async (apiKey: string) => {
    try {
      const { data: user } = await getCurrentUser(apiKey);
      window.localStorage.setItem('apiKey', apiKey);
      setApiKey(apiKey);
      setLoggedIn(true);
      setUser(user);
    } catch (err) {
      window.console.error(err);
    }
  }, []);

  const logout = useCallback(() => {
    window.localStorage.removeItem('apiKey');
    setApiKey('');
    setLoggedIn(false);
    setUser({ email: '', login: '' })
  }, []);

  const notifySuccess = (msg: string) => {
    toast.success(msg);
  };

  const notifyError = (msg: string) => {
    toast.error(msg);
  };

  return (
    isLoadingAuthInfo ? <Spinner/>
      : <div className="App">
        <NavBar isLoggedIn={isLoggedIn} user={user} logout={logout}/>
        <div className="Main">
          <Switch>
            <Route exact path="/" render={() => withForkMe(<Welcome/>)}/>
            <ProtectedRoute isLoggedIn={isLoggedIn} path="/main" component={SecretMain}/>
            {/*<ProtectedRoute isLoggedIn={isLoggedIn} path="/profile" render={() => withForkMe(*/}
            {/*<div>*/}
            {/*<ProfileDetails apiKey={apiKey} user={user} userService={userService}*/}
            {/*onUserUpdated={updateUserInfo}*/}
            {/*notifyError={notifyError} notifySuccess={notifySuccess}/>*/}
            {/*<PasswordDetails apiKey={apiKey} userService={userService}*/}
            {/*notifyError={notifyError} notifySuccess={notifySuccess}/>*/}
            {/*</div>*/}
            {/*)}/>*/}
            <Route path="/login" render={() => withForkMe(
              <Login onLoggedIn={onLoggedIn} notifyError={notifyError} isLoggedIn={isLoggedIn}/>
            )}/>
            <Route path="/register" render={() => withForkMe(
            <Register notifyError={notifyError} notifySuccess={notifySuccess}/>
            )}/>
            <Route path="/recover-lost-password" render={() => withForkMe(
            <RecoverLostPassword notifyError={notifyError} notifySuccess={notifySuccess}/>
            )}/>
            {/*<Route path="/password-reset" render={({ location }) => withForkMe(*/}
            {/*<PasswordReset passwordService={passwordService} queryParamsString={location.search}*/}
            {/*notifyError={notifyError} notifySuccess={notifySuccess}/>*/}
            {/*)}/>*/}
            <Route render={() => withForkMe(<NotFound/>)}/>
          </Switch>
        </div>
        <Footer version={version}/>
        <ToastContainer/>
      </div>
  );
};

export default App;
