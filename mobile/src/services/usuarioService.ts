import { api } from '../config/api'
import type { UsuarioResponse } from '../types/api'
import type { Papel } from '../types/enums'

export const usuarioService = {
  listar: (params?: { ativos?: boolean; papel?: Papel }) =>
    api.get<UsuarioResponse[]>('/usuarios', { params }).then((res) => res.data),
}
