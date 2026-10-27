import { useState } from 'react'
import type { FormEvent } from 'react'
import { Button, Field, Flex, Input, NativeSelect, Stack, Text, Textarea } from '@chakra-ui/react'
import type { DadosVaga, Requisito } from './api'

type Props = { initial: DadosVaga; pending: boolean; fields: Record<string, string>; onClearErrors: () => void; onSave: (dados: DadosVaga) => Promise<void>; onCancel: () => void }

// Edita somente os dados que o módulo permite enviar ao servidor
export function VagaForm({ initial, pending, fields, onClearErrors, onSave, onCancel }: Props) {
  const [dados, setDados] = useState<DadosVaga>(initial)

  // Aceita os nomes de campo usados tanto na criação quanto na edição
  function error(key: string) {
    return fields[`dados.${key}`] ?? fields[key]
  }

  // Atualiza um campo sem substituir os demais valores do formulário
  function field(key: keyof Omit<DadosVaga, 'requisitos'>, value: string) {
    setDados(previous => ({ ...previous, [key]: key === 'titulo' ? value : value || null }))
  }

  // Atualiza o texto ou a classificação do requisito escolhido
  function requisito(index: number, change: Partial<Requisito>) {
    setDados(previous => ({ ...previous, requisitos: previous.requisitos.map((item, position) =>
      position === index ? { ...item, ...change } : item) }))
  }

  // Mantém o envio no fluxo controlado pelo painel de vagas
  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    void onSave(dados)
  }

  // Mostra os campos do rascunho e a lista ordenada de requisitos
  return (
    <form onSubmit={submit}>
      <Stack gap="4">
        <Text fontSize="sm">Para publicar, preencha área, descrição e modalidade. Vagas presenciais ou híbridas também precisam de cidade e UF.</Text>
        <Field.Root required invalid={Boolean(error('titulo'))}><Field.Label htmlFor="vaga-titulo">Título</Field.Label>
          <Input id="vaga-titulo" value={dados.titulo} maxLength={150} required disabled={pending}
            onChange={event => field('titulo', event.target.value)} />
          <Field.ErrorText>{error('titulo')}</Field.ErrorText></Field.Root>
        <Field.Root invalid={Boolean(error('area'))}><Field.Label htmlFor="vaga-area">Área</Field.Label>
          <Input id="vaga-area" value={dados.area ?? ''} maxLength={100} disabled={pending}
            onChange={event => field('area', event.target.value)} />
          <Field.ErrorText>{error('area')}</Field.ErrorText></Field.Root>
        <Field.Root invalid={Boolean(error('descricao'))}><Field.Label htmlFor="vaga-descricao">Descrição</Field.Label>
          <Textarea id="vaga-descricao" value={dados.descricao ?? ''} maxLength={10000} disabled={pending}
            onChange={event => field('descricao', event.target.value)} />
          <Field.ErrorText>{error('descricao')}</Field.ErrorText></Field.Root>
        <Field.Root invalid={Boolean(error('modalidade'))}><Field.Label htmlFor="vaga-modalidade">Modalidade</Field.Label>
          <NativeSelect.Root disabled={pending}><NativeSelect.Field id="vaga-modalidade" value={dados.modalidade ?? ''}
            onChange={event => field('modalidade', event.target.value)}>
            <option value="">Selecionar</option><option value="PRESENCIAL">Presencial</option>
            <option value="HIBRIDO">Híbrido</option><option value="REMOTO">Remoto</option>
          </NativeSelect.Field><NativeSelect.Indicator /></NativeSelect.Root>
          <Field.ErrorText>{error('modalidade')}</Field.ErrorText></Field.Root>
        <Flex gap="3"><Field.Root invalid={Boolean(error('cidade'))}><Field.Label htmlFor="vaga-cidade">Cidade</Field.Label>
          <Input id="vaga-cidade" value={dados.cidade ?? ''} maxLength={100} disabled={pending}
            onChange={event => field('cidade', event.target.value)} />
          <Field.ErrorText>{error('cidade')}</Field.ErrorText></Field.Root>
          <Field.Root maxW="24" invalid={Boolean(error('estado'))}><Field.Label htmlFor="vaga-estado">UF</Field.Label>
            <Input id="vaga-estado" value={dados.estado ?? ''} maxLength={2} pattern="[A-Z]{2}" disabled={pending}
              onChange={event => field('estado', event.target.value.toUpperCase())} />
          <Field.ErrorText>{error('estado')}</Field.ErrorText></Field.Root></Flex>
        <Text fontWeight="semibold">Requisitos</Text>
        {error('requisitos') && <Text role="alert" color="red.600">{error('requisitos')}</Text>}
        {/* Mantém a mensagem de cada requisito junto ao campo correspondente */}
        {dados.requisitos.map((item, index) => (
          <Stack key={index} gap="2" borderWidth="1px" p="3">
            <Field.Root required invalid={Boolean(error(`requisitos[${index}].descricao`))}><Field.Label htmlFor={`requisito-${index}`}>Requisito {index + 1}</Field.Label>
              <Input id={`requisito-${index}`} value={item.descricao} maxLength={255} required disabled={pending}
                onChange={event => requisito(index, { descricao: event.target.value })} />
              <Field.ErrorText>{error(`requisitos[${index}].descricao`)}</Field.ErrorText></Field.Root>
            <Field.Root invalid={Boolean(error(`requisitos[${index}].tipo`))}><Field.Label htmlFor={`tipo-${index}`}>Classificação {index + 1}</Field.Label>
              <NativeSelect.Root disabled={pending}><NativeSelect.Field id={`tipo-${index}`} value={item.tipo}
                onChange={event => requisito(index, { tipo: event.target.value as Requisito['tipo'] })}>
                <option value="OBRIGATORIO">Obrigatório</option><option value="DESEJAVEL">Desejável</option>
              </NativeSelect.Field><NativeSelect.Indicator /></NativeSelect.Root>
              <Field.ErrorText>{error(`requisitos[${index}].tipo`)}</Field.ErrorText></Field.Root>
            {/* Remove o item e limpa mensagens que usavam a posição anterior da lista */}
            <Button type="button" variant="ghost" alignSelf="start" disabled={pending}
              onClick={() => { onClearErrors(); setDados(previous => ({ ...previous, requisitos: previous.requisitos.filter((_, position) => position !== index) })) }}>
              Remover requisito {index + 1}</Button>
          </Stack>
        ))}
        {/* Acrescenta um requisito obrigatório sem alterar os que já foram preenchidos */}
        <Button type="button" variant="outline" alignSelf="start" disabled={pending || dados.requisitos.length >= 100}
          onClick={() => { onClearErrors(); setDados(previous => ({ ...previous, requisitos: [...previous.requisitos, { descricao: '', tipo: 'OBRIGATORIO' }] })) }}>
          Adicionar requisito</Button>
        {/* Envia os dados atuais ou fecha o formulário sem salvar */}
        <Flex gap="3"><Button type="submit" bg="brand.blue" color="gray.950" loading={pending}>Salvar vaga</Button>
          <Button type="button" variant="outline" disabled={pending} onClick={onCancel}>Cancelar</Button></Flex>
      </Stack>
    </form>
  )
}
