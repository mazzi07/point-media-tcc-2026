import assert from 'node:assert/strict'
import { afterEach, test } from 'node:test'
import { alterarEstado, consultar, listar, salvar } from '../src/features/vagas/api.ts'
import type { DadosVaga, VagaAdmin } from '../src/features/vagas/api.ts'
import { AuthError } from '../src/features/auth/api.ts'

const originalFetch = globalThis.fetch
const dados: DadosVaga = { titulo: 'Vaga fictícia', area: 'Tecnologia', descricao: 'Descrição', modalidade: 'REMOTO', cidade: null, estado: null, requisitos: [] }
const vaga: VagaAdmin = { ...dados, id: 1, publicadaEm: null, status: 'RASCUNHO', versao: 3, criadaPor: 9 }

// Restaura o cliente HTTP depois de cada cenário isolado
afterEach(() => { globalThis.fetch = originalFetch })

// Monta as respostas controladas sem iniciar um banco ou servidor
function json(status: number, body: unknown) {
  return new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } })
}

// Mantém a consulta de visitantes no namespace público
test('listagem e detalhes públicos usam somente os caminhos de divulgação', async () => {
  const paths: string[] = []
  globalThis.fetch = async (input, options) => {
    paths.push(String(input))
    assert.equal(options?.credentials, 'same-origin')
    assert.equal(options?.method, 'GET')
    return json(200, [])
  }
  await listar(false, 2)
  await consultar(false, 7)
  assert.deepEqual(paths, ['/api/public/vagas?pagina=2', '/api/public/vagas/7'])
})

// Envia a versão lida do servidor junto com os dados editáveis
test('edição envia versão e CSRF sem incluir autoria ou estado no corpo', async () => {
  globalThis.fetch = async (input, options) => {
    if (String(input).endsWith('/csrf')) return json(200, { headerName: 'X-CSRF-TOKEN', token: 'token-ficticio' })
    assert.equal(String(input), '/api/rh/vagas/1')
    assert.equal(options?.method, 'PUT')
    assert.ok(new Headers(options?.headers).get('X-CSRF-TOKEN'))
    assert.deepEqual(JSON.parse(String(options?.body)), { versao: 3, dados })
    return json(200, { ...vaga, versao: 4 })
  }
  assert.equal((await salvar(vaga, vaga)).versao, 4)
})

// Não repete uma edição recusada por concorrência ou indisponibilidade
for (const status of [409, 503]) {
  test(`HTTP ${status} é devolvido sem repetir a escrita`, async () => {
    let writes = 0
    globalThis.fetch = async (input) => {
      if (String(input).endsWith('/csrf')) return json(200, { headerName: 'X-CSRF-TOKEN', token: 'token-ficticio' })
      writes++
      return json(status, { message: 'Operação não concluída' })
    }
    await assert.rejects(() => alterarEstado(vaga, 'publicar'), (error: unknown) => error instanceof AuthError && error.status === status)
    assert.equal(writes, 1)
  })
}

// Evita repetir uma escrita cujo resultado se perdeu na comunicação
test('falha de rede não repete nem confirma a alteração', async () => {
  let writes = 0
  globalThis.fetch = async (input) => {
    if (String(input).endsWith('/csrf')) return json(200, { headerName: 'X-CSRF-TOKEN', token: 'token-ficticio' })
    writes++
    throw new TypeError('Rede interrompida')
  }
  await assert.rejects(() => salvar(dados, null), (error: unknown) => error instanceof AuthError && error.status === 0)
  assert.equal(writes, 1)
})

// Permite uma única repetição quando o servidor rejeita o token antes da ação
test('CSRF inválido é renovado antes de repetir a ação uma vez', async () => {
  let writes = 0
  globalThis.fetch = async (input, options) => {
    if (String(input).endsWith('/csrf')) return json(200, { headerName: 'X-CSRF-TOKEN', token: 'novo-token' })
    writes++
    if (writes === 1) return json(403, { code: 'CSRF_INVALIDO' })
    assert.equal(new Headers(options?.headers).get('X-CSRF-TOKEN'), 'novo-token')
    return json(200, { ...vaga, status: 'PUBLICADA' })
  }
  await alterarEstado(vaga, 'publicar')
  assert.equal(writes, 2)
})
