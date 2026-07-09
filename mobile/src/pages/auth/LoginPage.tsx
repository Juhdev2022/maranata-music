import { useState } from 'react'
import type { FormEvent } from 'react'
import { isAxiosError } from 'axios'
import { Link, useNavigate } from 'react-router-dom'
import { Button } from '../../components/ui/Button'
import { Input } from '../../components/ui/Input'
import { authService } from '../../services/authService'
import { useAuthStore } from '../../stores/authStore'

export function LoginPage() {
  const navigate = useNavigate()
  const login = useAuthStore((state) => state.login)
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [carregando, setCarregando] = useState(false)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setCarregando(true)
    try {
      const response = await authService.login({ email, senha })
      login(response)
      navigate('/', { replace: true })
    } catch (error) {
      if (isAxiosError(error) && error.response?.status === 403) {
        navigate('/primeiro-acesso', { state: { email } })
        return
      }
    } finally {
      setCarregando(false)
    }
  }

  return (
    <div className="flex min-h-screen flex-col justify-center px-6 py-12">
      <h1 className="mb-8 text-2xl font-semibold">Maranata Music</h1>
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
          autoComplete="current-password"
          required
          value={senha}
          onChange={(e) => setSenha(e.target.value)}
        />
        <Button type="submit" disabled={carregando} className="mt-2">
          {carregando ? 'Entrando...' : 'Entrar'}
        </Button>
      </form>
      <p className="mt-6 text-center text-sm text-text-secondary">
        <Link to="/registro" className="text-accent-gold">
          Não tenho conta
        </Link>
      </p>
      <p className="mt-2 text-center text-sm text-text-secondary">
        <Link to="/primeiro-acesso" state={{ email }} className="text-accent-gold">
          Primeiro acesso
        </Link>
      </p>
    </div>
  )
}
