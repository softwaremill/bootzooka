import { screen } from '@testing-library/react';
import { renderWithClient } from '@/tests';
import { AppFooter } from './';
import { useGetAdminVersion } from '@/api/apiComponents';

vi.mock('@/api/apiComponents', () => ({
  useGetAdminVersion: vi.fn(),
}));

beforeEach(() => {
  vi.clearAllMocks();
});

test('<AppFooter />should render Bootzooka version info', async () => {
  (useGetAdminVersion as unknown as ReturnType<typeof vi.fn>).mockReturnValue({
    isPending: false,
    isLoading: false,
    isError: false,
    isSuccess: true,
    data: { buildDate: 'testDate', buildSha: 'testSha' },
  });

  renderWithClient(<AppFooter />);

  const buildSha = await screen.findByText('Version: testSha');

  expect(useGetAdminVersion).toHaveBeenCalled();
  expect(buildSha).toBeVisible();
});
