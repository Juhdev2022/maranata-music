import { useState } from 'react'
import type { FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { PageHeader } from '../../components/layout/PageHeader'
import { Button } from '../../components/ui/Button'
import { Input } from '../../components/ui/Input'
import { useToast } from '../../hooks/useToast'
import { usuarioService } from '../../services/usuarioService'

export function CriarUsuarioPage() {
  const navigate = useNavigate()
  const showToast = useToast()
  const [nome, setNome] = useState('')
  const [email, setEmail] = useState('')
  const [telefone, setTelefone] = useState('')
  const [criando, setCriando] = useState(false)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setCriando(true)
    try {
      await usuarioService.criar({
        nome,
        email,
        telefone: telefone || undefined,
      })
      showToast('Usuário criado', 'success')
      navigate('/usuarios', { replace: true })
    } finally {
      setCriando(false)
    }
  }

  return (
    <div>
      <PageHeader title="Adicionar usuário" onBack />
      <form onSubmit={handleSubmit} className="flex flex-col gap-4 p-4">
        <Input label="Nome" required value={nome} onChange={(e) => setNome(e.target.value)} />
        <Input
          label="Email"
          type="email"
          required
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <Input
          label="Telefone (opcional)"
          value={telefone}
          onChange={(e) => setTelefone(e.target.value)}
        />
        <Button type="submit" disabled={criando} className="mt-2">
          {criando ? 'Criando...' : 'Criar usuário'}
        </Button>
      </form>
    </div>
  )
}
