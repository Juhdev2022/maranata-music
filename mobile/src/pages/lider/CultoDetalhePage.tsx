import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { PageHeader } from '../../components/layout/PageHeader'
import { Badge } from '../../components/ui/Badge'
import { Button } from '../../components/ui/Button'
import { Card } from '../../components/ui/Card'
import { ConfirmModal } from '../../components/ui/ConfirmModal'
import { EmptyState } from '../../components/ui/EmptyState'
import { Modal } from '../../components/ui/Modal'
import { Select } from '../../components/ui/Select'
import { Spinner } from '../../components/ui/Spinner'
import { useToast } from '../../hooks/useToast'
import { cultoService } from '../../services/cultoService'
import { usuarioService } from '../../services/usuarioService'
import type { CultoDetalheResponse, EscalaResumo, UsuarioResponse } from '../../types/api'
import { CATEGORIA_INSTRUMENTO_LABEL, CULTO_TIPO_LABEL, ESCALA_STATUS_LABEL, ESCALA_STATUS_TONE } from '../../types/enums'
import { formatCultoDataHora } from '../../utils/dateFormat'

function XIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M6 6l12 12M18 6L6 18" />
    </svg>
  )
}

export function CultoDetalhePage() {
  const { id } = useParams()
  const cultoId = Number(id)
  const showToast = useToast()

  const [culto, setCulto] = useState<CultoDetalheResponse | null>(null)
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState(false)

  const [observacoesEdit, setObservacoesEdit] = useState('')
  const [salvandoObservacoes, setSalvandoObservacoes] = useState(false)

  const [modalAberto, setModalAberto] = useState(false)
  const [usuarios, setUsuarios] = useState<UsuarioResponse[]>([])
  const [usuarioId, setUsuarioId] = useState('')
  const [escalando, setEscalando] = useState(false)

  const [paraRemover, setParaRemover] = useState<EscalaResumo | null>(null)
  const [removendo, setRemovendo] = useState(false)

  useEffect(() => {
    let cancelado = false
    setCarregando(true)
    setErro(false)
    cultoService
      .detalhar(cultoId)
      .then((data) => {
        if (!cancelado) {
          setCulto(data)
          setObservacoesEdit(data.observacoes ?? '')
        }
      })
      .catch(() => {
        if (!cancelado) setErro(true)
      })
      .finally(() => {
        if (!cancelado) setCarregando(false)
      })
    return () => {
      cancelado = true
    }
  }, [cultoId])

  async function handleSalvarObservacoes() {
    setSalvandoObservacoes(true)
    try {
      const atualizado = await cultoService.atualizarObservacoes(cultoId, { observacoes: observacoesEdit })
      setCulto((atual) => (atual ? { ...atual, observacoes: atualizado.observacoes } : atual))
      showToast('Observações salvas', 'success')
    } finally {
      setSalvandoObservacoes(false)
    }
  }

  function abrirModal() {
    usuarioService.listar({ ativos: true }).then(setUsuarios)
    setUsuarioId('')
    setModalAberto(true)
  }

  async function handleEscalar() {
    setEscalando(true)
    try {
      await cultoService.escalar(cultoId, { usuarioId: Number(usuarioId) })
      const atualizado = await cultoService.detalhar(cultoId)
      setCulto(atualizado)
      showToast('Músico escalado', 'success')
      setModalAberto(false)
    } finally {
      setEscalando(false)
    }
  }

  async function handleRemover() {
    if (!paraRemover) return
    setRemovendo(true)
    try {
      await cultoService.removerEscala(cultoId, paraRemover.id)
      const atualizado = await cultoService.detalhar(cultoId)
      setCulto(atualizado)
      showToast('Músico removido da escala', 'success')
      setParaRemover(null)
    } finally {
      setRemovendo(false)
    }
  }

  if (carregando) {
    return (
      <div className="flex justify-center py-12">
        <Spinner />
      </div>
    )
  }

  if (erro || !culto) {
    return <EmptyState title="Não deu pra carregar o culto" description="Verifique sua conexão e tente de novo." />
  }

  return (
    <div>
      <PageHeader title="Culto" onBack />

      <div className="flex flex-col gap-4 p-4">
        <Card className="flex flex-col gap-3">
          <div>
            <p className="text-2xl text-text-primary">{formatCultoDataHora(culto.dataHora)}</p>
            <p className="text-sm text-text-secondary">{CULTO_TIPO_LABEL[culto.tipo]}</p>
            {culto.ministro && <p className="text-sm text-text-secondary">Ministro: {culto.ministro.nome}</p>}
          </div>

          <div className="flex flex-col gap-1.5">
            <label htmlFor="observacoes" className="text-sm text-text-secondary">
              Observações
            </label>
            <textarea
              id="observacoes"
              rows={3}
              value={observacoesEdit}
              onChange={(e) => setObservacoesEdit(e.target.value)}
              placeholder="Ex.: figurino, horário de ensaio..."
              className="rounded-btn border border-border bg-surface px-3.5 py-2.5 text-text-primary outline-none transition focus:ring-2 focus:ring-accent"
            />
            <Button
              size="sm"
              onClick={handleSalvarObservacoes}
              disabled={salvandoObservacoes || observacoesEdit === (culto.observacoes ?? '')}
              className="self-end"
            >
              {salvandoObservacoes ? 'Salvando...' : 'Salvar observações'}
            </Button>
          </div>
        </Card>

        <div>
          <h2 className="mb-2 font-medium text-text-primary">Equipe escalada</h2>

          {culto.equipe.length === 0 ? (
            <EmptyState title="Ninguém escalado ainda" />
          ) : (
            <div className="flex flex-col gap-2">
              {culto.equipe.map((escala) => (
                <Card key={escala.id} className="flex items-center justify-between gap-3">
                  <div>
                    <p className="text-text-primary">{escala.usuario.nome}</p>
                    <p className="text-sm text-text-secondary">
                      {escala.instrumento.nome} · {CATEGORIA_INSTRUMENTO_LABEL[escala.instrumento.categoria]}
                    </p>
                  </div>
                  <div className="flex items-center gap-3">
                    <Badge tone={ESCALA_STATUS_TONE[escala.status]}>{ESCALA_STATUS_LABEL[escala.status]}</Badge>
                    {(escala.status === 'PENDENTE' || escala.status === 'CONFIRMADA') && (
                      <button
                        type="button"
                        aria-label={`Remover ${escala.usuario.nome}`}
                        onClick={() => setParaRemover(escala)}
                        className="text-text-secondary transition active:scale-95"
                      >
                        <XIcon />
                      </button>
                    )}
                  </div>
                </Card>
              ))}
            </div>
          )}
        </div>

        <Button onClick={abrirModal}>Escalar músico</Button>
      </div>

      <Modal isOpen={modalAberto} onClose={() => setModalAberto(false)} title="Escalar músico">
        <div className="flex flex-col gap-4">
          <Select label="Músico" value={usuarioId} onChange={(e) => setUsuarioId(e.target.value)}>
            <option value="">Selecione</option>
            {usuarios.map((usuario) => (
              <option key={usuario.id} value={usuario.id}>
                {usuario.nome}
              </option>
            ))}
          </Select>
          <Button onClick={handleEscalar} disabled={!usuarioId || escalando}>
            {escalando ? 'Escalando...' : 'Escalar'}
          </Button>
        </div>
      </Modal>

      <ConfirmModal
        isOpen={paraRemover !== null}
        onClose={() => setParaRemover(null)}
        onConfirm={handleRemover}
        title="Remover da escala"
        message={`Remover ${paraRemover?.usuario.nome} (${paraRemover?.instrumento.nome}) deste culto?`}
        confirmLabel="Remover"
        confirming={removendo}
      />
    </div>
  )
}
