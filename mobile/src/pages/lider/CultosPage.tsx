import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { PageHeader } from '../../components/layout/PageHeader'
import { CultoCard } from '../../components/domain/CultoCard'
import { EmptyState } from '../../components/ui/EmptyState'
import { Spinner } from '../../components/ui/Spinner'
import { cultoService } from '../../services/cultoService'
import type { CultoResponse } from '../../types/api'
import { formatMesLabel, mesAdjacente, mesAtual } from '../../utils/dateFormat'

function ChevronIcon({ direction }: { direction: 'left' | 'right' }) {
  const d = direction === 'left' ? 'M15 18l-6-6 6-6' : 'M9 18l6-6-6-6'
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d={d} />
    </svg>
  )
}

function PlusIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M12 5v14M5 12h14" />
    </svg>
  )
}

export function CultosPage() {
  const [mes, setMes] = useState(mesAtual())
  const [cultos, setCultos] = useState<CultoResponse[] | null>(null)
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState(false)
  const navigate = useNavigate()

  useEffect(() => {
    let cancelado = false
    setCarregando(true)
    setErro(false)
    cultoService
      .listarPorMes(mes)
      .then((data) => {
        if (!cancelado) setCultos(data)
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
  }, [mes])

  return (
    <div className="relative min-h-screen">
      <PageHeader title="Cultos" />

      <div className="flex items-center justify-between px-4 py-3">
        <button
          type="button"
          aria-label="Mês anterior"
          className="text-text-secondary transition active:scale-95"
          onClick={() => setMes((atual) => mesAdjacente(atual, -1))}
        >
          <ChevronIcon direction="left" />
        </button>
        <p className="text-text-primary">{formatMesLabel(mes)}</p>
        <button
          type="button"
          aria-label="Próximo mês"
          className="text-text-secondary transition active:scale-95"
          onClick={() => setMes((atual) => mesAdjacente(atual, 1))}
        >
          <ChevronIcon direction="right" />
        </button>
      </div>

      <div className="flex flex-col gap-3 px-4 pb-6">
        {carregando && (
          <div className="flex justify-center py-12">
            <Spinner />
          </div>
        )}

        {!carregando && erro && (
          <EmptyState title="Não deu pra carregar os cultos" description="Verifique sua conexão e tente de novo." />
        )}

        {!carregando && !erro && cultos?.length === 0 && <EmptyState title="Nenhum culto neste mês" />}

        {!carregando &&
          !erro &&
          cultos?.map((culto) => (
            <CultoCard key={culto.id} culto={culto} onClick={() => navigate(`/cultos/${culto.id}`)} />
          ))}
      </div>

      <div className="fixed inset-x-0 bottom-24 z-50 mx-auto flex max-w-md justify-end pr-4">
        <button
          type="button"
          aria-label="Criar culto"
          onClick={() => navigate('/cultos/novo')}
          className="flex h-14 w-14 items-center justify-center rounded-full bg-accent text-text-primary shadow-lg transition active:scale-95"
        >
          <PlusIcon />
        </button>
      </div>
    </div>
  )
}
