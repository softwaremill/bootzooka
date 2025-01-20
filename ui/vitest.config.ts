import { defineConfig, mergeConfig } from 'vitest/config';
import viteConfig from './vite.config';

export default defineConfig((env) =>
  mergeConfig(
    viteConfig(env),
    defineConfig({
      test: {
        globals: true,
        environment: 'jsdom',
        setupFiles: ['./src/setupTest.ts'],
        reporters: ['verbose'],
        coverage: {
          reporter: ['text', 'json', 'html'],
          include: ['src/**/*'],
          exclude: [],
        },
      },
    })
  )
);
