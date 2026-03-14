import { defineConfig } from 'vitest/config';
import angular from '@analogjs/vite-plugin-angular';

export default defineConfig({
  plugins: [angular()],
  test: {
    globals: false,
    environment: 'jsdom',
    setupFiles: ['src/test-setup.ts'],
    include: ['src/**/*.spec.ts'],
    reporters: ['default', 'junit'],
    outputFile: {
      junit: './coverage/frontend/junit-report.xml',
    },
    coverage: {
      provider: 'v8',
      reportsDirectory: './coverage/frontend',
      reporter: ['text-summary', 'html', 'lcovonly'],
      include: ['src/**/*.ts'],
      exclude: [
        'node_modules/**',
        'src/test-setup.ts',
        'src/main.ts',
        'src/app/app.config.ts',
        'src/app/app.routes.ts',
        'src/app/dtos/**',
        'src/app/enums/**',
        'src/app/exceptions/**',
        'src/app/utils/paginator-ptbr.ts',
        '**/*.module.ts',
        '**/*.spec.ts',
      ],
    },
  },
});
