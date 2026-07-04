import { useToastStore } from '../../stores/toastStore'
import type { ToastType } from '../../stores/toastStore'

const TYPE_CLASSES: Record<ToastType, string> = {
  success: 'border-success text-success',
  error: 'border-error text-error',
  info: 'border-border text-text-primary',
}

export function ToastContainer() {
  const toasts = useToastStore((state) => state.toasts)

  if (toasts.length === 0) return null

  return (
    <div className="fixed inset-x-0 top-4 z-50 flex flex-col items-center gap-2 px-4">
      {toasts.map((toast) => (
        <div
          key={toast.id}
          className={`w-full max-w-sm rounded-btn border bg-surface-2 px-4 py-3 text-sm shadow-lg ${TYPE_CLASSES[toast.type]}`}
        >
          {toast.message}
        </div>
      ))}
    </div>
  )
}
