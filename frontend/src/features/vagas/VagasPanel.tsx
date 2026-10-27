import { useEffect, useRef, useState } from 'react'
import { Box, Button, Flex, Heading, Stack, Text } from '@chakra-ui/react'
import { AuthError } from '../auth/api'
import type { SessionControl } from '../auth/useSession'
import * as api from './api'
import { VagaForm } from './VagaForm'

const vazio: api.DadosVaga = { titulo: '', area: null, descricao: null, modalidade: null, cidade: null, estado: null, requisitos: [] }

// Reúne a consulta pública e as ações da equipe sem substituir a autorização do servidor
export function VagasPanel({ control }: { control: SessionControl }) {
  const { session, capture, isCurrent, expire } = control
  const canManage = session.status === 'authenticated' && ['RH', 'GESTOR'].includes(session.user.perfil)
  const [admin, setAdmin] = useState(false)
  const administrative = admin && canManage
  const [items, setItems] = useState<api.ResumoVaga[]>([])
  const [selected, setSelected] = useState<api.VagaPublica | api.VagaAdmin | null>(null)
  const [editing, setEditing] = useState(false)
  const [pagina, setPagina] = useState(0)
  const [revision, setRevision] = useState(0)
  const [loading, setLoading] = useState(true)
  const [pending, setPending] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [fields, setFields] = useState<Record<string, string>>({})
  const [blocked, setBlocked] = useState(false)
  const generation = useRef(0)
  const selectedAdmin = administrative && selected && 'versao' in selected ? selected : null
  const busy = pending || control.changing

  // Descarta conteúdo administrativo quando a conta ou o estado da sessão muda
  useEffect(() => {
    generation.current++
    setSelected(null)
    setEditing(false)
    setBlocked(false)
    setError(null)
    setFields({})
    setPending(false)
  }, [administrative, session.user?.id, session.status])

  // Consulta a página escolhida e ignora respostas de uma seleção antiga
  useEffect(() => {
    let active = true
    const sessionRequest = capture()
    setLoading(true)
    setItems([])
    api.listar(administrative, pagina).then(result => {
      if (active && isCurrent(sessionRequest)) setItems(result)
    }).catch(failure => { if (active && isCurrent(sessionRequest)) fail(failure, sessionRequest) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [administrative, pagina, revision, session.user?.id, capture, isCurrent, expire])

  // Exibe o erro e retira detalhes administrativos quando o acesso é negado
  function fail(failure: unknown, sessionRequest: number) {
    setError(failure instanceof AuthError ? failure.message : 'Não foi possível concluir a operação.')
    // Mantém os dados digitados e encaminha as mensagens de validação ao formulário
    if (failure instanceof AuthError && failure.status === 400) setFields(failure.fields)
    // Um 401 confirma a sessão inválida para os dois painéis
    if (failure instanceof AuthError && failure.status === 401) expire(sessionRequest)
    // Falta de permissão bloqueia este painel sem encerrar a conta
    if (failure instanceof AuthError && [401, 403].includes(failure.status)) {
      setBlocked(true)
      setSelected(null)
      setEditing(false)
    }
  }

  // Abre a vaga sem aceitar uma resposta de uma seleção anterior
  async function open(id: number) {
    const request = ++generation.current
    const sessionRequest = capture()
    setPending(true)
    setError(null)
    setFields({})
    try {
      const vaga = await api.consultar(administrative, id)
      if (request === generation.current && isCurrent(sessionRequest)) { setSelected(vaga); setEditing(false) }
    } catch (failure) { if (request === generation.current && isCurrent(sessionRequest)) fail(failure, sessionRequest) }
    finally { if (request === generation.current) setPending(false) }
  }

  // Salva sem repetir automaticamente uma operação cujo resultado ficou incerto
  async function save(dados: api.DadosVaga) {
    const request = ++generation.current
    const sessionRequest = capture()
    setPending(true)
    setError(null)
    setFields({})
    try {
      const vaga = await api.salvar(dados, selectedAdmin)
      // Ignora a conclusão de uma ação se a conta mudou durante a chamada
      if (request !== generation.current || !isCurrent(sessionRequest)) return
      setSelected(vaga)
      setEditing(false)
      setRevision(value => value + 1)
    } catch (failure) { if (request === generation.current && isCurrent(sessionRequest)) fail(failure, sessionRequest) }
    finally { if (request === generation.current) setPending(false) }
  }

  // Usa a versão exibida nas mudanças de estado e recarrega a lista após sucesso
  async function change(acao: 'publicar' | 'encerrar' | 'arquivar') {
    if (!selectedAdmin) return
    const request = ++generation.current
    const sessionRequest = capture()
    setPending(true)
    setError(null)
    setFields({})
    try {
      const vaga = await api.alterarEstado(selectedAdmin, acao)
      // Não reabre uma vaga administrativa após logout ou troca de acesso
      if (request !== generation.current || !isCurrent(sessionRequest)) return
      setSelected(vaga)
      setRevision(value => value + 1)
    } catch (failure) { if (request === generation.current && isCurrent(sessionRequest)) fail(failure, sessionRequest) }
    finally { if (request === generation.current) setPending(false) }
  }

  // Alterna a consulta sem reaproveitar a paginação do outro tipo de acesso
  function mode(value: boolean) {
    setAdmin(value)
    setPagina(0)
    setSelected(null)
    setEditing(false)
    setFields({})
  }

  // Mantém os controles de edição separados dos detalhes públicos
  return (
    <Box as="section" aria-labelledby="vagas-heading" mt="8" borderWidth="1px" p={{ base: '5', md: '8' }}>
      <Heading id="vagas-heading" as="h2" size="lg" mb="4">Vagas</Heading>
      <Flex gap="3" mb="4" flexWrap="wrap">
        <Button variant="outline" disabled={busy} onClick={() => mode(false)}>Vagas publicadas</Button>
        {canManage && <Button variant="outline" borderColor="brand.pink" disabled={busy}
          onClick={() => mode(true)}>Gerenciar vagas</Button>}
        {/* Limpa a falha anterior e tenta carregar a lista outra vez */}
        <Button variant="ghost" disabled={busy} onClick={() => { setError(null); setBlocked(false); setRevision(value => value + 1) }}>Atualizar lista</Button>
      </Flex>
      {error && <Box role="alert" bg="red.50" p="3" mb="4"><Text>{error}</Text>
        {selected && <Button variant="outline" mt="2" disabled={busy} onClick={() => void open(selected.id)}>
          Recarregar vaga e descartar alterações</Button>}</Box>}
      {blocked ? <Text>Acesso negado. Confirme sua sessão antes de tentar novamente.</Text> : (
        <Stack gap="5">
          {/* Abre um rascunho vazio sem enviar nada ao servidor */}
          {administrative && <Button alignSelf="start" bg="brand.blue" color="gray.950" disabled={busy}
            onClick={() => { setSelected(null); setEditing(true); setError(null); setFields({}) }}>Nova vaga</Button>}
          {loading ? <Text role="status">Carregando vagas…</Text> : items.length === 0 ? <Text>Nenhuma vaga encontrada.</Text> : (
            <Stack gap="2">{items.map(item => <Button key={item.id} variant="outline" justifyContent="start" h="auto" py="3"
              whiteSpace="normal" disabled={busy} onClick={() => void open(item.id)}>
              {item.titulo}{administrative ? ` · ${item.status}` : item.area ? ` · ${item.area}` : ''}</Button>)}</Stack>
          )}
          {/* Avança ou volta uma página mantendo a consulta escolhida */}
          <Flex gap="3"><Button variant="ghost" disabled={pagina === 0 || loading || busy} onClick={() => setPagina(value => value - 1)}>Anterior</Button>
            <Text alignSelf="center">Página {pagina + 1}</Text><Button variant="ghost" disabled={items.length < 20 || loading || busy}
              onClick={() => setPagina(value => value + 1)}>Próxima</Button></Flex>
          {/* Mantém o formulário aberto após validação ou conflito até uma recarga explícita */}
          {editing && administrative ? <VagaForm key={selectedAdmin ? `${selectedAdmin.id}-${selectedAdmin.versao}` : 'nova'}
            initial={selectedAdmin ?? vazio} pending={busy} fields={fields} onClearErrors={() => setFields({})} onSave={save} onCancel={() => setEditing(false)} /> : selected && (
            <Box borderTopWidth="1px" pt="4">
              <Heading as="h3" size="md">{selected.titulo}</Heading>
              <Text mt="2">{selected.area} · {selected.modalidade} {selected.cidade} {selected.estado}</Text>
              <Text mt="3" whiteSpace="pre-wrap">{selected.descricao}</Text>
              <Stack gap="2" mt="4">{selected.requisitos.map((item, index) => <Text key={index}>
                {item.tipo === 'OBRIGATORIO' ? 'Obrigatório' : 'Desejável'}: {item.descricao}</Text>)}</Stack>
              {selectedAdmin && <Stack gap="3" mt="4"><Text>Estado: {selectedAdmin.status} · Versão {selectedAdmin.versao}</Text>
                {/* Oferece somente as ações previstas para o estado exibido */}
                <Flex gap="3" flexWrap="wrap">
                  {['RASCUNHO', 'PUBLICADA'].includes(selectedAdmin.status) && <Button variant="outline" disabled={busy} onClick={() => setEditing(true)}>Editar vaga</Button>}
                  {selectedAdmin.status === 'RASCUNHO' && <Button bg="brand.blue" color="gray.950" disabled={busy} onClick={() => void change('publicar')}>Publicar vaga</Button>}
                  {selectedAdmin.status === 'PUBLICADA' && <Button variant="outline" disabled={busy} onClick={() => void change('encerrar')}>Encerrar vaga</Button>}
                  {selectedAdmin.status !== 'ARQUIVADA' && <Button variant="outline" borderColor="brand.pink" disabled={busy} onClick={() => void change('arquivar')}>Arquivar vaga</Button>}
                </Flex></Stack>}
            </Box>
          )}
        </Stack>
      )}
    </Box>
  )
}
