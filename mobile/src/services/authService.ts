import { api } from '../config/api'
import type { AuthResponse, DefinirSenhaRequest, LoginRequest, RegistroRequest } from '../types/api'

export const authService = {
  login: (payload: LoginRequest) => api.post<AuthResponse>('/auth/login', payload).then((res) => res.data),

  registro: (payload: RegistroRequest) =>
    api.post<AuthResponse>('/auth/registro', payload).then((res) => res.data),

  definirSenha: (payload: DefinirSenhaRequest) =>
    api.post<AuthResponse>('/auth/definir-senha', payload).then((res) => res.data),
}
