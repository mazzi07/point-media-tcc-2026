import { AuthError, ensureCsrf, refreshCsrf } from '../auth/api.ts'

export type Requisito = { descricao: string; tipo: 'OBRIGATORIO' | 'DESEJAVEL' }
export type DadosVaga = {
  titulo: string; area: string | null; descricao: string | null
  modalidade: 'PRESENCIAL' | 'HIBRIDO' | 'REMOTO' | null
  cidade: string | null; estado: string | null; requisitos: Requisito[]
}
export type VagaPublica = DadosVaga & { id: number; publicadaEm: string | null }
export type VagaAdmin = VagaPublica & {
  status: 'RASCUNHO' | 'PUBLICADA' | 'ENCERRADA' | 'ARQUIVADA'; versao: number; criadaPor: number
}
export type ResumoVaga = {
  id: number; titulo: string; area?: string; modalidade?: string; cidade?: string; estado?: string
  status?: VagaAdmin['status']; versao?: number
}

// Encaminha leituras e escritas ao módulo usando a sessão existente
async function request<T>(path: string, method = 'GET', body?: unknown): Promise<T> {
  // Repete somente uma escrita rejeitada antes de ser executada por CSRF inválido
  for (let attempt = 0; attempt < 2; attempt++) {
    const headers: Record<string, string> = { Accept: 'application/json' }
    if (method !== 'GET') {
      const csrf = await ensureCsrf()
      headers[csrf.headerName] = csrf.token
      headers['Content-Type'] = 'application/json'
    }
    let response: Response
    try {
      // Mantém os cookies na mesma origem e limita a duração da chamada
      response = await fetch(`/api/${path}`, { method, headers, credentials: 'same-origin',
        body: body === undefined ? undefined : JSON.stringify(body), signal: AbortSignal.timeout(10000) })
    } catch {
      throw new AuthError(0, 'Não foi possível confirmar a operação. Atualize a consulta antes de tentar salvar novamente.')
    }
    // Devolve a mensagem segura da API sem tentar repetir conflitos ou falhas de rede
    if (!response.ok) {
      const error = await response.json().catch(() => ({}))
      if (method !== 'GET' && attempt === 0 && error.code === 'CSRF_INVALIDO') {
        await refreshCsrf()
        continue
      }
      throw new AuthError(response.status, error.message ?? 'Não foi possível consultar as vagas.', error.code, error.errors)
    }
    return response.json() as Promise<T>
  }
  throw new AuthError(403, 'Atualize a página e tente novamente.')
}

// Consulta as vagas publicadas ou a lista administrativa autorizada
export function listar(admin: boolean, pagina: number) {
  return request<ResumoVaga[]>(`${admin ? 'rh' : 'public'}/vagas?pagina=${pagina}`)
}

// Busca os detalhes permitidos para o tipo de acesso atual
export function consultar(admin: boolean, id: number) {
  return request<VagaAdmin | VagaPublica>(`${admin ? 'rh' : 'public'}/vagas/${id}`)
}

// Cria o rascunho ou salva a edição com a versão recebida do servidor
export function salvar(dados: DadosVaga, atual: VagaAdmin | null) {
  // Retira os campos administrativos recebidos na consulta antes de enviar o formulário
  const editaveis: DadosVaga = { titulo: dados.titulo, area: dados.area, descricao: dados.descricao,
    modalidade: dados.modalidade, cidade: dados.cidade, estado: dados.estado,
    requisitos: dados.requisitos.map(item => ({ descricao: item.descricao, tipo: item.tipo })) }
  return atual ? request<VagaAdmin>(`rh/vagas/${atual.id}`, 'PUT', { versao: atual.versao, dados: editaveis })
    : request<VagaAdmin>('rh/vagas', 'POST', editaveis)
}

// Solicita uma mudança de estado usando a versão da vaga exibida
export function alterarEstado(vaga: VagaAdmin, acao: 'publicar' | 'encerrar' | 'arquivar') {
  return request<VagaAdmin>(`rh/vagas/${vaga.id}/${acao}`, 'POST', { versao: vaga.versao })
}
