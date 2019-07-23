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

class App extends Component {
  constructor(props) {
    super(props);
    this.state = {
      isLoggedIn: false,
      user: null,
      isLoadingAuthInfo: true
    };

    this.notifySuccess = this.notifySuccess.bind(this);
    this.notifyError = this.notifyError.bind(this);
  }

  async componentDidMount() {
    try {
      const { data: user } = await this.props.userService.getCurrentUser();
      this.setState({ isLoggedIn: true, user, isLoadingAuthInfo: false });
    } catch (_error) {
      // user is not logged in
      // TODO the backend API should not throw in case of user not logged in - it should rather return an information about that fact.
      this.setState({ isLoadingAuthInfo: false });
    }
  }

  updateUserInfo({ email, login }) {
    this.setState(state => ({ ...state, user: { ...state.user, email, login } }));
  }

  onLoggedIn(user) {
    this.setState({ isLoggedIn: true, user });
  }

  async logout() {
    try {
      await this.props.userService.logout();
      this.setState({ isLoggedIn: false, user: null });
    } catch (error) {
      this.notifyError('Logout failed!');
      console.error(error);
    }
  }

  notifySuccess(msg) {
    toast.success(msg);
  }

  notifyError(msg) {
    toast.error(msg);
  }

  render() {
    const { userService, versionService } = this.props;
    const { isLoadingAuthInfo, isLoggedIn, user } = this.state;
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
                  <ProfileDetails user={user} userService={userService}
                    onUserUpdated={this.updateUserInfo.bind(this)}
                    notifyError={this.notifyError} notifySuccess={this.notifySuccess} />
                  <PasswordDetails userService={userService} notifyError={this.notifyError} notifySuccess={this.notifySuccess} />
                </div>
              )} />
              <Route path="/login" render={() => withForkMe(
                <Login userService={userService} onLoggedIn={this.onLoggedIn.bind(this)}
                  notifyError={this.notifyError} />
                )} />
              <Route path="/register" render={() => withForkMe(
                <Register userService={userService}
                  notifyError={this.notifyError} notifySuccess={this.notifySuccess} />
                )} />
              <Route path="/recover-lost-password" render={() => withForkMe(
                <RecoverLostPassword userService={userService}
                  notifyError={this.notifyError} notifySuccess={this.notifySuccess} />
                )} />
              <Route path="/password-reset" render={({ location }) => withForkMe(
                <PasswordReset userService={userService} queryParamsString={location.search}
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
  userService: PropTypes.shape({
    logout: PropTypes.func.isRequired,
    getCurrentUser: PropTypes.func.isRequired,
  }).isRequired
};

export default App;
