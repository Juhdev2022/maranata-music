import { Button } from './Button'
import { Modal } from './Modal'

interface ConfirmModalProps {
  isOpen: boolean
  onClose: () => void
  onConfirm: () => void
  title: string
  message: string
  confirmLabel?: string
  confirming?: boolean
}

export function ConfirmModal({
  isOpen,
  onClose,
  onConfirm,
  title,
  message,
  confirmLabel = 'Confirmar',
  confirming = false,
}: ConfirmModalProps) {
  return (
    <Modal isOpen={isOpen} onClose={onClose} title={title}>
      <p className="mb-6 text-text-secondary">{message}</p>
      <div className="flex gap-2">
        <Button variant="secondary" onClick={onClose} disabled={confirming} className="flex-1">
          Cancelar
        </Button>
        <Button variant="danger" onClick={onConfirm} disabled={confirming} className="flex-1">
          {confirming ? 'Aguarde...' : confirmLabel}
        </Button>
      </div>
    </Modal>
  )
}
