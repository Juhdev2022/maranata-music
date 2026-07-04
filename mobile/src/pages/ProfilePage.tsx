import { useNavigate } from 'react-router-dom'
import { PageHeader } from '../components/layout/PageHeader'
import { Button } from '../components/ui/Button'
import { useAuth } from '../hooks/useAuth'

export function ProfilePage() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div>
      <PageHeader title="Perfil" />
      <div className="p-4">
        <p className="text-text-primary">{user?.nome}</p>
        <p className="text-sm text-text-secondary">{user?.email}</p>
        <Button variant="danger" onClick={handleLogout} className="mt-6">
          Sair
        </Button>
      </div>
    </div>
  )
}
