import { act, renderHook } from '@testing-library/react';
import { UserContextProvider } from './UserContext';
import { useUserContext } from './User.context';

test('useUserContext() should handle actions and state updates correctly', () => {
  const hook = renderHook(() => useUserContext(), {
    wrapper: UserContextProvider,
  });

  expect(hook.result.current.state).toEqual({
    user: null,
  });

  act(() => {
    hook.result.current.dispatch({
      type: 'LOG_IN',
      user: {
        createdOn: '2023-10-01T12:00:00Z',
        login: 'test-user',
        email: 'test-user@example.com',
      },
    });
  });

  hook.rerender();

  expect(hook.result.current.state).toEqual({
    user: {
      createdOn: '2023-10-01T12:00:00Z',
      login: 'test-user',
      email: 'test-user@example.com',
    },
  });

  act(() => {
    hook.result.current.dispatch({
      type: 'UPDATE_USER_DATA',
      user: {
        login: 'updated-user',
        email: 'updatedEmail@address.pl',
      },
    });
  });

  hook.rerender();

  expect(hook.result.current.state).toEqual({
    user: {
      createdOn: '2023-10-01T12:00:00Z',
      login: 'updated-user',
      email: 'updatedEmail@address.pl',
    },
  });
});
