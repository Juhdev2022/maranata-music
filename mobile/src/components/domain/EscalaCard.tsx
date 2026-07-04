import { useState } from 'react'
import { Badge } from '../ui/Badge'
import { Button } from '../ui/Button'
import { Card } from '../ui/Card'
import type { EscalaMinhaResponse } from '../../types/api'
import { CATEGORIA_INSTRUMENTO_LABEL, ESCALA_STATUS_LABEL, ESCALA_STATUS_TONE } from '../../types/enums'
import { formatCultoDataHora } from '../../utils/dateFormat'

interface EscalaCardProps {
  escala: EscalaMinhaResponse
  onConfirmar: (id: number) => Promise<void>
  onRecusar: (id: number) => Promise<void>
}

export function EscalaCard({ escala, onConfirmar, onRecusar }: EscalaCardProps) {
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function handleConfirmar() {
    setIsSubmitting(true)
    try {
      await onConfirmar(escala.id)
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleRecusar() {
    setIsSubmitting(true)
    try {
      await onRecusar(escala.id)
    } finally {
      setIsSubmitting(false)
    }
  }

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

      {escala.status === 'PENDENTE' && (
        <div className="flex gap-2">
          <Button size="sm" disabled={isSubmitting} onClick={handleConfirmar} className="flex-1">
            Confirmar
          </Button>
          <Button size="sm" variant="secondary" disabled={isSubmitting} onClick={handleRecusar} className="flex-1">
            Recusar
          </Button>
        </div>
      )}

      {escala.status === 'CONFIRMADA' && (
        <Button size="sm" variant="ghost" disabled={isSubmitting} onClick={handleRecusar} className="self-start">
          Não vou poder
        </Button>
      )}
    </Card>
  )
}
