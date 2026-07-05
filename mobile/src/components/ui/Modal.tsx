import { useEffect } from 'react'
import type { ReactNode } from 'react'

interface ModalProps {
  isOpen: boolean
  onClose: () => void
  title?: string
  children: ReactNode
}

export function Modal({ isOpen, onClose, title, children }: ModalProps) {
  useEffect(() => {
    if (!isOpen) return

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') onClose()
    }

    document.addEventListener('keydown', handleKeyDown)
    return () => document.removeEventListener('keydown', handleKeyDown)
  }, [isOpen, onClose])

  if (!isOpen) return null

  return (
    <div
      className="fixed inset-0 z-50 flex items-end justify-center bg-black/60 sm:items-center sm:p-4"
      onClick={onClose}
    >
      <div
        className="max-h-[85vh] w-full max-w-sm overflow-y-auto rounded-t-card border border-border bg-surface-2 p-5 sm:rounded-card"
        onClick={(event) => event.stopPropagation()}
      >
        {title && <h2 className="mb-4 text-lg font-semibold text-text-primary">{title}</h2>}
        {children}
      </div>
    </div>
  )
}
