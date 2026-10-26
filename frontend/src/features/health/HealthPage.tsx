import { useEffect, useState } from 'react'
import { Box, Button, Container, Flex, Heading, Text } from '@chakra-ui/react'

type ConnectionStatus = 'loading' | 'up' | 'error'

export function HealthPage() {
  const [status, setStatus] = useState<ConnectionStatus>('loading')
  const [attempt, setAttempt] = useState(0)

  useEffect(() => {
    const controller = new AbortController()

    async function checkHealth() {
      setStatus('loading')

      try {
        const response = await fetch('/api/health', {
          signal: AbortSignal.any([controller.signal, AbortSignal.timeout(5000)]),
          headers: { Accept: 'application/json' },
        })
        if (!response.ok) throw new Error('Não foi possível consultar o backend.')

        const data: unknown = await response.json()
        if (
          typeof data !== 'object' || data === null ||
          !('status' in data) || data.status !== 'UP'
        ) {
          throw new Error('Resposta de saúde inesperada.')
        }

        if (!controller.signal.aborted) setStatus('up')
      } catch {
        if (!controller.signal.aborted) setStatus('error')
      }
    }

    void checkHealth()
    return () => controller.abort()
  }, [attempt])

  const messages: Record<ConnectionStatus, string> = {
    loading: 'Verificando conexão…',
    up: 'Backend conectado',
    error: 'Backend indisponível',
  }

  return (
    <Box minH="100vh">
      <Flex h="2" aria-hidden="true">
        <Box bg="brand.pink" flex="1" />
        <Box bg="brand.blue" flex="1" />
      </Flex>
      <Container as="main" maxW="3xl" py={{ base: '12', md: '24' }}>
        <Text fontSize="sm" fontWeight="semibold" mb="4">BANCO DE TALENTOS</Text>
        <Heading as="h1" size="4xl" mb="4">Point Media</Heading>
        <Text color="gray.600" maxW="xl" mb="10">
          Base inicial do projeto. Esta página verifica a comunicação com a aplicação.
        </Text>
        <Box borderWidth="1px" borderColor="gray.200" borderTopWidth="4px"
          borderTopColor="brand.blue" p={{ base: '5', md: '8' }}>
          <Heading as="h2" size="lg" mb="3">Estado da aplicação</Heading>
          <Text role="status" aria-live="polite" fontWeight="medium" mb="2">
            {messages[status]}
          </Text>
          <Text color="gray.600" fontSize="sm" mb="6">
            {status === 'up'
              ? 'A aplicação respondeu à verificação de conexão.'
              : status === 'error'
                ? 'Confira se o backend está em execução e tente novamente.'
                : 'Aguardando resposta da aplicação.'}
          </Text>
          <Button type="button" variant="outline" borderColor="brand.pink"
            loading={status === 'loading'} loadingText="Verificando"
            onClick={() => setAttempt((value) => value + 1)}>
            Verificar novamente
          </Button>
        </Box>
      </Container>
    </Box>
  )
}
