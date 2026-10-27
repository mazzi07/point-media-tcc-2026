import assert from 'node:assert/strict'
import { after, before, test } from 'node:test'
import { pathToFileURL } from 'node:url'

const modulePath = process.env.POINT_MEDIA_PLAYWRIGHT_MODULE
const baseUrl = process.env.POINT_MEDIA_FRONTEND_URL ?? 'http://127.0.0.1:5173'
const rh = { id: 1, nome: 'RH fictício', email: 'rh@example.invalid', perfil: 'RH' }
const vaga = { id: 7, titulo: 'Vaga fictícia', area: 'Tecnologia', descricao: 'Descrição fictícia',
  modalidade: 'REMOTO', cidade: null, estado: null, requisitos: [{ descricao: 'Java', tipo: 'OBRIGATORIO' }],
  status: 'PUBLICADA', versao: 2, criadaPor: 1, publicadaEm: '2026-10-26T12:00:00' }
let browser

// Usa um navegador já disponível sem adicionar dependências ao projeto
before(async () => {
  if (!modulePath) return
  const { chromium } = await import(pathToFileURL(modulePath).href)
  browser = await chromium.launch({ channel: 'msedge', headless: true })
})

// Encerra apenas o navegador aberto por estes testes
after(async () => { await browser?.close() })

// Entrega respostas controladas à interface sem acessar o banco
function respond(route, status, body = {}) {
  return route.fulfill({ status, contentType: 'application/json', body: status === 204 ? '' : JSON.stringify(body) })
}

// Espera a tela chegar ao estado observado sem depender de uma pausa fixa
async function visible(locator) {
  await locator.waitFor({ state: 'visible', timeout: 5000 })
}

// Isola cookies, respostas e erros de cada cenário no navegador real
function uiTest(name, run) {
  test(name, { skip: !modulePath, timeout: 30000 }, async t => {
    const context = await browser.newContext()
    t.after(() => context.close())
    const page = await context.newPage()
    const errors = []
    page.on('pageerror', error => errors.push(error.message))
    const api = { user: rh, handler: null, calls: [] }
    await page.route('**/api/**', async route => {
      const request = route.request()
      const path = new URL(request.url()).pathname
      api.calls.push(`${request.method()} ${path}`)
      // Permite atrasar ou rejeitar somente a chamada relevante de cada teste
      if (api.handler && await api.handler(route, path, request)) return
      if (path === '/api/health') return respond(route, 200, { status: 'UP' })
      if (path.endsWith('/csrf')) return respond(route, 200, { headerName: 'X-CSRF-TOKEN', token: 'csrf-ficticio' })
      if (path.endsWith('/me')) return respond(route, api.user ? 200 : 401, api.user ?? {})
      if (path.endsWith('/logout')) { api.user = null; return respond(route, 204) }
      if (path.endsWith('/login')) { api.user = { ...rh, nome: 'Novo acesso fictício' }; return respond(route, 204) }
      if (path === '/api/public/vagas') return respond(route, 200, [])
      if (path === '/api/rh/vagas') return respond(route, 200, [vaga])
      if (path === '/api/rh/vagas/7') return respond(route, 200, vaga)
      return respond(route, 404, { message: 'Caminho não esperado no teste' })
    })
    await page.goto(baseUrl)
    await visible(page.getByText('Olá, RH fictício.'))
    await run(page, api)
    assert.deepEqual(errors, [])
  })
}

// Confirma que um 401 de vagas remove a conta e os controles administrativos juntos
uiTest('401 em vagas atualiza a sessão compartilhada', async (page, api) => {
  // Uma confirmação anterior também precisa deixar de aparecer após o 401
  await page.getByRole('button', { name: 'Conferir sessão', exact: true }).click()
  await visible(page.getByText('Sua sessão está ativa.', { exact: true }))
  api.handler = async (route, path) => {
    if (path !== '/api/rh/vagas') return false
    await respond(route, 401, { message: 'Entre para continuar' })
    return true
  }
  await page.getByRole('button', { name: 'Gerenciar vagas', exact: true }).click()
  await visible(page.getByLabel('E-mail', { exact: true }))
  assert.equal(await page.getByText('Olá, RH fictício.').count(), 0)
  assert.equal(await page.getByText('Sua sessão está ativa.', { exact: true }).count(), 0)
  assert.equal(await page.getByRole('button', { name: 'Gerenciar vagas', exact: true }).count(), 0)
})

// Falhas de permissão ou comunicação não confirmam que a conta saiu
for (const status of [403, 503, 'network', 'timeout']) {
  uiTest(`${status} em vagas preserva a sessão e permite logout`, async (page, api) => {
    // Encurta somente o prazo do teste mantendo o AbortSignal verdadeiro
    if (status === 'timeout') await page.evaluate(() => {
      const timeout = AbortSignal.timeout.bind(AbortSignal)
      AbortSignal.timeout = milliseconds => timeout(milliseconds === 10000 ? 30 : milliseconds)
    })
    api.handler = async (route, path) => {
      if (path !== '/api/rh/vagas') return false
      if (status === 'network') await route.abort('failed')
      else if (status === 'timeout') {
        await new Promise(resolve => setTimeout(resolve, 100))
        await respond(route, 200, [vaga]).catch(() => {})
      } else await respond(route, status, { message: `Falha ${status}` })
      return true
    }
    await page.getByRole('button', { name: 'Gerenciar vagas', exact: true }).click()
    await visible(page.getByRole('alert').first())
    assert.equal(await page.getByText('Olá, RH fictício.').count(), 1)
    assert.equal(api.calls.filter(call => call === 'GET /api/rh/vagas').length, 1)
    await page.getByRole('button', { name: 'Sair da conta', exact: true }).click()
    await visible(page.getByLabel('E-mail', { exact: true }))
  })
}

// Uma consulta antiga de sessão não pode desfazer o 401 recebido depois por vagas
uiTest('resposta antiga de /me não restaura sessão invalidada por vagas', async (page, api) => {
  const held = Promise.withResolvers()
  const started = Promise.withResolvers()
  api.handler = async (route, path) => {
    if (path.endsWith('/me')) {
      started.resolve()
      await held.promise
      await respond(route, 200, rh)
      return true
    }
    if (path === '/api/rh/vagas') { await respond(route, 401); return true }
    return false
  }
  await page.evaluate(() => window.dispatchEvent(new Event('focus')))
  await started.promise
  await page.getByRole('button', { name: 'Gerenciar vagas', exact: true }).click()
  await visible(page.getByLabel('E-mail', { exact: true }))
  // Aguarda a resposta atrasada terminar de passar pelo fluxo React
  const response = page.waitForResponse('**/api/auth/me')
  held.resolve()
  await response
  await page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve))))
  assert.equal(await page.getByText('Olá, RH fictício.').count(), 0)
  assert.equal(await page.getByLabel('E-mail', { exact: true }).count(), 1)
})

// Reutilizar a mesma conta no novo login não torna válidas respostas da sessão anterior
for (const status of [401, 200]) {
  uiTest(`resposta ${status} antiga de vagas não interfere em novo login da mesma conta`, async (page, api) => {
    const held = Promise.withResolvers()
    const started = Promise.withResolvers()
    let first = true
    api.handler = async (route, path) => {
      if (path !== '/api/rh/vagas' || !first) return false
      first = false
      started.resolve()
      await held.promise
      await respond(route, status, status === 200 ? [{ ...vaga, titulo: 'Resposta administrativa antiga' }] : {})
      return true
    }
    await page.getByRole('button', { name: 'Gerenciar vagas', exact: true }).click()
    await started.promise
    await page.getByRole('button', { name: 'Sair da conta', exact: true }).click()
    await visible(page.getByLabel('E-mail', { exact: true }))
    await page.getByLabel('E-mail', { exact: true }).fill(rh.email)
    await page.getByLabel('Senha', { exact: true }).fill('Senha-ficticia-2026!')
    await page.getByRole('button', { name: 'Acessar conta', exact: true }).click()
    await visible(page.getByText('Olá, Novo acesso fictício.'))
    const response = page.waitForResponse('**/api/rh/vagas*')
    held.resolve()
    await response
    await page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve))))
    assert.equal(await page.getByText('Olá, Novo acesso fictício.').count(), 1)
    assert.equal(await page.getByRole('button', { name: 'Gerenciar vagas', exact: true }).count(), 1)
    assert.equal(await page.getByText('Resposta administrativa antiga', { exact: true }).count(), 0)
  })
}

// Um 401 já em trânsito não confirma logout enquanto a saída ainda está sem resposta
uiTest('401 antigo durante logout não mascara falha de comunicação da saída', async (page, api) => {
  const list = Promise.withResolvers()
  const logout = Promise.withResolvers()
  const started = Promise.withResolvers()
  api.handler = async (route, path) => {
    if (path === '/api/rh/vagas') {
      started.resolve()
      await list.promise
      await respond(route, 401)
      return true
    }
    if (path.endsWith('/logout')) {
      await logout.promise
      await respond(route, 503)
      return true
    }
    return false
  }
  await page.getByRole('button', { name: 'Gerenciar vagas', exact: true }).click()
  await started.promise
  const exiting = page.waitForRequest('**/api/auth/logout')
  await page.getByRole('button', { name: 'Sair da conta', exact: true }).click()
  await exiting
  const response = page.waitForResponse('**/api/rh/vagas*')
  list.resolve()
  await response
  assert.equal(await page.getByLabel('E-mail', { exact: true }).count(), 0)
  logout.resolve()
  await visible(page.getByText('Último acesso confirmado: RH fictício.'))
  await visible(page.getByRole('button', { name: 'Tentar novamente', exact: true }))
})

// Usa os dois formatos de nomes de campo retornados pelo backend sem perder o formulário
for (const editing of [false, true]) {
  uiTest(`400 na ${editing ? 'edição' : 'criação'} mostra erros por campo e preserva valores`, async (page, api) => {
    const held = Promise.withResolvers()
    const started = Promise.withResolvers()
    api.handler = async (route, path, request) => {
      if (!['POST', 'PUT'].includes(request.method()) || !path.startsWith('/api/rh/vagas')) return false
      const prefix = editing ? 'dados.' : ''
      started.resolve()
      await held.promise
      await respond(route, 400, { message: 'Confira os campos informados.', errors: {
        [`${prefix}titulo`]: 'Revise o título fictício', [`${prefix}requisitos[0].descricao`]: 'Revise o requisito fictício',
        [`${prefix}requisitos[0].tipo`]: 'Revise a classificação fictícia',
      } })
      return true
    }
    await page.getByRole('button', { name: 'Gerenciar vagas', exact: true }).click()
    if (editing) {
      await page.getByRole('button', { name: /Vaga fictícia · PUBLICADA/ }).click()
      await page.getByRole('button', { name: 'Editar vaga', exact: true }).click()
    } else {
      await page.getByRole('button', { name: 'Nova vaga', exact: true }).click()
      await page.getByRole('button', { name: 'Adicionar requisito', exact: true }).click()
    }
    await page.getByLabel('Título', { exact: true }).fill('Título preenchido')
    await page.getByLabel('Requisito 1', { exact: true }).fill('Requisito preenchido')
    await page.getByRole('button', { name: 'Salvar vaga', exact: true }).click()
    await started.promise
    // Revalidar a mesma sessão por foco não descarta a resposta do formulário em andamento
    const inspection = page.waitForResponse('**/api/auth/me')
    await page.evaluate(() => window.dispatchEvent(new Event('focus')))
    await inspection
    held.resolve()
    await visible(page.getByText('Revise o título fictício', { exact: true }))
    await visible(page.getByText('Revise o requisito fictício', { exact: true }))
    await visible(page.getByText('Revise a classificação fictícia', { exact: true }))
    assert.equal(await page.getByLabel('Título', { exact: true }).inputValue(), 'Título preenchido')
    assert.equal(await page.getByLabel('Requisito 1', { exact: true }).inputValue(), 'Requisito preenchido')
    assert.equal(await page.getByLabel('Título', { exact: true }).getAttribute('aria-invalid'), 'true')
    assert.equal(await page.getByLabel('Requisito 1', { exact: true }).getAttribute('aria-invalid'), 'true')
  })
}
