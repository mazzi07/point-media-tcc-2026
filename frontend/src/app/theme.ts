import { createSystem, defaultConfig, defineConfig } from '@chakra-ui/react'

// Centraliza as cores institucionais e a aparência da aplicação
const config = defineConfig({
  theme: {
    tokens: {
      colors: {
        brand: {
          pink: { value: '#EB6099' },
          blue: { value: '#6CABDE' },
        },
      },
    },
  },
  // Mantém a página com o tema claro aprovado
  globalCss: {
    'html, body': {
      minHeight: '100%',
      background: 'white',
      color: 'gray.900',
      colorScheme: 'light',
    },
  },
})

// Combina os tokens da aplicação com o tema do Chakra
export const system = createSystem(defaultConfig, config)
