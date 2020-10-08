import PropTypes from 'prop-types';
import React, { Component } from 'react';
import { Route, Switch } from 'react-router-dom';
import { ToastContainer, toast } from 'react-toastify';
import './App.css';
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
import PasswordReset from './PasswordReset/PasswordReset';
import PasswordService from './PasswordService/PasswordService';
import UserService from './UserService/UserService';
import VersionService from './VersionService/VersionService';

class App extends Component {
  constructor(props) {
    super(props);
    this.state = {
      apiKey: null,
      isLoggedIn: false,
      user: null,
      isLoadingAuthInfo: true
    };

    this.notifySuccess = this.notifySuccess.bind(this);
    this.notifyError = this.notifyError.bind(this);
  }

  async componentDidMount() {
    const apiKey = window.localStorage.getItem('apiKey');

    if (apiKey) {
      try {
        const { data: user } = await this.props.userService.getCurrentUser(apiKey);
        this.setState({ apiKey, isLoggedIn: true, user, isLoadingAuthInfo: false });
      } catch (err) {
        window.console.error(err);
        window.localStorage.removeItem('apiKey');
        this.setState({ isLoadingAuthInfo: false });
      }
    }
    this.setState({ isLoadingAuthInfo: false });
  }

  updateUserInfo({ email, login }) {
    this.setState(state => ({ ...state, user: { ...state.user, email, login } }));
  }

  async onLoggedIn(apiKey) {
    try {
      const { data: user } = await this.props.userService.getCurrentUser(apiKey);
      window.localStorage.setItem('apiKey', apiKey);
      this.setState({ apiKey, isLoggedIn: true, user });
    } catch (err) {
      window.console.error(err);
    }
  }

  logout() {
    window.localStorage.removeItem('apiKey');
    this.setState({ apiKey: null, isLoggedIn: false, user: null });
  }

  notifySuccess(msg) {
    toast.success(msg);
  }

  notifyError(msg) {
    toast.error(msg);
  }

  render() {
    const { passwordService, userService, versionService } = this.props;
    const { apiKey, isLoadingAuthInfo, isLoggedIn, user } = this.state;
    return (
      isLoadingAuthInfo ? <Spinner />
      : <div className="App">
          <NavBar isLoggedIn={isLoggedIn} user={user} logout={this.logout.bind(this)} />
          <div className="Main">
            <Switch>
              <Route exact path="/" render={() => withForkMe(<Welcome />)} />
              <ProtectedRoute isLoggedIn={isLoggedIn} path="/main" component={SecretMain} />
              <ProtectedRoute isLoggedIn={isLoggedIn} path="/profile" render={() => withForkMe(
                <div>
                  <ProfileDetails apiKey={apiKey} user={user} userService={userService}
                    onUserUpdated={this.updateUserInfo.bind(this)}
                    notifyError={this.notifyError} notifySuccess={this.notifySuccess} />
                  <PasswordDetails apiKey={apiKey} userService={userService}
                    notifyError={this.notifyError} notifySuccess={this.notifySuccess} />
                </div>
              )} />
              <Route path="/login" render={() => withForkMe(
                <Login userService={userService} onLoggedIn={this.onLoggedIn.bind(this)}
                  notifyError={this.notifyError} isLoggedIn={isLoggedIn} />
                )} />
              <Route path="/register" render={() => withForkMe(
                <Register userService={userService}
                  notifyError={this.notifyError} notifySuccess={this.notifySuccess} />
                )} />
              <Route path="/recover-lost-password" render={() => withForkMe(
                <RecoverLostPassword passwordService={passwordService}
                  notifyError={this.notifyError} notifySuccess={this.notifySuccess} />
                )} />
              <Route path="/password-reset" render={({ location }) => withForkMe(
                <PasswordReset passwordService={passwordService} queryParamsString={location.search}
                  notifyError={this.notifyError} notifySuccess={this.notifySuccess} />
              )} />
              <Route render={() => withForkMe(<NotFound />)} />
            </Switch>
          </div>
          <Footer versionService={versionService} />
          <ToastContainer />
        </div>
    );
  }
}

App.propTypes = {
  passwordService: PropTypes.instanceOf(PasswordService).isRequired,
  userService: PropTypes.instanceOf(UserService).isRequired,
  versionService: PropTypes.instanceOf(VersionService).isRequired,
};

export default App;
