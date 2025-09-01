import path from 'path';
import tailwindcss from '@tailwindcss/vite';
import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react-swc';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');

  return {
    define: {
      'process.env.REACT_APP_BASE_URL': JSON.stringify(env.VITE_APP_BASE_URL),
    },
    plugins: [react(), tailwindcss()],
    server: {
      open: true,
      port: 8081,
    },
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
      },
    },
    test: {
      setupFiles: [path.resolve(__dirname, './src/tests/setup.ts')],
    },
  };
});
