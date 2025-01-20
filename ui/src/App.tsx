import { BrowserRouter } from 'react-router';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Main } from 'main/Main/Main';
import { UserContextProvider } from 'contexts';

const queryClient = new QueryClient();

export const App = () => (
  <BrowserRouter>
    <QueryClientProvider client={queryClient}>
      <UserContextProvider>
        <Main />
      </UserContextProvider>
    </QueryClientProvider>
  </BrowserRouter>
);
