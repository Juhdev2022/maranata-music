import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { PageHeader } from '../../components/layout/PageHeader'
import { Badge } from '../../components/ui/Badge'
import { Button } from '../../components/ui/Button'
import { Card } from '../../components/ui/Card'
import { EmptyState } from '../../components/ui/EmptyState'
import { Modal } from '../../components/ui/Modal'
import { Select } from '../../components/ui/Select'
import { Spinner } from '../../components/ui/Spinner'
import { useToast } from '../../hooks/useToast'
import { cultoService } from '../../services/cultoService'
import { instrumentoService } from '../../services/instrumentoService'
import { usuarioService } from '../../services/usuarioService'
import type { CultoDetalheResponse, InstrumentoResponse, UsuarioResponse } from '../../types/api'
import { CATEGORIA_INSTRUMENTO_LABEL, CULTO_TIPO_LABEL, ESCALA_STATUS_LABEL, ESCALA_STATUS_TONE } from '../../types/enums'
import { formatCultoDataHora } from '../../utils/dateFormat'

export function CultoDetalhePage() {
  const { id } = useParams()
  const cultoId = Number(id)
  const showToast = useToast()

  const [culto, setCulto] = useState<CultoDetalheResponse | null>(null)
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState(false)

  const [modalAberto, setModalAberto] = useState(false)
  const [usuarios, setUsuarios] = useState<UsuarioResponse[]>([])
  const [instrumentos, setInstrumentos] = useState<InstrumentoResponse[]>([])
  const [usuarioId, setUsuarioId] = useState('')
  const [instrumentoId, setInstrumentoId] = useState('')
  const [escalando, setEscalando] = useState(false)

  useEffect(() => {
    let cancelado = false
    setCarregando(true)
    setErro(false)
    cultoService
      .detalhar(cultoId)
      .then((data) => {
        if (!cancelado) setCulto(data)
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

  function abrirModal() {
    usuarioService.listar({ ativos: true }).then(setUsuarios)
    instrumentoService.listar().then(setInstrumentos)
    setUsuarioId('')
    setInstrumentoId('')
    setModalAberto(true)
  }

  async function handleEscalar() {
    setEscalando(true)
    try {
      await cultoService.escalar(cultoId, {
        usuarioId: Number(usuarioId),
        instrumentoId: Number(instrumentoId),
      })
      const atualizado = await cultoService.detalhar(cultoId)
      setCulto(atualizado)
      showToast('Músico escalado', 'success')
      setModalAberto(false)
    } finally {
      setEscalando(false)
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
        <Card className="flex flex-col gap-1">
          <p className="text-2xl text-text-primary">{formatCultoDataHora(culto.dataHora)}</p>
          <p className="text-sm text-text-secondary">{CULTO_TIPO_LABEL[culto.tipo]}</p>
          {culto.ministro && <p className="text-sm text-text-secondary">Ministro: {culto.ministro.nome}</p>}
          {culto.observacoes && <p className="mt-2 text-sm text-text-primary">{culto.observacoes}</p>}
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
                  <Badge tone={ESCALA_STATUS_TONE[escala.status]}>{ESCALA_STATUS_LABEL[escala.status]}</Badge>
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
          <Select label="Instrumento" value={instrumentoId} onChange={(e) => setInstrumentoId(e.target.value)}>
            <option value="">Selecione</option>
            {instrumentos.map((instrumento) => (
              <option key={instrumento.id} value={instrumento.id}>
                {instrumento.nome}
              </option>
            ))}
          </Select>
          <Button
            onClick={handleEscalar}
            disabled={!usuarioId || !instrumentoId || escalando}
          >
            {escalando ? 'Escalando...' : 'Escalar'}
          </Button>
        </div>
      </Modal>
    </div>
  )
}
