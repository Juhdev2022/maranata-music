import { useNavigate } from 'react-router-dom'
import { PageHeader } from '../../components/layout/PageHeader'
import { Card } from '../../components/ui/Card'

function InstrumentoIcon() {
  return (
    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
      <circle cx="7" cy="17" r="3" />
      <path d="M10 17V5l8-2v12" />
      <circle cx="18" cy="15" r="3" />
    </svg>
  )
}

function UsuariosIcon() {
  return (
    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
      <circle cx="9" cy="8" r="3.5" />
      <path d="M2.5 20a6.5 6.5 0 0 1 13 0" />
      <circle cx="17.5" cy="9" r="2.8" />
      <path d="M15.5 20a5 5 0 0 1 6.5-4.77" />
    </svg>
  )
}

export function GestaoPage() {
  const navigate = useNavigate()

  return (
    <div>
      <PageHeader title="Gestão" />
      <div className="flex flex-col gap-3 p-4">
        <Card
          onClick={() => navigate('/instrumentos')}
          className="flex cursor-pointer items-center gap-4 transition active:scale-95"
        >
          <div className="text-accent">
            <InstrumentoIcon />
          </div>
          <p className="text-lg text-text-primary">Instrumentos</p>
        </Card>
        <Card
          onClick={() => navigate('/usuarios')}
          className="flex cursor-pointer items-center gap-4 transition active:scale-95"
        >
          <div className="text-accent">
            <UsuariosIcon />
          </div>
          <p className="text-lg text-text-primary">Usuários</p>
        </Card>
      </div>
    </div>
  )
}
