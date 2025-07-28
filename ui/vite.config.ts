import path from 'path';
import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react-swc';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');

  return {
    define: {
      'process.env.REACT_APP_BASE_URL': JSON.stringify(env.VITE_APP_BASE_URL),
    },
    plugins: [react()],
    server: {
      open: true,
      port: 8081,
    },
    resolve: {
      alias: {
        api: path.resolve(__dirname, './src/api'),
        assets: path.resolve(__dirname, './src/assets'),
        components: path.resolve(__dirname, './src/components'),
        contexts: path.resolve(__dirname, './src/contexts'),
        main: path.resolve(__dirname, './src/main'),
        pages: path.resolve(__dirname, './src/pages'),
        tests: path.resolve(__dirname, './src/tests'),
        hooks: path.resolve(__dirname, './src/hooks'),
      },
    },
  };
});
