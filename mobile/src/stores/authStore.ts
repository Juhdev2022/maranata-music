import { create } from 'zustand'
import type { AuthResponse } from '../types/api'
import type { Papel } from '../types/enums'

const STORAGE_KEY = 'maranata_auth'

export interface AuthUser {
  nome: string
  email: string
  papeis: Papel[]
}

interface PersistedAuth {
  token: string
  user: AuthUser
}

interface AuthState {
  user: AuthUser | null
  token: string | null
  hydrated: boolean
  login: (response: AuthResponse) => void
  logout: () => void
  hidratar: () => void
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  token: null,
  hydrated: false,

  login: (response) => {
    const user: AuthUser = { nome: response.nome, email: response.email, papeis: response.papeis }
    const persisted: PersistedAuth = { token: response.token, user }
    localStorage.setItem(STORAGE_KEY, JSON.stringify(persisted))
    set({ user, token: response.token })
  },

  logout: () => {
    localStorage.removeItem(STORAGE_KEY)
    set({ user: null, token: null })
  },

  hidratar: () => {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) {
      set({ hydrated: true })
      return
    }
    try {
      const persisted = JSON.parse(raw) as PersistedAuth
      set({ user: persisted.user, token: persisted.token, hydrated: true })
    } catch {
      localStorage.removeItem(STORAGE_KEY)
      set({ hydrated: true })
    }
  },
}))
