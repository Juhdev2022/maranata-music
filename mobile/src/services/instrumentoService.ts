import { api } from '../config/api'
import type { CriarInstrumentoRequest, InstrumentoResponse } from '../types/api'

export const instrumentoService = {
  listar: () => api.get<InstrumentoResponse[]>('/instrumentos').then((res) => res.data),

  criar: (payload: CriarInstrumentoRequest) =>
    api.post<InstrumentoResponse>('/instrumentos', payload).then((res) => res.data),

  remover: (id: number) => api.delete<void>(`/instrumentos/${id}`).then((res) => res.data),
}
