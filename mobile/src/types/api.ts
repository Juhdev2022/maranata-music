import type { CategoriaInstrumento, CultoTipo, EscalaStatus, Papel } from './enums'

export interface AuthResponse {
  token: string
  nome: string
  email: string
  papeis: Papel[]
}

export interface LoginRequest {
  email: string
  senha: string
}

export interface RegistroRequest {
  nome: string
  email: string
  senha: string
  telefone?: string
}

export interface ErroResponse {
  erro: string
  campos?: Record<string, string>
}

export interface MinistroResumo {
  id: number
  nome: string
}

export interface MusicoResumo {
  id: number
  nome: string
}

export interface InstrumentoResumo {
  id: number
  nome: string
  categoria: CategoriaInstrumento
}

export interface CultoResumo {
  id: number
  dataHora: string
  tipo: CultoTipo
}

export interface EscalaResumo {
  id: number
  usuario: MusicoResumo
  instrumento: InstrumentoResumo
  status: EscalaStatus
  confirmadaEm: string | null
}

export interface CultoResponse {
  id: number
  dataHora: string
  tipo: CultoTipo
  ministro: MinistroResumo | null
  observacoes: string | null
  repertorioTrancado: boolean
  totalEscalados: number
}

export interface CultoDetalheResponse extends CultoResponse {
  equipe: EscalaResumo[]
}

export interface CriarCultoRequest {
  dataHora: string
  tipo: CultoTipo
  ministroId?: number
  observacoes?: string
}

export interface EscalarMusicoRequest {
  usuarioId: number
  instrumentoId: number
}

export interface EscalaMinhaResponse {
  id: number
  culto: CultoResumo
  instrumento: InstrumentoResumo
  status: EscalaStatus
  confirmadaEm: string | null
}

export interface InstrumentoResponse {
  id: number
  nome: string
  categoria: CategoriaInstrumento
}

export interface CriarInstrumentoRequest {
  nome: string
  categoria: CategoriaInstrumento
}

export interface UsuarioResponse {
  id: number
  nome: string
  email: string
  ativo: boolean
  papeis: Papel[]
}

export interface AlterarPapelRequest {
  papel: Papel
}
