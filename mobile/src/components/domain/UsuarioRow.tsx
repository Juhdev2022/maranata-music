import { useState } from 'react'
import type { ReactNode } from 'react'
import { Badge } from '../ui/Badge'
import { Card } from '../ui/Card'
import { ConfirmModal } from '../ui/ConfirmModal'
import { useToast } from '../../hooks/useToast'
import { usuarioService } from '../../services/usuarioService'
import type { UsuarioResponse } from '../../types/api'
import { InstrumentosDoMusicoModal } from './InstrumentosDoMusicoModal'

interface UsuarioRowProps {
  usuario: UsuarioResponse
  onChanged: () => void
}

function KebabIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
      <circle cx="12" cy="5" r="1.5" />
      <circle cx="12" cy="12" r="1.5" />
      <circle cx="12" cy="19" r="1.5" />
    </svg>
  )
}

function MenuItem({ onClick, children }: { onClick: () => void; children: ReactNode }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className="w-full px-4 py-2.5 text-left text-sm text-text-primary transition hover:bg-surface active:scale-95"
    >
      {children}
    </button>
  )
}

export function UsuarioRow({ usuario, onChanged }: UsuarioRowProps) {
  const showToast = useToast()
  const isLider = usuario.papeis.includes('LIDER')

  const [menuAberto, setMenuAberto] = useState(false)
  const [modalInstrumentosAberto, setModalInstrumentosAberto] = useState(false)
  const [confirmDesativar, setConfirmDesativar] = useState(false)
  const [confirmRemoverLider, setConfirmRemoverLider] = useState(false)
  const [processando, setProcessando] = useState(false)

  async function handlePromover() {
    setMenuAberto(false)
    await usuarioService.adicionarPapel(usuario.id, 'LIDER')
    showToast(`Papel LIDER adicionado a ${usuario.nome}`, 'success')
    onChanged()
  }

  async function handleConfirmRemoverLider() {
    setProcessando(true)
    try {
      await usuarioService.removerPapel(usuario.id, 'LIDER')
      showToast(`Papel LIDER removido de ${usuario.nome}`, 'success')
      setConfirmRemoverLider(false)
      onChanged()
    } finally {
      setProcessando(false)
    }
  }

  async function handleConfirmDesativar() {
    setProcessando(true)
    try {
      await usuarioService.desativar(usuario.id)
      showToast(`Usuário ${usuario.nome} desativado`, 'success')
      setConfirmDesativar(false)
      onChanged()
    } finally {
      setProcessando(false)
    }
  }

  async function handleReativar() {
    setMenuAberto(false)
    await usuarioService.reativar(usuario.id)
    showToast(`Usuário ${usuario.nome} reativado`, 'success')
    onChanged()
  }

  return (
    <Card className="flex items-center justify-between gap-3">
      <div className="flex min-w-0 flex-col gap-1.5">
        <div className="flex items-center gap-2">
          <span
            className={`h-2 w-2 shrink-0 rounded-full ${usuario.ativo ? 'bg-success' : 'bg-text-secondary'}`}
            aria-hidden="true"
          />
          <p className="truncate text-text-primary">{usuario.nome}</p>
        </div>
        <p className="truncate text-sm text-text-secondary">{usuario.email}</p>
        <div className="flex flex-wrap gap-1.5">
          {usuario.papeis.map((papel) => (
            <Badge key={papel} tone="gray">
              {papel}
            </Badge>
          ))}
        </div>
      </div>

      <div className="relative shrink-0">
        <button
          type="button"
          aria-label="Mais ações"
          onClick={() => setMenuAberto((v) => !v)}
          className="p-1 text-text-secondary transition active:scale-95"
        >
          <KebabIcon />
        </button>

        {menuAberto && (
          <>
            <div className="fixed inset-0 z-40" onClick={() => setMenuAberto(false)} />
            <div className="absolute right-0 top-full z-50 mt-1 w-52 overflow-hidden rounded-btn border border-border bg-surface-2 py-1 shadow-lg">
              {usuario.ativo && !isLider && <MenuItem onClick={handlePromover}>Promover a LIDER</MenuItem>}
              {usuario.ativo && isLider && (
                <MenuItem
                  onClick={() => {
                    setMenuAberto(false)
                    setConfirmRemoverLider(true)
                  }}
                >
                  Remover LIDER
                </MenuItem>
              )}
              {usuario.ativo && (
                <MenuItem
                  onClick={() => {
                    setMenuAberto(false)
                    setConfirmDesativar(true)
                  }}
                >
                  Desativar
                </MenuItem>
              )}
              {!usuario.ativo && <MenuItem onClick={handleReativar}>Reativar</MenuItem>}
              <MenuItem
                onClick={() => {
                  setMenuAberto(false)
                  setModalInstrumentosAberto(true)
                }}
              >
                Ver instrumentos
              </MenuItem>
            </div>
          </>
        )}
      </div>

      <ConfirmModal
        isOpen={confirmDesativar}
        onClose={() => setConfirmDesativar(false)}
        onConfirm={handleConfirmDesativar}
        title="Desativar usuário"
        message={`Desativar ${usuario.nome}? Ele/ela não conseguirá mais fazer login.`}
        confirmLabel="Desativar"
        confirming={processando}
      />

      <ConfirmModal
        isOpen={confirmRemoverLider}
        onClose={() => setConfirmRemoverLider(false)}
        onConfirm={handleConfirmRemoverLider}
        title="Remover LIDER"
        message={`Remover papel LIDER de ${usuario.nome}?`}
        confirmLabel="Remover"
        confirming={processando}
      />

      <InstrumentosDoMusicoModal
        usuario={usuario}
        isOpen={modalInstrumentosAberto}
        onClose={() => setModalInstrumentosAberto(false)}
      />
    </Card>
  )
}
