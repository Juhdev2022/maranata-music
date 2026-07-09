import { useEffect, useState } from 'react'
import { Button } from '../ui/Button'
import { Modal } from '../ui/Modal'
import type { MusicaCultoResponse } from '../../types/api'
import type { RevisaoLider } from '../../types/enums'

interface RevisarMusicaModalProps {
  musica: MusicaCultoResponse | null
  onClose: () => void
  onRevisar: (revisaoLider: RevisaoLider, observacaoLiderPrivada?: string) => Promise<void>
}

export function RevisarMusicaModal({ musica, onClose, onRevisar }: RevisarMusicaModalProps) {
  const [observacao, setObservacao] = useState('')
  const [salvando, setSalvando] = useState(false)

  useEffect(() => {
    setObservacao(musica?.observacaoLiderPrivada ?? '')
  }, [musica])

  async function handleAprovar() {
    setSalvando(true)
    try {
      await onRevisar('APROVADA')
    } finally {
      setSalvando(false)
    }
  }

  async function handleComObservacao() {
    setSalvando(true)
    try {
      await onRevisar('COM_OBSERVACAO', observacao)
    } finally {
      setSalvando(false)
    }
  }

  return (
    <Modal isOpen={musica !== null} onClose={onClose} title={`Revisar "${musica?.titulo ?? ''}"`}>
      <div className="flex flex-col gap-4">
        <Button onClick={handleAprovar} disabled={salvando}>
          Aprovar
        </Button>

        <div className="flex flex-col gap-1.5">
          <label htmlFor="observacao-lider" className="text-sm text-text-secondary">
            Observação privada (só o ministro vê)
          </label>
          <textarea
            id="observacao-lider"
            rows={3}
            value={observacao}
            onChange={(e) => setObservacao(e.target.value)}
            placeholder="Ex.: trocar o tom, revisar a introdução..."
            className="rounded-btn border border-border bg-surface px-3.5 py-2.5 text-text-primary outline-none transition focus:ring-2 focus:ring-accent"
          />
          <Button
            variant="secondary"
            onClick={handleComObservacao}
            disabled={!observacao.trim() || salvando}
            className="self-end"
          >
            Salvar observação
          </Button>
        </div>
      </div>
    </Modal>
  )
}
