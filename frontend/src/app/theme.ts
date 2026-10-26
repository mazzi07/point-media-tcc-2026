import { createSystem, defaultConfig, defineConfig } from '@chakra-ui/react'

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
  globalCss: {
    'html, body': {
      minHeight: '100%',
      background: 'white',
      color: 'gray.900',
      colorScheme: 'light',
    },
  },
})

export const system = createSystem(defaultConfig, config)
