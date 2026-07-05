import { Badge } from '../ui/Badge'
import { Card } from '../ui/Card'
import type { CultoResponse } from '../../types/api'
import { CULTO_TIPO_LABEL } from '../../types/enums'
import { formatCultoDataHora } from '../../utils/dateFormat'

interface CultoCardProps {
  culto: CultoResponse
  onClick?: () => void
}

function chipDoTotal(total: number): { tone: 'gray' | 'amber' | 'green'; label: string } {
  if (total === 0) return { tone: 'gray', label: 'Sem escalas' }
  if (total <= 2) return { tone: 'amber', label: 'Falta gente' }
  return { tone: 'green', label: 'Equipe montada' }
}

export function CultoCard({ culto, onClick }: CultoCardProps) {
  const chip = chipDoTotal(culto.totalEscalados)

  return (
    <Card onClick={onClick} className="flex cursor-pointer items-center justify-between gap-3 transition active:scale-95">
      <div>
        <p className="text-text-primary">{formatCultoDataHora(culto.dataHora)}</p>
        <p className="text-sm text-text-secondary">
          {CULTO_TIPO_LABEL[culto.tipo]}
          {culto.ministro && ` · ${culto.ministro.nome}`}
        </p>
      </div>
      <Badge tone={chip.tone}>{chip.label}</Badge>
    </Card>
  )
}
