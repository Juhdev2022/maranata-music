export type Papel = 'MUSICO' | 'MINISTRO' | 'LIDER'

export type CultoTipo = 'DOMINGO_MANHA' | 'DOMINGO_NOITE' | 'QUARTA' | 'ESPECIAL'

export type CategoriaInstrumento = 'VOCAL' | 'CORDA' | 'PERCUSSAO' | 'TECLA' | 'SOPRO'

export type EscalaStatus = 'PENDENTE' | 'CONFIRMADA' | 'RECUSADA' | 'SUBSTITUIDA'

export const CULTO_TIPO_LABEL: Record<CultoTipo, string> = {
  DOMINGO_MANHA: 'Domingo de manhã',
  DOMINGO_NOITE: 'Domingo à noite',
  QUARTA: 'Quarta-feira',
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
