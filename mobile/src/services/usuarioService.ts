import { api } from '../config/api'
import type { CriarUsuarioRequest, UsuarioResponse } from '../types/api'
import type { Papel } from '../types/enums'

export const usuarioService = {
  listar: (params?: { ativos?: boolean; papel?: Papel }) =>
    api.get<UsuarioResponse[]>('/usuarios', { params }).then((res) => res.data),

  criar: (dados: CriarUsuarioRequest) => api.post<UsuarioResponse>('/usuarios', dados).then((res) => res.data),

  adicionarPapel: (usuarioId: number, papel: Papel) =>
    api.post<UsuarioResponse>(`/usuarios/${usuarioId}/papeis`, { papel }).then((res) => res.data),

  removerPapel: (usuarioId: number, papel: Papel) =>
    api.delete<void>(`/usuarios/${usuarioId}/papeis/${papel}`).then((res) => res.data),

  desativar: (usuarioId: number) => api.delete<void>(`/usuarios/${usuarioId}`).then((res) => res.data),

  reativar: (usuarioId: number) => api.post<UsuarioResponse>(`/usuarios/${usuarioId}/reativar`).then((res) => res.data),
}
