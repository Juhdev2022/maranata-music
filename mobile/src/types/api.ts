import type { CategoriaInstrumento, CultoTipo, EscalaStatus, MotivoSubstituicao, Papel, SolicitacaoStatus } from './enums'

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

export interface DefinirSenhaRequest {
  email: string
  senha: string
  confirmarSenha: string
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
}

export interface AtualizarObservacoesRequest {
  observacoes: string | null
}

export interface EscalaMinhaResponse {
  id: number
  culto: CultoResumo
  instrumento: InstrumentoResumo
  status: EscalaStatus
  confirmadaEm: string | null
  solicitacaoAberta: boolean
}

export interface SolicitarSubstituicaoRequest {
  escalaId: number
  motivo: MotivoSubstituicao
  observacao?: string
  substitutoSugeridoId?: number
}

export interface AprovarSubstituicaoRequest {
  substitutoFinalId: number | null
}

export interface SolicitacaoResponse {
  id: number
  escalaId: number
  solicitante: MusicoResumo
  culto: CultoResumo
  instrumento: InstrumentoResumo
  motivo: MotivoSubstituicao
  observacao: string | null
  substitutoSugerido: MusicoResumo | null
  status: SolicitacaoStatus
  substitutoFinal: MusicoResumo | null
}

export interface SubstitutoElegivelResponse {
  id: number
  nome: string
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
  precisaDefinirSenha: boolean
}

export interface CriarUsuarioRequest {
  nome: string
  email: string
  telefone?: string
}

export interface AlterarPapelRequest {
  papel: Papel
}

export interface MusicoInstrumentoResponse {
  usuarioId: number
  usuarioNome: string
  instrumentoId: number
  instrumentoNome: string
  categoria: CategoriaInstrumento
  principal: boolean
}
