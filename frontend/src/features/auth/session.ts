import * as api from './api.ts'
import type { AuthenticatedUser } from './api.ts'

export type SessionState =
  | { status: 'anonymous'; user: null }
  | { status: 'authenticated'; user: AuthenticatedUser }
  | { status: 'unverified'; user: AuthenticatedUser | null }

// Reconhece somente a confirmação de sessão inválida
function invalidSession(error: unknown) {
  return error instanceof api.AuthError && error.status === 401
}

// Confere a sessão sem tratar falha de comunicação como saída
export async function verifySession(previous: SessionState): Promise<SessionState> {
  try {
    return { status: 'authenticated', user: await api.currentUser() }
  // Distingue sessão inválida de uma resposta que não pôde ser confirmada
  } catch (error) {
    return invalidSession(error)
      ? { status: 'anonymous', user: null }
      : { status: 'unverified', user: previous.user }
  }
}

// Confirma o login sem presumir o resultado de uma resposta perdida
export async function signIn(email: string, senha: string): Promise<SessionState> {
  try {
    await api.login(email, senha)
  // Distingue sessão inválida de uma resposta que não pôde ser confirmada
  } catch (error) {
    // Devolve falhas definitivas do login para o formulário
    if (error instanceof api.AuthError && error.status > 0 && error.status < 500) throw error
    // A resposta pode ter se perdido depois de o servidor autenticar
    return { status: 'unverified', user: null }
  }
  return verifySession({ status: 'unverified', user: null })
}

// Preserva o último usuário até confirmar a saída
export async function signOut(previous: SessionState): Promise<SessionState> {
  try {
    await api.logout()
    return { status: 'anonymous', user: null }
  // Distingue sessão inválida de uma resposta que não pôde ser confirmada
  } catch (error) {
    return invalidSession(error)
      ? { status: 'anonymous', user: null }
      : { status: 'unverified', user: previous.user }
  }
}
