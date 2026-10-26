import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// Configura o desenvolvimento do frontend
export default defineConfig({
  plugins: [react()],
  // Mantém o servidor restrito à máquina local
  server: {
    host: '127.0.0.1',
    port: 5173,
    strictPort: true,
    // Encaminha as chamadas da API ao backend local
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      },
    },
  },
})
