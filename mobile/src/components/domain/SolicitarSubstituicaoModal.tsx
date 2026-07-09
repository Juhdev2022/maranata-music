import { useEffect, useState } from 'react'
import { Button } from '../ui/Button'
import { Modal } from '../ui/Modal'
import { Select } from '../ui/Select'
import { substituicaoService } from '../../services/substituicaoService'
import type { EscalaMinhaResponse, SubstitutoElegivelResponse } from '../../types/api'
import type { MotivoSubstituicao } from '../../types/enums'
import { MOTIVO_SUBSTITUICAO_LABEL } from '../../types/enums'

const MOTIVOS: MotivoSubstituicao[] = ['VIAGEM', 'SAUDE', 'TRABALHO', 'OUTRO']

interface SolicitarSubstituicaoModalProps {
  isOpen: boolean
  onClose: () => void
  escala: EscalaMinhaResponse
  onSolicitado: () => void
}

export function SolicitarSubstituicaoModal({ isOpen, onClose, escala, onSolicitado }: SolicitarSubstituicaoModalProps) {
  const [motivo, setMotivo] = useState<MotivoSubstituicao>('VIAGEM')
  const [observacao, setObservacao] = useState('')
  const [substitutoSugeridoId, setSubstitutoSugeridoId] = useState('')
  const [substitutos, setSubstitutos] = useState<SubstitutoElegivelResponse[]>([])
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    if (!isOpen) return
    setMotivo('VIAGEM')
    setObservacao('')
    setSubstitutoSugeridoId('')
    substituicaoService.substitutosElegiveis(escala.id).then(setSubstitutos)
  }, [isOpen, escala.id])

  async function handleSolicitar() {
    if (!substitutoSugeridoId) return
    setEnviando(true)
    try {
      await substituicaoService.solicitar({
        escalaId: escala.id,
        motivo,
        observacao: observacao || undefined,
        substitutoSugeridoId: Number(substitutoSugeridoId),
      })
      onSolicitado()
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Solicitar substituição">
      <div className="flex flex-col gap-4">
        <Select label="Motivo" value={motivo} onChange={(e) => setMotivo(e.target.value as MotivoSubstituicao)}>
          {MOTIVOS.map((valor) => (
            <option key={valor} value={valor}>
              {MOTIVO_SUBSTITUICAO_LABEL[valor]}
            </option>
          ))}
        </Select>

        <div className="flex flex-col gap-1.5">
          <label htmlFor="observacao-substituicao" className="text-sm text-text-secondary">
            Observação (opcional)
          </label>
          <textarea
            id="observacao-substituicao"
            rows={2}
            value={observacao}
            onChange={(e) => setObservacao(e.target.value)}
            className="rounded-btn border border-border bg-surface px-3.5 py-2.5 text-text-primary outline-none transition focus:ring-2 focus:ring-accent"
          />
        </div>

        <Select
          label="Quem vai tocar no seu lugar?"
          value={substitutoSugeridoId}
          onChange={(e) => setSubstitutoSugeridoId(e.target.value)}
        >
          <option value="">Selecione</option>
          {substitutos.map((substituto) => (
            <option key={substituto.id} value={substituto.id}>
              {substituto.nome}
            </option>
          ))}
        </Select>

        <Button onClick={handleSolicitar} disabled={!substitutoSugeridoId || enviando}>
          {enviando ? 'Enviando...' : 'Solicitar substituição'}
        </Button>
      </div>
    </Modal>
  )
}
