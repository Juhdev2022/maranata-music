import { useNavigate } from 'react-router-dom'
import { PageHeader } from '../components/layout/PageHeader'
import { Badge } from '../components/ui/Badge'
import { Button } from '../components/ui/Button'
import { Card } from '../components/ui/Card'
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
        <Card className="flex flex-col gap-3">
          <div>
            <p className="text-lg text-text-primary">{user?.nome}</p>
            <p className="text-sm text-text-secondary">{user?.email}</p>
          </div>
          <div className="flex flex-wrap gap-2">
            {user?.papeis.map((papel) => (
              <Badge key={papel} tone="gray">
                {papel}
              </Badge>
            ))}
          </div>
        </Card>
        <Button variant="danger" onClick={handleLogout} className="mt-6">
          Sair
        </Button>
      </div>
    </div>
  )
}
