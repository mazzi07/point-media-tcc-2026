import { useCallback, useRef, useState } from 'react'
import type { SessionState } from './session'

// Mantém uma única sessão para os painéis e identifica as chamadas que ainda valem
export function useSession() {
  const [session, setSession] = useState<SessionState>({ status: 'unverified', user: null })
  const [changing, setChanging] = useState(false)
  const revision = useRef(0)
  const action = useRef(false)

  // Invalida respostas anteriores antes de uma ação de autenticação
  const begin = useCallback(() => {
    action.current = true
    setChanging(true)
    return ++revision.current
  }, [])

  // Aplica somente o resultado da operação de sessão mais recente
  const update = useCallback((next: SessionState, expected: number) => {
    if (expected !== revision.current) return false
    setSession(next)
    return true
  }, [])

  // Libera os controles sem interferir em uma operação iniciada depois
  const finish = useCallback((expected: number) => {
    if (expected !== revision.current) return
    action.current = false
    setChanging(false)
  }, [])

  // Captura a sessão à qual uma chamada de vagas pertence
  const capture = useCallback(() => revision.current, [])

  // Recusa respostas antigas ou recebidas enquanto login ou logout estão em andamento
  const isCurrent = useCallback((expected: number) => expected === revision.current && !action.current, [])

  // Compartilha o 401 confirmado sem deixar uma chamada antiga encerrar um novo login
  const expire = useCallback((expected: number) => {
    if (expected !== revision.current || action.current) return
    revision.current++
    setSession({ status: 'anonymous', user: null })
  }, [])

  return { session, changing, begin, update, finish, capture, isCurrent, expire }
}

export type SessionControl = ReturnType<typeof useSession>
