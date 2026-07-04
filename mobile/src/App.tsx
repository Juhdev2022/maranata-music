import { useEffect, useState } from 'react'
import { BrowserRouter } from 'react-router-dom'
import { AppRoutes } from './router'
import { ToastContainer } from './components/ui/Toast'
import { Spinner } from './components/ui/Spinner'
import { useAuthStore } from './stores/authStore'

export function App() {
  const [hydrated, setHydrated] = useState(useAuthStore.getState().hydrated)

  useEffect(() => {
    useAuthStore.getState().hidratar()
    setHydrated(true)
  }, [])

  if (!hydrated) {
    return <Spinner fullScreen />
  }

  return (
    <BrowserRouter>
      <AppRoutes />
      <ToastContainer />
    </BrowserRouter>
  )
}
