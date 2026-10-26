import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { Box, Button, Field, Flex, Heading, Input, Stack, Text } from '@chakra-ui/react'
import * as auth from './api'
import { signIn, signOut, verifySession } from './session'
import type { SessionState } from './session'

// Controla o acesso à conta e o estado da sessão
export function AuthPanel() {
  const [session, setSession] = useState<SessionState>({ status: 'unverified', user: null })
  const [mode, setMode] = useState<'login' | 'register'>('login')
  const [checking, setChecking] = useState(true)
  const [pending, setPending] = useState(false)
  const [fields, setFields] = useState<Record<string, string>>({})
  const [notice, setNotice] = useState<{ error: boolean; message: string } | null>(null)
  const sessionRef = useRef(session)
  const busy = useRef(false)
  const generation = useRef(0)

  // Acompanha a sessão enquanto o painel está aberto
  useEffect(() => {
    let active = true

    // Confere a sessão ao abrir ou recuperar o foco da página
    async function inspectSession() {
      // Evita disputar a sessão com uma ação em andamento
      if (busy.current) return
      const inspection = ++generation.current
      const previous = sessionRef.current
      const next = await verifySession(previous)
      // Ignora respostas de consultas antigas ou de um painel fechado
      if (!active || inspection !== generation.current) return
      updateSession(next)
      // Avisa quando uma sessão antes confirmada deixou de ser válida
      if (next.status === 'anonymous' && previous.user) {
        setNotice({ error: true, message: 'Sua sessão expirou. Entre novamente.' })
      } else if (next.status !== 'anonymous') {
        setNotice(null)
      }
      setChecking(false)
    }

    void inspectSession()
    // Confere novamente a sessão ao voltar para a página
    window.addEventListener('focus', inspectSession)
    // Descarta consultas antigas ao desmontar o painel
    return () => {
      active = false
      window.removeEventListener('focus', inspectSession)
    }
  }, [])

  // Atualiza o estado exibido e a referência usada nas consultas
  function updateSession(next: SessionState) {
    sessionRef.current = next
    setSession(next)
  }

  // Bloqueia ações simultâneas e limpa mensagens anteriores
  function beginAction() {
    busy.current = true
    generation.current++
    setChecking(false)
    setPending(true)
    setFields({})
    setNotice(null)
  }

  // Libera os controles após a operação
  function endAction() {
    busy.current = false
    setPending(false)
  }

  // Envia os dados de cadastro ou login
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = event.currentTarget
    const data = new FormData(form)
    const email = String(data.get('email') ?? '')
    const senha = String(data.get('senha') ?? '')
    beginAction()
    try {
      // Escolhe entre criar a conta e iniciar uma sessão
      if (mode === 'register') {
        // Envia o cadastro e prepara o formulário de login
        await auth.register(String(data.get('nome') ?? ''), email, senha)
        form.reset()
        setMode('login')
        setNotice({ error: false, message: 'Cadastro criado. Entre com seu e-mail e senha.' })
      } else {
        // Consulta a sessão após a tentativa de login
        const next = await signIn(email, senha)
        updateSession(next)
        if (next.status === 'anonymous') {
          setNotice({ error: true, message: 'Sua sessão expirou. Entre novamente.' })
        }
        form.reset()
      }
    // Mostra a falha sem presumir que a sessão foi encerrada
    } catch (error) {
      // Limpa a conta somente quando o servidor confirma a sessão inválida
      if (error instanceof auth.AuthError && error.status === 401) {
        updateSession({ status: 'anonymous', user: null })
      }
      setNotice({ error: true, message: errorMessage(error) })
      if (error instanceof auth.AuthError) setFields(error.fields)
    } finally {
      endAction()
    }
  }

  // Solicita a saída e só confirma após resposta do servidor
  async function logout() {
    beginAction()
    try {
      const next = await signOut(sessionRef.current)
      updateSession(next)
      // Avisa quando uma sessão antes confirmada deixou de ser válida
      if (next.status === 'anonymous') {
        setMode('login')
        setNotice({ error: false, message: 'Você saiu da sua conta.' })
      }
    } finally {
      endAction()
    }
  }

  // Tenta confirmar novamente a sessão atual
  async function confirmSession() {
    beginAction()
    try {
      const next = await verifySession(sessionRef.current)
      updateSession(next)
      // Apresenta o resultado quando a sessão pôde ser conferida
      if (next.status !== 'unverified') {
        setNotice({ error: next.status === 'anonymous', message: next.status === 'anonymous'
          ? 'Sua sessão expirou. Entre novamente.' : 'Sua sessão está ativa.' })
      }
    } finally {
      endAction()
    }
  }

  const roleNames = { USUARIO: 'Usuário', RH: 'RH', GESTOR: 'Gestor' }
  const user = session.user

  // Mostra acesso à conta ou as opções de verificar e encerrar a sessão
  return (
    <Box as="section" aria-labelledby="auth-heading" mt="8" borderWidth="1px"
      borderColor="gray.200" p={{ base: '5', md: '8' }}>
      <Heading id="auth-heading" as="h2" size="lg" mb="3">Sua conta Point Media</Heading>
      {notice && (
        <Box role={notice.error ? 'alert' : 'status'} mb="5" p="3"
          bg={notice.error ? 'red.50' : 'gray.50'} borderLeftWidth="3px"
          borderColor={notice.error ? 'red.600' : 'brand.blue'}>
          <Text fontSize="sm">{notice.message}</Text>
        </Box>
      )}
      {checking && !user ? <Text role="status">Consultando sessão…</Text> : session.status !== 'anonymous' ? (
        <Stack gap="4">
          {session.status === 'unverified' && (
            <Text role="alert" fontSize="sm">
              Não foi possível verificar sua sessão. Tente novamente ou saia da conta.
            </Text>
          )}
          {user && <Box>
            <Text fontWeight="semibold">{session.status === 'unverified'
              ? `Último acesso confirmado: ${user.nome}.` : `Olá, ${user.nome}.`}</Text>
            <Text color="gray.600" fontSize="sm">{user.email} · {roleNames[user.perfil]}</Text>
          </Box>}
          <Flex gap="3" flexWrap="wrap">
            <Button type="button" variant="outline" onClick={confirmSession} disabled={pending}>
              {session.status === 'unverified' ? 'Tentar novamente' : 'Conferir sessão'}
            </Button>
            <Button type="button" variant="outline" borderColor="brand.pink" onClick={logout}
              loading={pending} loadingText="Aguarde">Sair da conta</Button>
          </Flex>
        </Stack>
      ) : (
        <>
          <Text color="gray.600" fontSize="sm" mb="5">
            Você pode continuar como visitante. Entre ou crie sua conta para os próximos recursos do Banco de Talentos.
          </Text>
          <Flex gap="2" mb="6" aria-label="Escolha como acessar sua conta">
            <Button type="button" variant={mode === 'login' ? 'outline' : 'ghost'}
              borderColor={mode === 'login' ? 'brand.blue' : undefined}
              aria-pressed={mode === 'login'} disabled={pending}
              onClick={() => { setMode('login'); setFields({}); setNotice(null) }}>Entrar</Button>
            <Button type="button" variant={mode === 'register' ? 'outline' : 'ghost'}
              borderColor={mode === 'register' ? 'brand.pink' : undefined}
              aria-pressed={mode === 'register'} disabled={pending}
              onClick={() => { setMode('register'); setFields({}); setNotice(null) }}>Criar conta</Button>
          </Flex>
          <form onSubmit={submit} key={mode}>
            <Stack gap="5">
              {mode === 'register' && (
                <Field.Root invalid={Boolean(fields.nome)} required>
                  <Field.Label htmlFor="auth-nome">Nome</Field.Label>
                  <Input id="auth-nome" name="nome" autoComplete="name" maxLength={120}
                    required disabled={pending} />
                  <Field.ErrorText>{fields.nome}</Field.ErrorText>
                </Field.Root>
              )}
              <Field.Root invalid={Boolean(fields.email)} required>
                <Field.Label htmlFor="auth-email">E-mail</Field.Label>
                <Input id="auth-email" name="email" type="email" autoComplete="username"
                  maxLength={254} required disabled={pending} />
                <Field.ErrorText>{fields.email}</Field.ErrorText>
              </Field.Root>
              <Field.Root invalid={Boolean(fields.senha)} required>
                <Field.Label htmlFor="auth-senha">Senha</Field.Label>
                <Input id="auth-senha" name="senha" type="password" required disabled={pending}
                  autoComplete={mode === 'register' ? 'new-password' : 'current-password'}
                  minLength={mode === 'register' ? 12 : undefined} maxLength={72} />
                {mode === 'register' && <Field.HelperText>Use pelo menos 12 caracteres, até 72 bytes em UTF-8.</Field.HelperText>}
                <Field.ErrorText>{fields.senha}</Field.ErrorText>
              </Field.Root>
              <Button type="submit" alignSelf="start" bg="brand.blue" color="gray.950"
                loading={pending} loadingText="Aguarde">
                {mode === 'register' ? 'Concluir cadastro' : 'Acessar conta'}
              </Button>
            </Stack>
          </form>
        </>
      )}
    </Box>
  )
}

// Escolhe uma mensagem segura para apresentar na tela
function errorMessage(error: unknown) {
  return error instanceof auth.AuthError ? error.message : 'Não foi possível concluir a solicitação. Tente novamente.'
}
