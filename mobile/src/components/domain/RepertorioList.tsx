import { Badge } from '../ui/Badge'
import { Card } from '../ui/Card'
import { EmptyState } from '../ui/EmptyState'
import type { MusicaCultoResponse } from '../../types/api'

function XIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M6 6l12 12M18 6L6 18" />
    </svg>
  )
}

interface RepertorioListProps {
  musicas: MusicaCultoResponse[]
  podeEditar: boolean
  isLider: boolean
  onRemover: (musicaCultoId: number) => void
  onRevisar: (musica: MusicaCultoResponse) => void
}

export function RepertorioList({ musicas, podeEditar, isLider, onRemover, onRevisar }: RepertorioListProps) {
  if (musicas.length === 0) {
    return <EmptyState title="Nenhuma música adicionada ainda" />
  }

  return (
    <div className="flex flex-col gap-2">
      {musicas.map((musica) => (
        <Card key={musica.id} className="flex flex-col gap-2">
          <div className="flex items-start justify-between gap-3">
            <p className="text-text-primary">
              {musica.ordem}. {musica.titulo}
            </p>
            {podeEditar && (
              <button
                type="button"
                aria-label={`Remover ${musica.titulo}`}
                onClick={() => onRemover(musica.id)}
                className="text-text-secondary transition active:scale-95"
              >
                <XIcon />
              </button>
            )}
          </div>

          {musica.linkVideo && (
            <a href={musica.linkVideo} target="_blank" rel="noreferrer" className="text-sm text-accent-gold underline">
              Ver vídeo
            </a>
          )}

          <div className="flex flex-wrap items-center gap-2">
            {musica.revisaoLider === 'APROVADA' && <Badge tone="green">Aprovada pelo líder</Badge>}
            {musica.revisaoLider === 'COM_OBSERVACAO' && <Badge tone="amber">Observação do líder</Badge>}
            {isLider && (
              <button type="button" onClick={() => onRevisar(musica)} className="text-sm text-accent-gold underline">
                Revisar
              </button>
            )}
          </div>

          {musica.observacaoLiderPrivada && (
            <p className="text-sm text-warning">Obs. do líder: {musica.observacaoLiderPrivada}</p>
          )}
        </Card>
      ))}
    </div>
  )
}
