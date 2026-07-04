import type { ReactNode } from 'react'
import { NavLink } from 'react-router-dom'
import { useIsLider } from '../../hooks/useIsLider'

interface Tab {
  to: string
  label: string
  icon: ReactNode
}

function EscalasIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6">
      <rect x="3" y="5" width="18" height="16" rx="2" />
      <path d="M3 9h18M8 3v4M16 3v4" />
      <path d="M8 14l2.5 2.5L16 11" />
    </svg>
  )
}

function CultosIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6">
      <rect x="3" y="5" width="18" height="16" rx="2" />
      <path d="M3 9h18M8 3v4M16 3v4" />
    </svg>
  )
}

function GestaoIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6">
      <circle cx="12" cy="8" r="3" />
      <path d="M5 21v-1a7 7 0 0 1 14 0v1" />
    </svg>
  )
}

function PerfilIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6">
      <circle cx="12" cy="8" r="3.5" />
      <path d="M5 20a7 7 0 0 1 14 0" />
    </svg>
  )
}

const MUSICO_TABS: Tab[] = [
  { to: '/escalas', label: 'Escalas', icon: <EscalasIcon /> },
  { to: '/perfil', label: 'Perfil', icon: <PerfilIcon /> },
]

const LIDER_TABS: Tab[] = [
  { to: '/escalas', label: 'Escalas', icon: <EscalasIcon /> },
  { to: '/cultos', label: 'Cultos', icon: <CultosIcon /> },
  { to: '/gestao', label: 'Gestão', icon: <GestaoIcon /> },
  { to: '/perfil', label: 'Perfil', icon: <PerfilIcon /> },
]

export function BottomNav() {
  const isLider = useIsLider()
  const tabs = isLider ? LIDER_TABS : MUSICO_TABS

  return (
    <nav className="fixed inset-x-0 bottom-0 z-40 border-t border-border bg-surface pb-[env(safe-area-inset-bottom)]">
      <div className="mx-auto flex max-w-md">
        {tabs.map((tab) => (
          <NavLink
            key={tab.to}
            to={tab.to}
            className={({ isActive }) =>
              `flex flex-1 flex-col items-center gap-1 py-2.5 text-xs transition active:scale-95 ${
                isActive ? 'text-accent' : 'text-text-secondary'
              }`
            }
          >
            {tab.icon}
            {tab.label}
          </NavLink>
        ))}
      </div>
    </nav>
  )
}
