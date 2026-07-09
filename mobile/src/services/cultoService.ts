import { api } from '../config/api'
import type {
  AtualizarObservacoesRequest,
  CriarCultoRequest,
  CultoDetalheResponse,
  CultoResponse,
  EscalaResumo,
  EscalarMusicoRequest,
} from '../types/api'

export const cultoService = {
  listarPorMes: (mes: string) => api.get<CultoResponse[]>('/cultos', { params: { mes } }).then((res) => res.data),

  criar: (payload: CriarCultoRequest) => api.post<CultoResponse>('/cultos', payload).then((res) => res.data),

  detalhar: (id: number) => api.get<CultoDetalheResponse>(`/cultos/${id}`).then((res) => res.data),

  escalar: (cultoId: number, payload: EscalarMusicoRequest) =>
    api.post<EscalaResumo>(`/cultos/${cultoId}/escalas`, payload).then((res) => res.data),

  atualizarObservacoes: (id: number, payload: AtualizarObservacoesRequest) =>
    api.patch<CultoResponse>(`/cultos/${id}/observacoes`, payload).then((res) => res.data),

  removerEscala: (cultoId: number, escalaId: number) =>
    api.delete<void>(`/cultos/${cultoId}/escalas/${escalaId}`).then((res) => res.data),
}
