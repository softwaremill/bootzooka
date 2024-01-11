import React from "react";
import { BrowserRouter } from "react-router-dom";
import { Main } from "main";
import { UserContextProvider } from "contexts";
import { QueryClient, QueryClientProvider } from "react-query";

const queryClient = new QueryClient();

export const App: React.FC = () => (
  <BrowserRouter>
    <QueryClientProvider client={queryClient}>
      <UserContextProvider>
        <Main />
      </UserContextProvider>
    </QueryClientProvider>
  </BrowserRouter>
);
