import { useEffect, useState } from 'react'
import { PageHeader } from '../../components/layout/PageHeader'
import { Button } from '../../components/ui/Button'
import { Card } from '../../components/ui/Card'
import { ConfirmModal } from '../../components/ui/ConfirmModal'
import { EmptyState } from '../../components/ui/EmptyState'
import { Modal } from '../../components/ui/Modal'
import { Select } from '../../components/ui/Select'
import { Spinner } from '../../components/ui/Spinner'
import { useToast } from '../../hooks/useToast'
import { substituicaoService } from '../../services/substituicaoService'
import { usuarioService } from '../../services/usuarioService'
import type { SolicitacaoResponse, UsuarioResponse } from '../../types/api'
import { CATEGORIA_INSTRUMENTO_LABEL, MOTIVO_SUBSTITUICAO_LABEL } from '../../types/enums'
import { formatCultoDataHora } from '../../utils/dateFormat'

export function SubstituicoesPendentesPage() {
  const [solicitacoes, setSolicitacoes] = useState<SolicitacaoResponse[] | null>(null)
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState(false)
  const showToast = useToast()

  const [paraRejeitar, setParaRejeitar] = useState<SolicitacaoResponse | null>(null)
  const [rejeitando, setRejeitando] = useState(false)

  const [paraAprovar, setParaAprovar] = useState<SolicitacaoResponse | null>(null)
  const [usuarios, setUsuarios] = useState<UsuarioResponse[]>([])
  const [substitutoFinalId, setSubstitutoFinalId] = useState('')
  const [aprovando, setAprovando] = useState(false)

  function carregar() {
    setCarregando(true)
    setErro(false)
    substituicaoService
      .pendentes()
      .then(setSolicitacoes)
      .catch(() => setErro(true))
      .finally(() => setCarregando(false))
  }

  useEffect(carregar, [])

  function abrirAprovar(solicitacao: SolicitacaoResponse) {
    if (!solicitacao.substitutoSugerido) {
      usuarioService.listar({ ativos: true }).then(setUsuarios)
      setSubstitutoFinalId('')
    }
    setParaAprovar(solicitacao)
  }

  async function handleAprovar() {
    if (!paraAprovar) return
    setAprovando(true)
    try {
      await substituicaoService.aprovar(paraAprovar.id, {
        substitutoFinalId: paraAprovar.substitutoSugerido ? null : Number(substitutoFinalId),
      })
      showToast('Substituição aprovada', 'success')
      setParaAprovar(null)
      carregar()
    } finally {
      setAprovando(false)
    }
  }

  async function handleRejeitar() {
    if (!paraRejeitar) return
    setRejeitando(true)
    try {
      await substituicaoService.rejeitar(paraRejeitar.id)
      showToast('Solicitação rejeitada', 'success')
      setParaRejeitar(null)
      carregar()
    } finally {
      setRejeitando(false)
    }
  }

  return (
    <div>
      <PageHeader title="Substituições" onBack />

      <div className="flex flex-col gap-3 p-4">
        {carregando && (
          <div className="flex justify-center py-12">
            <Spinner />
          </div>
        )}

        {!carregando && erro && (
          <EmptyState title="Não deu pra carregar as solicitações" description="Verifique sua conexão e tente de novo." />
        )}

        {!carregando && !erro && solicitacoes?.length === 0 && <EmptyState title="Nenhuma solicitação pendente" />}

        {!carregando &&
          !erro &&
          solicitacoes?.map((solicitacao) => (
            <Card key={solicitacao.id} className="flex flex-col gap-2">
              <p className="text-text-primary">{solicitacao.solicitante.nome}</p>
              <p className="text-sm text-text-secondary">
                {formatCultoDataHora(solicitacao.culto.dataHora)} · {solicitacao.instrumento.nome} ·{' '}
                {CATEGORIA_INSTRUMENTO_LABEL[solicitacao.instrumento.categoria]}
              </p>
              <p className="text-sm text-text-secondary">Motivo: {MOTIVO_SUBSTITUICAO_LABEL[solicitacao.motivo]}</p>
              {solicitacao.observacao && <p className="text-sm text-text-secondary">"{solicitacao.observacao}"</p>}
              <p className="text-sm text-text-secondary">
                Substituto sugerido: {solicitacao.substitutoSugerido?.nome ?? 'Nenhum (aberta)'}
              </p>
              <div className="mt-2 flex gap-2">
                <Button size="sm" onClick={() => abrirAprovar(solicitacao)} className="flex-1">
                  Aprovar
                </Button>
                <Button size="sm" variant="secondary" onClick={() => setParaRejeitar(solicitacao)} className="flex-1">
                  Rejeitar
                </Button>
              </div>
            </Card>
          ))}
      </div>

      <ConfirmModal
        isOpen={paraAprovar !== null && !!paraAprovar?.substitutoSugerido}
        onClose={() => setParaAprovar(null)}
        onConfirm={handleAprovar}
        title="Aprovar substituição"
        message={`Aprovar substituição de ${paraAprovar?.solicitante.nome} por ${paraAprovar?.substitutoSugerido?.nome}?`}
        confirmLabel="Aprovar"
        confirming={aprovando}
      />

      <Modal
        isOpen={paraAprovar !== null && !paraAprovar?.substitutoSugerido}
        onClose={() => setParaAprovar(null)}
        title="Escolher substituto"
      >
        <div className="flex flex-col gap-4">
          <Select label="Substituto" value={substitutoFinalId} onChange={(e) => setSubstitutoFinalId(e.target.value)}>
            <option value="">Selecione</option>
            {usuarios.map((usuario) => (
              <option key={usuario.id} value={usuario.id}>
                {usuario.nome}
              </option>
            ))}
          </Select>
          <Button onClick={handleAprovar} disabled={!substitutoFinalId || aprovando}>
            {aprovando ? 'Aprovando...' : 'Aprovar'}
          </Button>
        </div>
      </Modal>

      <ConfirmModal
        isOpen={paraRejeitar !== null}
        onClose={() => setParaRejeitar(null)}
        onConfirm={handleRejeitar}
        title="Rejeitar solicitação"
        message={`Rejeitar a solicitação de substituição de ${paraRejeitar?.solicitante.nome}?`}
        confirmLabel="Rejeitar"
        confirming={rejeitando}
      />
    </div>
  )
}
