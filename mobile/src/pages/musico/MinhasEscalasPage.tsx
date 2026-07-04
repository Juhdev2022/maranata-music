import { useEffect, useState } from 'react'
import { PageHeader } from '../../components/layout/PageHeader'
import { EscalaCard } from '../../components/domain/EscalaCard'
import { EmptyState } from '../../components/ui/EmptyState'
import { Spinner } from '../../components/ui/Spinner'
import { useToast } from '../../hooks/useToast'
import { escalaService } from '../../services/escalaService'
import type { EscalaMinhaResponse } from '../../types/api'
import { formatMesLabel, mesAdjacente, mesAtual } from '../../utils/dateFormat'

function ChevronIcon({ direction }: { direction: 'left' | 'right' }) {
  const d = direction === 'left' ? 'M15 18l-6-6 6-6' : 'M9 18l6-6-6-6'
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d={d} />
    </svg>
  )
}

export function MinhasEscalasPage() {
  const [mes, setMes] = useState(mesAtual())
  const [escalas, setEscalas] = useState<EscalaMinhaResponse[] | null>(null)
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState(false)
  const showToast = useToast()

  useEffect(() => {
    let cancelado = false
    setCarregando(true)
    setErro(false)
    escalaService
      .minhas(mes)
      .then((data) => {
        if (!cancelado) setEscalas(data)
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

  async function handleConfirmar(id: number) {
    const atualizada = await escalaService.confirmar(id)
    setEscalas((atual) => atual?.map((e) => (e.id === id ? atualizada : e)) ?? atual)
    showToast('Presença confirmada', 'success')
  }

  async function handleRecusar(id: number) {
    const atualizada = await escalaService.recusar(id)
    setEscalas((atual) => atual?.map((e) => (e.id === id ? atualizada : e)) ?? atual)
    showToast('Presença recusada', 'success')
  }

  return (
    <div>
      <PageHeader title="Minhas escalas" />

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
          <EmptyState title="Não deu pra carregar as escalas" description="Verifique sua conexão e tente de novo." />
        )}

        {!carregando && !erro && escalas?.length === 0 && (
          <EmptyState title="Nenhuma escala neste mês" />
        )}

        {!carregando &&
          !erro &&
          escalas?.map((escala) => (
            <EscalaCard key={escala.id} escala={escala} onConfirmar={handleConfirmar} onRecusar={handleRecusar} />
          ))}
      </div>
    </div>
  )
}
