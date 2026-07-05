import { api } from '../config/api'
import type { InstrumentoResponse } from '../types/api'

export const instrumentoService = {
  listar: () => api.get<InstrumentoResponse[]>('/instrumentos').then((res) => res.data),
}
