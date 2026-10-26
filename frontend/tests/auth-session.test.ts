import assert from 'node:assert/strict'
import { afterEach, test } from 'node:test'
import { signIn, signOut, verifySession } from '../src/features/auth/session.ts'
import type { SessionState } from '../src/features/auth/session.ts'

const user = { id: 1, nome: 'Pessoa de teste', email: 'pessoa@example.invalid', perfil: 'USUARIO' as const }
const authenticated: SessionState = { status: 'authenticated', user }
const originalFetch = globalThis.fetch

// Restaura o cliente HTTP após cada cenário
afterEach(() => { globalThis.fetch = originalFetch })

// Prepara uma resposta HTTP controlada para o teste
function respond(status: number, body: unknown = {}) {
  return new Response(status === 204 ? null : JSON.stringify(body), {
    status, headers: { 'Content-Type': 'application/json' },
  })
}

// Repete os cenários de falha de comunicação
for (const failure of ['timeout', 'network', '503'] as const) {
  // Confere a preservação da sessão para cada falha simulada
  test(`${failure} ao verificar preserva o último usuário e marca a sessão como não verificável`, async (t) => {
    // Simula o tempo limite sem aguardar dez segundos no teste
    if (failure === 'timeout') {
      const timeout = AbortSignal.timeout.bind(AbortSignal)
      t.mock.method(AbortSignal, 'timeout', (milliseconds: number) => {
        assert.equal(milliseconds, 10000)
        return timeout(1)
      })
    }
    // Controla as respostas HTTP deste cenário
    globalThis.fetch = async (_input, options) => {
      // Simula a indisponibilidade informada pela API
      if (failure === '503') return respond(503, { code: 'SERVICO_INDISPONIVEL' })
      // Simula uma interrupção de rede
      if (failure === 'network') throw new TypeError('Conexão interrompida')
      // O temporizador mantém o processo vivo até o AbortSignal disparar
      return new Promise<Response>((_resolve, reject) => {
        const deadline = setTimeout(() => reject(new Error('AbortSignal não disparou')), 1000)
        const abort = () => { clearTimeout(deadline); reject(options?.signal?.reason) }
        // Rejeita a chamada quando o tempo limite dispara
        if (options?.signal?.aborted) abort()
        else options?.signal?.addEventListener('abort', abort, { once: true })
      })
    }
    const result = await verifySession(authenticated)
    assert.equal(result.status, 'unverified')
    assert.equal(result.user, user)
  })
}

// Limpa a conta apenas após a confirmação de sessão inválida
test('401 confirma sessão inválida e remove o usuário', async () => {
  // Controla a falha ou resposta esperada neste cenário
  globalThis.fetch = async () => respond(401)
  assert.deepEqual(await verifySession(authenticated), { status: 'anonymous', user: null })
})

// Não presume que a sessão inicial seja anônima
test('falha na consulta inicial não presume sessão anônima', async () => {
  // Controla a falha ou resposta esperada neste cenário
  globalThis.fetch = async () => respond(503)
  assert.deepEqual(await verifySession({ status: 'unverified', user: null }),
    { status: 'unverified', user: null })
})

// Repete os cenários de falha de comunicação
for (const failure of ['timeout', '503'] as const) {
  // Confere a preservação da sessão para cada falha simulada
  test(`login confirmado seguido de ${failure} mantém opções de recuperar e encerrar a sessão`, async () => {
    const calls: string[] = []
    // Controla as respostas HTTP deste cenário
    globalThis.fetch = async (input) => {
      const path = String(input)
      calls.push(path)
      // Entrega o token necessário para a escrita simulada
      if (path.endsWith('/csrf')) return respond(200, { headerName: 'X-CSRF-TOKEN', token: 'token-ficticio' })
      // Confirma o login antes de simular a próxima consulta
      if (path.endsWith('/login')) return respond(204)
      if (failure === 'timeout') throw new DOMException('Tempo excedido', 'TimeoutError')
      return respond(503)
    }
    const result = await signIn(user.email, 'Senha-ficticia-2026!')
    assert.deepEqual(result, { status: 'unverified', user: null })
    assert.ok(calls.includes('/api/auth/login'))
    assert.ok(calls.includes('/api/auth/me'))

    // Controla as respostas HTTP deste cenário
    globalThis.fetch = async () => respond(200, user)
    assert.deepEqual(await verifySession(result), authenticated)
  })
}

// Trata o login com resposta perdida como não verificável
test('resposta de login perdida não confirma que o usuário continua anônimo', async () => {
  // Controla a falha ou resposta esperada neste cenário
  globalThis.fetch = async (input) => {
    // Entrega o token usado na tentativa de logout
    if (String(input).endsWith('/csrf')) return respond(200, { headerName: 'X-CSRF-TOKEN', token: 'token-ficticio' })
    throw new TypeError('Resposta perdida')
  }
  assert.deepEqual(await signIn(user.email, 'Senha-ficticia-2026!'), { status: 'unverified', user: null })
})

// Mantém a conta até uma nova tentativa confirmar a saída
test('logout com 503 preserva usuário; nova tentativa só limpa após 204', async () => {
  let available = false
  let logouts = 0
  // Controla a falha ou resposta esperada neste cenário
  globalThis.fetch = async (input) => {
    // Entrega o token usado na tentativa de logout
    if (String(input).endsWith('/csrf')) return respond(200, { headerName: 'X-CSRF-TOKEN', token: 'token-ficticio' })
    assert.equal(String(input), '/api/auth/logout')
    logouts++
    return respond(available ? 204 : 503)
  }
  const failed = await signOut(authenticated)
  assert.deepEqual(failed, { status: 'unverified', user })
  available = true
  assert.deepEqual(await signOut(failed), { status: 'anonymous', user: null })
  assert.equal(logouts, 2)
})

// Renova o CSRF antes de repetir o logout rejeitado
test('logout após falha de comunicação renova CSRF inválido e encerra a sessão', async () => {
  // Controla a falha ou resposta esperada neste cenário
  globalThis.fetch = async () => { throw new TypeError('Falha temporária') }
  const uncertain = await verifySession(authenticated)
  let logouts = 0
  // Controla a falha ou resposta esperada neste cenário
  globalThis.fetch = async (input, options) => {
    // Entrega o token usado na tentativa de logout
    if (String(input).endsWith('/csrf')) return respond(200, { headerName: 'X-CSRF-TOKEN', token: 'token-renovado' })
    logouts++
    // Rejeita o primeiro token para testar sua renovação
    if (logouts === 1) return respond(403, { code: 'CSRF_INVALIDO' })
    assert.equal(new Headers(options?.headers).get('X-CSRF-TOKEN'), 'token-renovado')
    return respond(204)
  }
  assert.deepEqual(await signOut(uncertain), { status: 'anonymous', user: null })
  assert.equal(logouts, 2)
})

// Permite sair mesmo sem os dados da conta
test('sessão pode ser encerrada mesmo sem dados de usuário após login', async () => {
  // Controla a falha ou resposta esperada neste cenário
  globalThis.fetch = async (input) => String(input).endsWith('/csrf')
    ? respond(200, { headerName: 'X-CSRF-TOKEN', token: 'token-ficticio' }) : respond(204)
  assert.deepEqual(await signOut({ status: 'unverified', user: null }), { status: 'anonymous', user: null })
})
