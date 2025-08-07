import { Mock } from 'vitest';
import { screen } from '@testing-library/react';
import { renderWithClient } from 'tests';
import { useGetAdminVersion } from 'api/apiComponents';
import { AppFooter } from './';

vi.mock('api/apiComponents', () => ({
  useGetAdminVersion: vi.fn(),
}));

beforeEach(() => {
  vi.clearAllMocks();
});

test('<AppFooter />should render Bootzooka version info', async () => {
  const mockedUseGetAdminVersion = useGetAdminVersion as Mock;

  mockedUseGetAdminVersion.mockReturnValue({
    isPending: false,
    isLoading: false,
    isError: false,
    isSuccess: true,
    data: { buildDate: 'testDate', buildSha: 'testSha' },
  });

  renderWithClient(<AppFooter />);

  const buildSha = await screen.findByText('Version: testSha');

  expect(mockedUseGetAdminVersion).toHaveBeenCalled();
  expect(buildSha).toBeVisible();
});
