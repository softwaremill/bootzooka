import Navbar from 'react-bootstrap/Navbar';
import Nav from 'react-bootstrap/Nav';
import Container from 'react-bootstrap/Container';
import { Link } from 'react-router';
import { BiPowerOff, BiHappy } from 'react-icons/bi';
import { useUserContext } from 'contexts/UserContext/User.context';
import { usePostUserLogout } from 'api/apiComponents';
import { useApiKeyState } from 'hooks/auth';

export const Top = () => {
  const {
    state: { user },
    dispatch,
  } = useUserContext();
  const [apiKeyState] = useApiKeyState();

  const apiKey = apiKeyState?.apiKey;

  const { mutateAsync: logout } = usePostUserLogout({
    onSuccess: () => {
      dispatch({ type: 'LOG_OUT' });
    },
  });

  return (
    <Navbar variant="dark" bg="dark" sticky="top" collapseOnSelect expand="lg">
      <Container>
        <Navbar.Brand as={Link} to="/">
          Bootzooka
        </Navbar.Brand>
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="d-flex flex-grow-1 justify-content-between">
            <Nav.Link as={Link} to="/">
              Welcome
            </Nav.Link>
            <Nav.Link as={Link} to="/main">
              Home
            </Nav.Link>
            <div className="flex-grow-1" />
            {user && apiKey ? (
              <>
                <Nav.Link as={Link} to="/profile" className="text-lg-end">
                  <BiHappy />
                  &nbsp;{user?.login}
                </Nav.Link>{' '}
                <Nav.Link
                  className="text-lg-end"
                  onClick={() => {
                    logout({ body: { apiKey } });
                  }}
                >
                  <BiPowerOff />
                  &nbsp;Logout
                </Nav.Link>
              </>
            ) : (
              <>
                <Nav.Link as={Link} to="/register" className="text-lg-end">
                  Register
                </Nav.Link>
                <Nav.Link as={Link} to="/login" className="text-lg-end">
                  Login
                </Nav.Link>
              </>
            )}
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
};
