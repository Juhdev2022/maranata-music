import { useNavigate } from 'react-router-dom'

interface PageHeaderProps {
  title: string
  onBack?: boolean
}

function BackIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M15 18l-6-6 6-6" />
    </svg>
  )
}

export function PageHeader({ title, onBack = false }: PageHeaderProps) {
  const navigate = useNavigate()

  return (
    <header className="flex items-center gap-3 border-b border-border px-4 py-4">
      {onBack && (
        <button
          type="button"
          onClick={() => navigate(-1)}
          className="text-text-secondary transition active:scale-95"
          aria-label="Voltar"
        >
          <BackIcon />
        </button>
      )}
      <h1 className="text-lg font-semibold">{title}</h1>
    </header>
  )
}
