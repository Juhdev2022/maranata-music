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

  const [paraAprovar, setParaAprovar] = useState<SolicitacaoResponse | null>(null)
  const [aprovando, setAprovando] = useState(false)

  const [paraEscolherOutro, setParaEscolherOutro] = useState<SolicitacaoResponse | null>(null)
  const [usuarios, setUsuarios] = useState<UsuarioResponse[]>([])
  const [substitutoFinalId, setSubstitutoFinalId] = useState('')
  const [escolhendo, setEscolhendo] = useState(false)

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

  async function handleAprovar() {
    if (!paraAprovar) return
    setAprovando(true)
    try {
      await substituicaoService.aprovar(paraAprovar.id)
      showToast('Substituição aprovada', 'success')
      setParaAprovar(null)
      carregar()
    } finally {
      setAprovando(false)
    }
  }

  function abrirEscolherOutro(solicitacao: SolicitacaoResponse) {
    usuarioService.listar({ ativos: true }).then((lista) =>
      setUsuarios(lista.filter((usuario) => usuario.id !== solicitacao.solicitante.id)),
    )
    setSubstitutoFinalId('')
    setParaEscolherOutro(solicitacao)
  }

  async function handleEscolherOutro() {
    if (!paraEscolherOutro || !substitutoFinalId) return
    setEscolhendo(true)
    try {
      await substituicaoService.rejeitar(paraEscolherOutro.id, { substitutoFinalId: Number(substitutoFinalId) })
      showToast('Substituição trocada', 'success')
      setParaEscolherOutro(null)
      carregar()
    } finally {
      setEscolhendo(false)
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
                Indicação do músico: {solicitacao.substitutoSugerido?.nome}
              </p>
              <div className="mt-2 flex gap-2">
                <Button size="sm" onClick={() => setParaAprovar(solicitacao)} className="flex-1">
                  Aprovar indicação
                </Button>
                <Button size="sm" variant="secondary" onClick={() => abrirEscolherOutro(solicitacao)} className="flex-1">
                  Escolher outro
                </Button>
              </div>
            </Card>
          ))}
      </div>

      <ConfirmModal
        isOpen={paraAprovar !== null}
        onClose={() => setParaAprovar(null)}
        onConfirm={handleAprovar}
        title="Aprovar substituição"
        message={`Aprovar troca de ${paraAprovar?.solicitante.nome} por ${paraAprovar?.substitutoSugerido?.nome}?`}
        confirmLabel="Aprovar"
        confirming={aprovando}
      />

      <Modal
        isOpen={paraEscolherOutro !== null}
        onClose={() => setParaEscolherOutro(null)}
        title="Escolher outro substituto"
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
          <Button onClick={handleEscolherOutro} disabled={!substitutoFinalId || escolhendo}>
            {escolhendo ? 'Trocando...' : 'Confirmar troca'}
          </Button>
        </div>
      </Modal>
    </div>
  )
}
