import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Button } from '../../components/ui/Button'
import { Input } from '../../components/ui/Input'
import { authService } from '../../services/authService'
import { useAuthStore } from '../../stores/authStore'

export function PrimeiroAcessoPage() {
  const navigate = useNavigate()
  const login = useAuthStore((state) => state.login)
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [confirmarSenha, setConfirmarSenha] = useState('')
  const [carregando, setCarregando] = useState(false)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setCarregando(true)
    try {
      const response = await authService.definirSenha({ email, senha, confirmarSenha })
      login(response)
      navigate('/', { replace: true })
    } finally {
      setCarregando(false)
    }
  }

  return (
    <div className="flex min-h-screen flex-col justify-center px-6 py-12">
      <h1 className="mb-2 text-2xl font-semibold">Primeiro acesso</h1>
      <p className="mb-8 text-sm text-text-secondary">
        Seu líder já criou sua conta. Defina sua senha pra começar a usar o app.
      </p>
      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
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
          label="Confirmar senha"
          type="password"
          autoComplete="new-password"
          minLength={8}
          required
          value={confirmarSenha}
          onChange={(e) => setConfirmarSenha(e.target.value)}
        />
        <Button type="submit" disabled={carregando} className="mt-2">
          {carregando ? 'Definindo senha...' : 'Definir senha e entrar'}
        </Button>
      </form>
      <p className="mt-6 text-center text-sm text-text-secondary">
        <Link to="/login" className="text-accent-gold">
          Voltar pro login
        </Link>
      </p>
    </div>
  )
}
