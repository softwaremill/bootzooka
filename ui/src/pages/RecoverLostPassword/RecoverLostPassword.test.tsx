import { screen } from '@testing-library/react';
import { userEvent } from '@testing-library/user-event';
import { renderWithClient } from 'tests';
import { RecoverLostPassword } from './RecoverLostPassword';

const mockMutate = vi.fn();
const mockResponse = vi.fn();

vi.mock('api/apiComponents', () => ({
  usePostPasswordresetForgot: () => mockResponse(),
}));

beforeEach(() => {
  vi.clearAllMocks();
});

test('renders header', () => {
  mockResponse.mockReturnValueOnce({
    mutate: mockMutate,
    reset: vi.fn(),
    data: { apiKey: 'test-api-key' },
    isSuccess: true,
    isError: false,
    error: '',
  });

  renderWithClient(<RecoverLostPassword />);

  expect(screen.getByText('Recover lost password')).toBeInTheDocument();
});

test('handles password recover success', async () => {
  mockResponse.mockReturnValueOnce({
    mutate: mockMutate,
    reset: vi.fn(),
    data: { apiKey: 'test-api-key' },
    isSuccess: true,
    isError: false,
    error: '',
  });

  renderWithClient(<RecoverLostPassword />);

  await userEvent.type(screen.getByLabelText('Login or email'), 'test-login');
  await userEvent.click(screen.getByText('Reset password'));

  expect(mockMutate).toHaveBeenCalledWith({
    body: { loginOrEmail: 'test-login' },
  });

  await screen.findByRole('success');
  await screen.findByText('Password reset claim success');
});

test('handles password recover error', async () => {
  mockResponse.mockReturnValueOnce({
    mutate: mockMutate,
    reset: vi.fn(),
    data: { apiKey: 'test-api-key' },
    isSuccess: false,
    isError: true,
    error: 'Test error',
  });

  renderWithClient(<RecoverLostPassword />);

  await userEvent.type(screen.getByLabelText('Login or email'), 'test-login');
  await userEvent.click(screen.getByText('Reset password'));

  expect(mockMutate).toHaveBeenCalledWith({
    body: { loginOrEmail: 'test-login' },
  });

  await screen.findByRole('error');
});
