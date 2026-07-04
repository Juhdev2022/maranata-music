import { useAuthStore } from '../stores/authStore'

export function useAuth() {
  const user = useAuthStore((state) => state.user)
  const token = useAuthStore((state) => state.token)
  const logout = useAuthStore((state) => state.logout)

  return { user, token, isAuthenticated: Boolean(token), logout }
}
