import { api } from '../config/api'
import type {
  AtualizarMusicaCultoRequest,
  CriarMusicaCultoRequest,
  MusicaCultoResponse,
  RevisaoLiderRequest,
} from '../types/api'

export const repertorioService = {
  listar: (cultoId: number) =>
    api.get<MusicaCultoResponse[]>(`/cultos/${cultoId}/repertorio`).then((res) => res.data),

  adicionar: (cultoId: number, payload: CriarMusicaCultoRequest) =>
    api.post<MusicaCultoResponse>(`/cultos/${cultoId}/repertorio`, payload).then((res) => res.data),

  atualizar: (cultoId: number, musicaCultoId: number, payload: AtualizarMusicaCultoRequest) =>
    api.patch<MusicaCultoResponse>(`/cultos/${cultoId}/repertorio/${musicaCultoId}`, payload).then((res) => res.data),

  remover: (cultoId: number, musicaCultoId: number) =>
    api.delete<void>(`/cultos/${cultoId}/repertorio/${musicaCultoId}`).then((res) => res.data),

  revisar: (cultoId: number, musicaCultoId: number, payload: RevisaoLiderRequest) =>
    api
      .patch<MusicaCultoResponse>(`/cultos/${cultoId}/repertorio/${musicaCultoId}/revisao-lider`, payload)
      .then((res) => res.data),
}
