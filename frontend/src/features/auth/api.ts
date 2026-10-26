export type AuthenticatedUser = {
  id: number
  nome: string
  email: string
  perfil: 'USUARIO' | 'RH' | 'GESTOR'
}

type CsrfToken = { headerName: string; token: string }
type ErrorBody = { code?: string; message?: string; errors?: Record<string, string> }

export class AuthError extends Error {
  readonly status: number
  readonly code: string
  readonly fields: Record<string, string>

  // Guarda o erro da API sem expor detalhes internos
  constructor(
    status: number,
    message: string,
    code = '',
    fields: Record<string, string> = {},
  ) {
    super(message)
    this.status = status
    this.code = code
    this.fields = fields
  }
}

let csrfToken: CsrfToken | undefined
let csrfRequest: Promise<CsrfToken> | undefined

// Envia a chamada HTTP com os cookies da mesma origem
async function request(path: string, options?: RequestInit): Promise<Response> {
  let response: Response
  try {
    // Envia cookies e limita o tempo da chamada HTTP
    response = await fetch(`/api/auth/${path}`, {
      ...options,
      credentials: 'same-origin',
      signal: AbortSignal.timeout(10000),
    })
  // Transforma falhas de comunicação em uma mensagem para a tela
  } catch {
    throw new AuthError(0, 'Não foi possível conectar. Confira sua conexão e tente novamente.')
  }

  // Converte respostas de erro para o formato usado pelo painel
  if (!response.ok) {
    const error: ErrorBody = await response.json().catch(() => ({}))
    throw new AuthError(response.status, error.message ?? 'Não foi possível concluir a solicitação.',
      error.code, error.errors)
  }
  return response
}

// Descarta o token anterior antes de buscar outro
export async function refreshCsrf(): Promise<CsrfToken> {
  // Descarta o token vinculado à sessão anterior
  csrfToken = undefined
  return ensureCsrf()
}

// Reutiliza o token ou aguarda uma única busca em andamento
export async function ensureCsrf(): Promise<CsrfToken> {
  // Reutiliza o token já obtido para esta sessão
  if (csrfToken) return csrfToken
  // Compartilha a busca do token entre chamadas simultâneas
  if (!csrfRequest) {
    csrfRequest = request('csrf')
      .then((response) => response.json() as Promise<CsrfToken>)
      .then((token) => { csrfToken = token; return token })
      .finally(() => { csrfRequest = undefined })
  }
  return csrfRequest
}

// Envia uma escrita protegida por CSRF
async function post(path: string, body?: string | URLSearchParams, contentType?: string) {
  // Permite somente uma nova tentativa após rejeição de CSRF
  for (let attempt = 0; attempt < 2; attempt++) {
    const csrf = await ensureCsrf()
    try {
      // Envia o corpo e o token da operação de escrita
      return await request(path, {
        method: 'POST',
        headers: {
          [csrf.headerName]: csrf.token,
          ...(contentType ? { 'Content-Type': contentType } : {}),
        },
        body,
      })
    } catch (error) {
      // Um 403 de CSRF ocorre antes da ação; renovar permite uma única repetição segura
      if (attempt === 0 && error instanceof AuthError && error.code === 'CSRF_INVALIDO') {
        await refreshCsrf()
        continue
      }
      throw error
    }
  }
  throw new AuthError(403, 'Atualize a página e tente novamente.')
}

// Consulta os dados da sessão no backend
export async function currentUser(): Promise<AuthenticatedUser> {
  const response = await request('me')
  return response.json() as Promise<AuthenticatedUser>
}

// Envia somente os campos aceitos no cadastro
export async function register(nome: string, email: string, senha: string) {
  await post('register', JSON.stringify({ nome, email, senha }), 'application/json')
}

// Envia as credenciais ao login nativo do Spring
export async function login(email: string, senha: string) {
  await post('login', new URLSearchParams({ email, senha }), 'application/x-www-form-urlencoded')
  // Descarta o token vinculado à sessão anterior
  csrfToken = undefined
}

// Pede a invalidação da sessão no servidor
export async function logout() {
  await post('logout')
  // Descarta o token vinculado à sessão anterior
  csrfToken = undefined
}
