import { api } from '../config/api'
import type { EscalaMinhaResponse } from '../types/api'

export const escalaService = {
  minhas: (mes: string) =>
    api.get<EscalaMinhaResponse[]>('/escalas/minhas', { params: { mes } }).then((res) => res.data),

  confirmar: (id: number) =>
    api.post<EscalaMinhaResponse>(`/escalas/${id}/confirmar`).then((res) => res.data),

  recusar: (id: number) =>
    api.post<EscalaMinhaResponse>(`/escalas/${id}/recusar`).then((res) => res.data),
}
