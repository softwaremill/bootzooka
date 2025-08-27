import { screen } from '@testing-library/react';
import { userEvent } from '@testing-library/user-event';
import { renderWithClient } from '@/tests';
import { RecoverLostPassword } from './index';
import * as apiComponents from '@/api/apiComponents';

vi.mock('@/api/apiComponents', () => ({
  usePostPasswordresetForgot: vi.fn(),
}));

beforeEach(() => {
  vi.clearAllMocks();
});

test('<RecoverLostPassword /> should submit the form successfully', async () => {
  const mutate = vi.fn();

  vi.mocked(apiComponents.usePostPasswordresetForgot).mockReturnValue({
    mutate,
    reset: vi.fn(),
    data: { apiKey: 'test-api-key' },
    isSuccess: true,
    isError: false,
    error: '',
  } as unknown as ReturnType<typeof apiComponents.usePostPasswordresetForgot>);

  renderWithClient(<RecoverLostPassword />);

  await userEvent.type(screen.getByLabelText('Login or email'), 'test-login');
  await userEvent.click(screen.getByText('Reset Password'));

  expect(mutate).toHaveBeenCalledWith({
    body: { loginOrEmail: 'test-login' },
  });
});

test('<RecoverLostPassword /> should handle form submission error correctly', async () => {
  const mutate = vi.fn();

  vi.mocked(apiComponents.usePostPasswordresetForgot).mockReturnValue({
    mutate,
    reset: vi.fn(),
    data: { apiKey: 'test-api-key' },
    isSuccess: false,
    isError: true,
    error: 'Test error',
  } as unknown as ReturnType<typeof apiComponents.usePostPasswordresetForgot>);

  renderWithClient(<RecoverLostPassword />);

  await userEvent.type(screen.getByLabelText('Login or email'), 'test-login');
  await userEvent.click(screen.getByText('Reset Password'));

  expect(mutate).toHaveBeenCalledWith({
    body: { loginOrEmail: 'test-login' },
  });

  expect(await screen.findByText('Unknown error')).toBeVisible();
});
