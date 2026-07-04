import { useAuthStore } from '../stores/authStore'

export function useIsLider() {
  return useAuthStore((state) => state.user?.papeis.includes('LIDER') ?? false)
}
