import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { PageHeader } from '../../components/layout/PageHeader'
import { Button } from '../../components/ui/Button'
import { Card } from '../../components/ui/Card'
import { ConfirmModal } from '../../components/ui/ConfirmModal'
import { EmptyState } from '../../components/ui/EmptyState'
import { Input } from '../../components/ui/Input'
import { Modal } from '../../components/ui/Modal'
import { Select } from '../../components/ui/Select'
import { Spinner } from '../../components/ui/Spinner'
import { useToast } from '../../hooks/useToast'
import { instrumentoService } from '../../services/instrumentoService'
import type { InstrumentoResponse } from '../../types/api'
import type { CategoriaInstrumento } from '../../types/enums'
import { CATEGORIA_INSTRUMENTO_LABEL } from '../../types/enums'

const CATEGORIAS: CategoriaInstrumento[] = ['CORDA', 'PERCUSSAO', 'SOPRO', 'TECLA', 'VOCAL']

function PlusIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M12 5v14M5 12h14" />
    </svg>
  )
}

function XIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M6 6l12 12M18 6L6 18" />
    </svg>
  )
}

function agruparPorCategoria(instrumentos: InstrumentoResponse[]) {
  const grupos = new Map<CategoriaInstrumento, InstrumentoResponse[]>()
  for (const instrumento of instrumentos) {
    const lista = grupos.get(instrumento.categoria) ?? []
    lista.push(instrumento)
    grupos.set(instrumento.categoria, lista)
  }
  return Array.from(grupos.entries())
    .map(([categoria, lista]) => [categoria, [...lista].sort((a, b) => a.nome.localeCompare(b.nome))] as const)
    .sort(([a], [b]) => CATEGORIA_INSTRUMENTO_LABEL[a].localeCompare(CATEGORIA_INSTRUMENTO_LABEL[b]))
}

export function InstrumentosPage() {
  const [instrumentos, setInstrumentos] = useState<InstrumentoResponse[] | null>(null)
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState(false)
  const showToast = useToast()

  const [modalCriarAberto, setModalCriarAberto] = useState(false)
  const [nome, setNome] = useState('')
  const [categoria, setCategoria] = useState<CategoriaInstrumento>('CORDA')
  const [criando, setCriando] = useState(false)

  const [paraExcluir, setParaExcluir] = useState<InstrumentoResponse | null>(null)
  const [excluindo, setExcluindo] = useState(false)

  function carregar() {
    setCarregando(true)
    setErro(false)
    instrumentoService
      .listar()
      .then(setInstrumentos)
      .catch(() => setErro(true))
      .finally(() => setCarregando(false))
  }

  useEffect(carregar, [])

  function abrirModalCriar() {
    setNome('')
    setCategoria('CORDA')
    setModalCriarAberto(true)
  }

  async function handleCriar(event: FormEvent) {
    event.preventDefault()
    setCriando(true)
    try {
      await instrumentoService.criar({ nome, categoria })
      showToast('Instrumento criado', 'success')
      setModalCriarAberto(false)
      carregar()
    } finally {
      setCriando(false)
    }
  }

  async function handleExcluir() {
    if (!paraExcluir) return
    setExcluindo(true)
    try {
      await instrumentoService.remover(paraExcluir.id)
      showToast('Instrumento removido', 'success')
      setParaExcluir(null)
      carregar()
    } finally {
      setExcluindo(false)
    }
  }

  const grupos = instrumentos ? agruparPorCategoria(instrumentos) : []

  return (
    <div className="relative min-h-screen">
      <PageHeader title="Instrumentos" />

      <div className="flex flex-col gap-5 px-4 py-4">
        {carregando && (
          <div className="flex justify-center py-12">
            <Spinner />
          </div>
        )}

        {!carregando && erro && (
          <EmptyState title="Não deu pra carregar os instrumentos" description="Verifique sua conexão e tente de novo." />
        )}

        {!carregando && !erro && grupos.length === 0 && <EmptyState title="Nenhum instrumento cadastrado" />}

        {!carregando &&
          !erro &&
          grupos.map(([categoriaGrupo, lista]) => (
            <div key={categoriaGrupo}>
              <h2 className="mb-2 text-xs font-medium uppercase tracking-wide text-text-secondary">
                {CATEGORIA_INSTRUMENTO_LABEL[categoriaGrupo]}
              </h2>
              <div className="flex flex-col gap-2">
                {lista.map((instrumento) => (
                  <Card key={instrumento.id} className="flex items-center justify-between gap-3">
                    <p className="text-text-primary">{instrumento.nome}</p>
                    <button
                      type="button"
                      aria-label={`Remover ${instrumento.nome}`}
                      onClick={() => setParaExcluir(instrumento)}
                      className="text-text-secondary transition active:scale-95"
                    >
                      <XIcon />
                    </button>
                  </Card>
                ))}
              </div>
            </div>
          ))}
      </div>

      <div className="fixed inset-x-0 bottom-24 z-50 mx-auto flex max-w-md justify-end pr-4">
        <button
          type="button"
          aria-label="Criar instrumento"
          onClick={abrirModalCriar}
          className="flex h-14 w-14 items-center justify-center rounded-full bg-accent text-text-primary shadow-lg transition active:scale-95"
        >
          <PlusIcon />
        </button>
      </div>

      <Modal isOpen={modalCriarAberto} onClose={() => setModalCriarAberto(false)} title="Novo instrumento">
        <form onSubmit={handleCriar} className="flex flex-col gap-4">
          <Input label="Nome" required value={nome} onChange={(e) => setNome(e.target.value)} />
          <Select
            label="Categoria"
            value={categoria}
            onChange={(e) => setCategoria(e.target.value as CategoriaInstrumento)}
          >
            {CATEGORIAS.map((valor) => (
              <option key={valor} value={valor}>
                {CATEGORIA_INSTRUMENTO_LABEL[valor]}
              </option>
            ))}
          </Select>
          <Button type="submit" disabled={criando}>
            {criando ? 'Criando...' : 'Criar instrumento'}
          </Button>
        </form>
      </Modal>

      <ConfirmModal
        isOpen={paraExcluir !== null}
        onClose={() => setParaExcluir(null)}
        onConfirm={handleExcluir}
        title="Remover instrumento"
        message={`Tem certeza que quer deletar "${paraExcluir?.nome}"? Esta ação não pode ser desfeita.`}
        confirmLabel="Deletar"
        confirming={excluindo}
      />
    </div>
  )
}
