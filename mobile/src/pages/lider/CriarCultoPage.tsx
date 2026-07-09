import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { PageHeader } from '../../components/layout/PageHeader'
import { Button } from '../../components/ui/Button'
import { Input } from '../../components/ui/Input'
import { Select } from '../../components/ui/Select'
import { cultoService } from '../../services/cultoService'
import { usuarioService } from '../../services/usuarioService'
import type { UsuarioResponse } from '../../types/api'
import type { CultoTipo } from '../../types/enums'
import { CULTO_TIPO_LABEL } from '../../types/enums'

const TIPOS: CultoTipo[] = ['DOMINGO_MANHA', 'DOMINGO_NOITE', 'TERCA', 'QUARTA', 'QUINTA', 'SABADO', 'ESPECIAL']

export function CriarCultoPage() {
  const navigate = useNavigate()
  const [dataHora, setDataHora] = useState('')
  const [tipo, setTipo] = useState<CultoTipo>('DOMINGO_MANHA')
  const [ministroId, setMinistroId] = useState('')
  const [observacoes, setObservacoes] = useState('')
  const [usuarios, setUsuarios] = useState<UsuarioResponse[]>([])
  const [carregando, setCarregando] = useState(false)

  useEffect(() => {
    usuarioService.listar({ ativos: true }).then(setUsuarios)
  }, [])

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setCarregando(true)
    try {
      const culto = await cultoService.criar({
        dataHora,
        tipo,
        ministroId: ministroId ? Number(ministroId) : undefined,
        observacoes: observacoes || undefined,
      })
      navigate(`/cultos/${culto.id}`, { replace: true })
    } finally {
      setCarregando(false)
    }
  }

  return (
    <div>
      <PageHeader title="Criar culto" onBack />
      <form onSubmit={handleSubmit} className="flex flex-col gap-4 p-4">
        <Input
          label="Data e hora"
          type="datetime-local"
          required
          value={dataHora}
          onChange={(e) => setDataHora(e.target.value)}
        />
        <Select label="Dia da semana" required value={tipo} onChange={(e) => setTipo(e.target.value as CultoTipo)}>
          {TIPOS.map((valor) => (
            <option key={valor} value={valor}>
              {CULTO_TIPO_LABEL[valor]}
            </option>
          ))}
        </Select>
        <Select label="Ministro (opcional)" value={ministroId} onChange={(e) => setMinistroId(e.target.value)}>
          <option value="">Nenhum</option>
          {usuarios.map((usuario) => (
            <option key={usuario.id} value={usuario.id}>
              {usuario.nome}
            </option>
          ))}
        </Select>
        <div className="flex flex-col gap-1.5">
          <label htmlFor="observacoes" className="text-sm text-text-secondary">
            Observações (opcional)
          </label>
          <textarea
            id="observacoes"
            rows={3}
            value={observacoes}
            onChange={(e) => setObservacoes(e.target.value)}
            className="rounded-btn border border-border bg-surface px-3.5 py-2.5 text-text-primary outline-none transition focus:ring-2 focus:ring-accent"
          />
        </div>
        <Button type="submit" disabled={carregando} className="mt-2">
          {carregando ? 'Criando...' : 'Criar culto'}
        </Button>
      </form>
    </div>
  )
}
