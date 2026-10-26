import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { ChakraProvider } from '@chakra-ui/react'
import { system } from './app/theme'
import { HealthPage } from './features/health/HealthPage'

// Inicia a aplicação com o tema institucional
createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ChakraProvider value={system}>
      <HealthPage />
    </ChakraProvider>
  </StrictMode>,
)
