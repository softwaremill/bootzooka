import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactElement } from 'react';
import { render } from '@testing-library/react';

const defaultQueryClient = new QueryClient();

export const renderWithClient = (
  ui: ReactElement,
  client: QueryClient = defaultQueryClient
) => render(<QueryClientProvider client={client}>{ui}</QueryClientProvider>);
