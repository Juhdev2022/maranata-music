import type { ReactNode } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import { AppLayout } from './components/layout/AppLayout'
import { ProtectedRoute } from './components/layout/ProtectedRoute'
import { useAuth } from './hooks/useAuth'
import { useIsLider } from './hooks/useIsLider'
import { LoginPage } from './pages/auth/LoginPage'
import { RegistroPage } from './pages/auth/RegistroPage'
import { CultosPage } from './pages/lider/CultosPage'
import { GestaoPage } from './pages/lider/GestaoPage'
import { MinhasEscalasPage } from './pages/musico/MinhasEscalasPage'
import { ProfilePage } from './pages/ProfilePage'

function RootRedirect() {
  const { isAuthenticated } = useAuth()
  return <Navigate to={isAuthenticated ? '/escalas' : '/login'} replace />
}

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<RootRedirect />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/registro" element={<RegistroPage />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route path="/escalas" element={<MinhasEscalasPage />} />
          <Route path="/perfil" element={<ProfilePage />} />
          <Route path="/cultos" element={<LiderGuard><CultosPage /></LiderGuard>} />
          <Route path="/gestao" element={<LiderGuard><GestaoPage /></LiderGuard>} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

function LiderGuard({ children }: { children: ReactNode }) {
  const isLider = useIsLider()
  if (!isLider) return <Navigate to="/escalas" replace />
  return children
}
