import { isAxiosError } from 'axios'
import type { ErroResponse } from '../types/api'

export function extrairMensagemErro(error: unknown, fallback = 'Erro inesperado'): string {
  if (isAxiosError<ErroResponse>(error)) {
    const erro = error.response?.data?.erro
    if (erro) return erro
  }
  return fallback
}
