import { api } from '../config/api'
import type {
  AprovarSubstituicaoRequest,
  SolicitacaoResponse,
  SolicitarSubstituicaoRequest,
  SubstitutoElegivelResponse,
} from '../types/api'

export const substituicaoService = {
  solicitar: (payload: SolicitarSubstituicaoRequest) =>
    api.post<SolicitacaoResponse>('/substituicoes', payload).then((res) => res.data),

  pendentes: () => api.get<SolicitacaoResponse[]>('/substituicoes/pendentes').then((res) => res.data),

  aprovar: (id: number, payload: AprovarSubstituicaoRequest) =>
    api.post<SolicitacaoResponse>(`/substituicoes/${id}/aprovar`, payload).then((res) => res.data),

  rejeitar: (id: number) => api.post<SolicitacaoResponse>(`/substituicoes/${id}/rejeitar`).then((res) => res.data),

  substitutosElegiveis: (escalaId: number) =>
    api
      .get<SubstitutoElegivelResponse[]>('/substituicoes/substitutos-elegiveis', { params: { escalaId } })
      .then((res) => res.data),
}
