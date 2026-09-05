import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import path from 'path'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  base: './',
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
      '/admin/login': 'http://localhost:8080',
      '/admin/logout': 'http://localhost:8080',
    },
  },
  build: {
    outDir: '../src/main/resources/static/admin',
    emptyOutDir: true,
    rollupOptions: {
      output: {
        manualChunks: {
          'react-admin': ['react-admin'],
          mui: ['@mui/material', '@mui/icons-material'],
          router: ['react-router-dom'],
        },
      },
    },
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
  },
})
