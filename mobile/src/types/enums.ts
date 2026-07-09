export type Papel = 'MUSICO' | 'MINISTRO' | 'LIDER'

export type CultoTipo =
  | 'DOMINGO_MANHA'
  | 'DOMINGO_NOITE'
  | 'TERCA'
  | 'QUARTA'
  | 'QUINTA'
  | 'SABADO'
  | 'ESPECIAL'

export type CategoriaInstrumento = 'VOCAL' | 'CORDA' | 'PERCUSSAO' | 'TECLA' | 'SOPRO'

export type EscalaStatus = 'PENDENTE' | 'CONFIRMADA' | 'RECUSADA' | 'SUBSTITUIDA'

export type MotivoSubstituicao = 'VIAGEM' | 'SAUDE' | 'TRABALHO' | 'OUTRO'

export type SolicitacaoStatus = 'ABERTA' | 'APROVADA' | 'REJEITADA'

export const CULTO_TIPO_LABEL: Record<CultoTipo, string> = {
  DOMINGO_MANHA: 'Domingo de manhã',
  DOMINGO_NOITE: 'Domingo à noite',
  TERCA: 'Terça-feira',
  QUARTA: 'Quarta-feira',
  QUINTA: 'Quinta-feira',
  SABADO: 'Sábado',
  ESPECIAL: 'Culto especial',
}

export const CATEGORIA_INSTRUMENTO_LABEL: Record<CategoriaInstrumento, string> = {
  VOCAL: 'Vocal',
  CORDA: 'Corda',
  PERCUSSAO: 'Percussão',
  TECLA: 'Tecla',
  SOPRO: 'Sopro',
}

export const ESCALA_STATUS_LABEL: Record<EscalaStatus, string> = {
  PENDENTE: 'Pendente',
  CONFIRMADA: 'Confirmada',
  RECUSADA: 'Recusada',
  SUBSTITUIDA: 'Substituída',
}

export const ESCALA_STATUS_TONE: Record<EscalaStatus, 'amber' | 'green' | 'red' | 'gray'> = {
  PENDENTE: 'amber',
  CONFIRMADA: 'green',
  RECUSADA: 'red',
  SUBSTITUIDA: 'gray',
}

export const MOTIVO_SUBSTITUICAO_LABEL: Record<MotivoSubstituicao, string> = {
  VIAGEM: 'Viagem',
  SAUDE: 'Saúde',
  TRABALHO: 'Trabalho',
  OUTRO: 'Outro',
}
