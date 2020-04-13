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
import ProfileDetails from './ProfileDetails/ProfileDetails';
import PasswordDetails from './PasswordDetails/PasswordDetails';
import Footer from './Footer/Footer';
import { getCurrentUser } from './UserService/UserService';
import { getAppVersion } from './VersionService/VersionService';
import { User, Version } from './types/Types';
import { Either, Right } from 'ts-matches';

const App: React.FC = () => {
  const [apiKey, setApiKey] = useState('');
  const [isLoggedIn, setLoggedIn] = useState(false);
  const [user, setUser] = useState<User>({ email: '', login: '' });
  const [isLoadingAuthInfo, setLoadingAuthInfo] = useState(true);
  const [version, setVersion] = useState<Either<Error, Version>>(Right.of({ buildDate: '', buildSha: '' }));

  useEffect(() => {
    (async () => {
      const apiKey = window.localStorage.getItem('apiKey');
      if (apiKey) {
        (await getCurrentUser(apiKey)).fold({
          left: (error: Error) => {
            window.console.error(error);
            window.localStorage.removeItem('apiKey');
          },
          right: (user: User) => {
            console.log(user);
            setUser(user);
            setApiKey(apiKey);
            setLoggedIn(true);
          }
        });

        setLoadingAuthInfo(false);
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
    (await getCurrentUser(apiKey)).fold({
      left: (error: Error) => window.console.error(error),
      right: (data: User) => {
        window.localStorage.setItem('apiKey', apiKey);
        setApiKey(apiKey);
        setLoggedIn(true);
        setUser(data);
      }
    });
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
            <ProtectedRoute isLoggedIn={isLoggedIn} path="/profile" render={() => withForkMe(
              <div>
                <ProfileDetails apiKey={apiKey} user={user} onUserUpdated={updateUserInfo}
                                notifyError={notifyError} notifySuccess={notifySuccess}/>
                <PasswordDetails apiKey={apiKey} notifyError={notifyError} notifySuccess={notifySuccess}/>
              </div>
            )}/>
            <Route path="/login" render={() => withForkMe(
              <Login onLoggedIn={onLoggedIn} notifyError={notifyError} isLoggedIn={isLoggedIn}/>
            )}/>
            <Route path="/register" render={() => withForkMe(
              <Register notifyError={notifyError} notifySuccess={notifySuccess}/>
            )}/>
            <Route path="/recover-lost-password" render={() => withForkMe(
              <RecoverLostPassword notifyError={notifyError} notifySuccess={notifySuccess}/>
            )}/>
            <Route render={() => withForkMe(<NotFound/>)}/>
          </Switch>
        </div>
        <Footer version={version}/>
        <ToastContainer/>
      </div>
  );
};

export default App;
