import { useState } from 'react'
import { Badge } from '../ui/Badge'
import { Button } from '../ui/Button'
import { Card } from '../ui/Card'
import { SolicitarSubstituicaoModal } from './SolicitarSubstituicaoModal'
import type { EscalaMinhaResponse } from '../../types/api'
import { CATEGORIA_INSTRUMENTO_LABEL, ESCALA_STATUS_LABEL, ESCALA_STATUS_TONE } from '../../types/enums'
import { formatCultoDataHora } from '../../utils/dateFormat'

interface EscalaCardProps {
  escala: EscalaMinhaResponse
  onConfirmar: (id: number) => Promise<void>
  onSubstituicaoSolicitada: () => void
}

export function EscalaCard({ escala, onConfirmar, onSubstituicaoSolicitada }: EscalaCardProps) {
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [modalAberto, setModalAberto] = useState(false)

  async function handleConfirmar() {
    setIsSubmitting(true)
    try {
      await onConfirmar(escala.id)
    } finally {
      setIsSubmitting(false)
    }
  }

  function handleSolicitado() {
    setModalAberto(false)
    onSubstituicaoSolicitada()
  }

  const podeAgir = escala.status === 'PENDENTE' || escala.status === 'CONFIRMADA'

  return (
    <Card className="flex flex-col gap-3">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-text-primary">{formatCultoDataHora(escala.culto.dataHora)}</p>
          <p className="text-sm text-text-secondary">
            {escala.instrumento.nome} · {CATEGORIA_INSTRUMENTO_LABEL[escala.instrumento.categoria]}
          </p>
        </div>
        <Badge tone={ESCALA_STATUS_TONE[escala.status]}>{ESCALA_STATUS_LABEL[escala.status]}</Badge>
      </div>

      {escala.solicitacaoAberta ? (
        <Badge tone="gold">Aguardando líder</Badge>
      ) : (
        podeAgir && (
          <div className="flex gap-2">
            {escala.status === 'PENDENTE' && (
              <Button size="sm" disabled={isSubmitting} onClick={handleConfirmar} className="flex-1">
                Confirmar
              </Button>
            )}
            <Button
              size="sm"
              variant="secondary"
              disabled={isSubmitting}
              onClick={() => setModalAberto(true)}
              className="flex-1"
            >
              Solicitar substituição
            </Button>
          </div>
        )
      )}

      <SolicitarSubstituicaoModal
        isOpen={modalAberto}
        onClose={() => setModalAberto(false)}
        escala={escala}
        onSolicitado={handleSolicitado}
      />
    </Card>
  )
}
