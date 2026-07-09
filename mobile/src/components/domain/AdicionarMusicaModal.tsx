import { useState } from 'react'
import { Button } from '../ui/Button'
import { Input } from '../ui/Input'
import { Modal } from '../ui/Modal'

interface AdicionarMusicaModalProps {
  isOpen: boolean
  onClose: () => void
  onAdicionar: (titulo: string, linkVideo: string) => Promise<void>
}

export function AdicionarMusicaModal({ isOpen, onClose, onAdicionar }: AdicionarMusicaModalProps) {
  const [titulo, setTitulo] = useState('')
  const [linkVideo, setLinkVideo] = useState('')
  const [salvando, setSalvando] = useState(false)

  function handleClose() {
    setTitulo('')
    setLinkVideo('')
    onClose()
  }

  async function handleSalvar() {
    setSalvando(true)
    try {
      await onAdicionar(titulo, linkVideo)
      setTitulo('')
      setLinkVideo('')
    } finally {
      setSalvando(false)
    }
  }

  return (
    <Modal isOpen={isOpen} onClose={handleClose} title="Adicionar música">
      <div className="flex flex-col gap-4">
        <Input
          label="Título"
          value={titulo}
          onChange={(e) => setTitulo(e.target.value)}
          placeholder="Ex.: Grandes Coisas"
        />
        <Input
          label="Link do YouTube (opcional)"
          value={linkVideo}
          onChange={(e) => setLinkVideo(e.target.value)}
          placeholder="https://youtube.com/..."
        />
        <Button onClick={handleSalvar} disabled={!titulo.trim() || salvando}>
          {salvando ? 'Adicionando...' : 'Adicionar'}
        </Button>
      </div>
    </Modal>
  )
}
