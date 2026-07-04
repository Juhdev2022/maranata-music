import axios from 'axios'
import { useAuthStore } from '../stores/authStore'
import { useToastStore } from '../stores/toastStore'
import { extrairMensagemErro } from '../utils/errorHandler'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
})

api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status

    if (status === 401) {
      useAuthStore.getState().logout()
      window.location.assign('/login')
      return Promise.reject(error)
    }

    if (status === 403) {
      useToastStore.getState().show('Acesso negado', 'error')
      return Promise.reject(error)
    }

    if (status === 400 || status === 409) {
      useToastStore.getState().show(extrairMensagemErro(error), 'error')
      return Promise.reject(error)
    }

    useToastStore.getState().show('Erro inesperado', 'error')
    return Promise.reject(error)
  },
)
