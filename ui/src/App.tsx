import { BrowserRouter, Route, Routes } from 'react-router';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { UserContextProvider } from 'contexts';
import { AppLayout } from '@/components/AppLayout';
import './index.css';
import { PublicOnlyRoute } from './main/Routes/PublicOnlyRoute';

const queryClient = new QueryClient();

export const App = () => (
  <BrowserRouter>
    <QueryClientProvider client={queryClient}>
      <UserContextProvider>
        <Routes>
          <Route element={<AppLayout />}>
            <Route path="/" element={<></>} />

            <Route element={<PublicOnlyRoute />}>
              <Route path="/login" element={<></>} />
              <Route path="/register" element={<></>} />
              <Route path="/recover-lost-password" element={<></>} />
            </Route>

            <Route path="/*" element={<>Placeholder view</>} />
          </Route>
        </Routes>
      </UserContextProvider>
    </QueryClientProvider>
  </BrowserRouter>
);
