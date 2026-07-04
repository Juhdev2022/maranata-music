import { addMonths, format, parse, subMonths } from 'date-fns'
import { ptBR } from 'date-fns/locale'

const MES_FORMAT = 'yyyy-MM'

export function mesAtual(): string {
  return format(new Date(), MES_FORMAT)
}

export function mesAdjacente(mes: string, delta: number): string {
  const data = parse(mes, MES_FORMAT, new Date())
  const ajustada = delta >= 0 ? addMonths(data, delta) : subMonths(data, Math.abs(delta))
  return format(ajustada, MES_FORMAT)
}

export function formatMesLabel(mes: string): string {
  const data = parse(mes, MES_FORMAT, new Date())
  const label = format(data, 'MMMM yyyy', { locale: ptBR })
  return label.charAt(0).toUpperCase() + label.slice(1)
}

export function formatCultoDataHora(iso: string): string {
  const data = new Date(iso)
  const dia = format(data, 'EEE', { locale: ptBR }).replace(/\.$/, '')
  const diaCapitalizado = dia.charAt(0).toUpperCase() + dia.slice(1)
  const dataMes = format(data, 'dd MMM', { locale: ptBR })
  const hora = format(data, "HH'h'mm")
  return `${diaCapitalizado}, ${dataMes} · ${hora}`
}
