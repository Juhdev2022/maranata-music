import { api } from '../config/api'
import type { MusicoInstrumentoResponse } from '../types/api'

export const musicoInstrumentoService = {
  listarPorUsuario: (usuarioId: number) =>
    api.get<MusicoInstrumentoResponse[]>(`/musicos/${usuarioId}/instrumentos`).then((res) => res.data),

  vincular: (usuarioId: number, instrumentoId: number, principal: boolean) =>
    api
      .post<MusicoInstrumentoResponse>(`/musicos/${usuarioId}/instrumentos/${instrumentoId}`, { principal })
      .then((res) => res.data),

  desvincular: (usuarioId: number, instrumentoId: number) =>
    api.delete<void>(`/musicos/${usuarioId}/instrumentos/${instrumentoId}`).then((res) => res.data),
}
