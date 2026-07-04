import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Button } from '../../components/ui/Button'
import { Input } from '../../components/ui/Input'
import { authService } from '../../services/authService'
import { useAuthStore } from '../../stores/authStore'

export function RegistroPage() {
  const navigate = useNavigate()
  const login = useAuthStore((state) => state.login)
  const [nome, setNome] = useState('')
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [telefone, setTelefone] = useState('')
  const [carregando, setCarregando] = useState(false)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setCarregando(true)
    try {
      const response = await authService.registro({ nome, email, senha, telefone })
      login(response)
      navigate('/', { replace: true })
    } finally {
      setCarregando(false)
    }
  }

  return (
    <div className="flex min-h-screen flex-col justify-center px-6 py-12">
      <h1 className="mb-8 text-2xl font-semibold">Criar conta</h1>
      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <Input label="Nome" required value={nome} onChange={(e) => setNome(e.target.value)} />
        <Input
          label="Email"
          type="email"
          autoComplete="email"
          required
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <Input
          label="Senha"
          type="password"
          autoComplete="new-password"
          minLength={8}
          required
          value={senha}
          onChange={(e) => setSenha(e.target.value)}
        />
        <Input
          label="Telefone"
          type="tel"
          autoComplete="tel"
          value={telefone}
          onChange={(e) => setTelefone(e.target.value)}
        />
        <Button type="submit" disabled={carregando} className="mt-2">
          {carregando ? 'Criando conta...' : 'Criar conta'}
        </Button>
      </form>
      <p className="mt-6 text-center text-sm text-text-secondary">
        Já tenho conta.{' '}
        <Link to="/login" className="text-accent-gold">
          Entrar
        </Link>
      </p>
    </div>
  )
}
