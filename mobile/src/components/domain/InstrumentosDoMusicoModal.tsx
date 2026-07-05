import { useEffect, useState } from 'react'
import { Badge } from '../ui/Badge'
import { Button } from '../ui/Button'
import { ConfirmModal } from '../ui/ConfirmModal'
import { Modal } from '../ui/Modal'
import { Select } from '../ui/Select'
import { Spinner } from '../ui/Spinner'
import { useToast } from '../../hooks/useToast'
import { instrumentoService } from '../../services/instrumentoService'
import { musicoInstrumentoService } from '../../services/musicoInstrumentoService'
import type { InstrumentoResponse, MusicoInstrumentoResponse, UsuarioResponse } from '../../types/api'

interface InstrumentosDoMusicoModalProps {
  usuario: UsuarioResponse
  isOpen: boolean
  onClose: () => void
}

export function InstrumentosDoMusicoModal({ usuario, isOpen, onClose }: InstrumentosDoMusicoModalProps) {
  const showToast = useToast()

  const [vinculos, setVinculos] = useState<MusicoInstrumentoResponse[] | null>(null)
  const [todosInstrumentos, setTodosInstrumentos] = useState<InstrumentoResponse[]>([])
  const [carregando, setCarregando] = useState(true)

  const [instrumentoId, setInstrumentoId] = useState('')
  const [marcarPrincipal, setMarcarPrincipal] = useState(false)
  const [vinculando, setVinculando] = useState(false)

  const [paraDesvincular, setParaDesvincular] = useState<MusicoInstrumentoResponse | null>(null)
  const [desvinculando, setDesvinculando] = useState(false)

  function carregar() {
    setCarregando(true)
    Promise.all([musicoInstrumentoService.listarPorUsuario(usuario.id), instrumentoService.listar()])
      .then(([vinculosData, instrumentosData]) => {
        setVinculos(vinculosData)
        setTodosInstrumentos(instrumentosData)
      })
      .finally(() => setCarregando(false))
  }

  useEffect(() => {
    if (isOpen) {
      setInstrumentoId('')
      setMarcarPrincipal(false)
      carregar()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOpen, usuario.id])

  async function handleVincular() {
    setVinculando(true)
    try {
      await musicoInstrumentoService.vincular(usuario.id, Number(instrumentoId), marcarPrincipal)
      showToast('Instrumento vinculado', 'success')
      setInstrumentoId('')
      setMarcarPrincipal(false)
      carregar()
    } finally {
      setVinculando(false)
    }
  }

  async function handleDesvincular() {
    if (!paraDesvincular) return
    setDesvinculando(true)
    try {
      await musicoInstrumentoService.desvincular(usuario.id, paraDesvincular.instrumentoId)
      showToast('Instrumento desvinculado', 'success')
      setParaDesvincular(null)
      carregar()
    } finally {
      setDesvinculando(false)
    }
  }

  const instrumentosDisponiveis = todosInstrumentos.filter(
    (instrumento) => !vinculos?.some((v) => v.instrumentoId === instrumento.id),
  )

  return (
    <>
      <Modal isOpen={isOpen} onClose={onClose} title={`Instrumentos de ${usuario.nome}`}>
        {carregando ? (
          <div className="flex justify-center py-8">
            <Spinner />
          </div>
        ) : (
          <div className="flex flex-col gap-4">
            {vinculos && vinculos.length > 0 ? (
              <div className="flex flex-col gap-2">
                {vinculos.map((vinculo) => (
                  <div
                    key={vinculo.instrumentoId}
                    className="flex items-center justify-between gap-2 rounded-btn border border-border bg-surface px-3 py-2"
                  >
                    <div className="flex items-center gap-2">
                      <span className="text-text-primary">{vinculo.instrumentoNome}</span>
                      {vinculo.principal && <Badge tone="gold">Principal</Badge>}
                    </div>
                    <button
                      type="button"
                      aria-label={`Desvincular ${vinculo.instrumentoNome}`}
                      onClick={() => setParaDesvincular(vinculo)}
                      className="text-text-secondary transition active:scale-95"
                    >
                      ✕
                    </button>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-sm text-text-secondary">Nenhum instrumento vinculado ainda.</p>
            )}

            <div className="border-t border-border pt-4">
              <p className="mb-3 text-sm font-medium text-text-primary">Vincular novo instrumento</p>
              <div className="flex flex-col gap-3">
                <Select
                  label="Instrumento"
                  value={instrumentoId}
                  onChange={(e) => setInstrumentoId(e.target.value)}
                  disabled={instrumentosDisponiveis.length === 0}
                >
                  <option value="">Selecione</option>
                  {instrumentosDisponiveis.map((instrumento) => (
                    <option key={instrumento.id} value={instrumento.id}>
                      {instrumento.nome}
                    </option>
                  ))}
                </Select>
                <label className="flex items-center gap-2 text-sm text-text-secondary">
                  <input
                    type="checkbox"
                    checked={marcarPrincipal}
                    onChange={(e) => setMarcarPrincipal(e.target.checked)}
                    className="accent-accent"
                  />
                  Marcar como principal
                </label>
                <Button onClick={handleVincular} disabled={!instrumentoId || vinculando}>
                  {vinculando ? 'Vinculando...' : 'Vincular'}
                </Button>
              </div>
            </div>
          </div>
        )}
      </Modal>

      <ConfirmModal
        isOpen={paraDesvincular !== null}
        onClose={() => setParaDesvincular(null)}
        onConfirm={handleDesvincular}
        title="Desvincular instrumento"
        message={`Desvincular ${paraDesvincular?.instrumentoNome} de ${usuario.nome}?`}
        confirmLabel="Desvincular"
        confirming={desvinculando}
      />
    </>
  )
}
