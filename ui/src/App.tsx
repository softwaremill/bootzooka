import { BrowserRouter, Route, Routes } from 'react-router';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { UserContextProvider } from '@/contexts';
import { AppLayout } from '@/components/AppLayout';
import { PublicOnlyRoute } from './components/PublicOnlyRoute';
import { Login } from './pages/Login';
import { Toaster } from '@/components/ui/sonner';
import { Register } from './pages/Register';
import { ProtectedRoute } from './components/ProtectedRoute';
import { NotFound, SecretMain, Welcome } from './pages';
import { RecoverLostPassword } from './pages/RecoverLostPassword';
import { Profile } from './pages/Profile';
import './index.css';

const queryClient = new QueryClient();

export const App = () => (
  <BrowserRouter>
    <QueryClientProvider client={queryClient}>
      <UserContextProvider>
        <Routes>
          <Route element={<AppLayout />}>
            <Route path="/" element={<Welcome />} />

            <Route element={<PublicOnlyRoute />}>
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
              <Route
                path="/recover-lost-password"
                element={<RecoverLostPassword />}
              />
            </Route>

            <Route element={<ProtectedRoute />}>
              <Route path="/main" element={<SecretMain />} />
              <Route path="/profile" element={<Profile />} />
            </Route>

            <Route path="/*" element={<NotFound />} />
          </Route>
        </Routes>
        <Toaster />
      </UserContextProvider>
    </QueryClientProvider>
  </BrowserRouter>
);
