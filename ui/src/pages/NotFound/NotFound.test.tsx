import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { NotFound } from './NotFound';

test('<NotFound /> should render', () => {
  render(<NotFound />, { wrapper: MemoryRouter });

  expect(screen.getByText("You shouldn't be here for sure 😅")).toBeVisible();
  expect(screen.getByText('Home page')).toBeVisible();
  expect(screen.getByText('SoftwareMill - our company website')).toBeVisible();
  expect(screen.getByText('Bootzooka on GitHub')).toBeVisible();
});
