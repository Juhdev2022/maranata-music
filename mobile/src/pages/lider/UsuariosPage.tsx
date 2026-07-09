import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { PageHeader } from '../../components/layout/PageHeader'
import { UsuarioRow } from '../../components/domain/UsuarioRow'
import { Button } from '../../components/ui/Button'
import { EmptyState } from '../../components/ui/EmptyState'
import { Spinner } from '../../components/ui/Spinner'
import { usuarioService } from '../../services/usuarioService'
import type { UsuarioResponse } from '../../types/api'

export function UsuariosPage() {
  const navigate = useNavigate()
  const [mostrarInativos, setMostrarInativos] = useState(false)
  const [usuarios, setUsuarios] = useState<UsuarioResponse[] | null>(null)
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState(false)

  function carregar() {
    setCarregando(true)
    setErro(false)
    const requisicao = mostrarInativos
      ? Promise.all([usuarioService.listar({ ativos: true }), usuarioService.listar({ ativos: false })]).then(
          ([ativos, inativos]) => [...ativos, ...inativos].sort((a, b) => a.nome.localeCompare(b.nome)),
        )
      : usuarioService.listar({ ativos: true })

    requisicao
      .then(setUsuarios)
      .catch(() => setErro(true))
      .finally(() => setCarregando(false))
  }

  useEffect(carregar, [mostrarInativos])

  return (
    <div className="relative min-h-screen">
      <PageHeader title="Usuários" />

      <div className="flex items-center justify-between px-4 py-3">
        <label className="flex items-center gap-2 text-sm text-text-secondary">
          <input
            type="checkbox"
            checked={mostrarInativos}
            onChange={(e) => setMostrarInativos(e.target.checked)}
            className="accent-accent"
          />
          Mostrar inativos
        </label>
        <Button size="sm" onClick={() => navigate('/usuarios/novo')}>
          Adicionar usuário
        </Button>
      </div>

      <div className="flex flex-col gap-3 px-4 pb-6">
        {carregando && (
          <div className="flex justify-center py-12">
            <Spinner />
          </div>
        )}

        {!carregando && erro && (
          <EmptyState title="Não deu pra carregar os usuários" description="Verifique sua conexão e tente de novo." />
        )}

        {!carregando && !erro && usuarios?.length === 0 && <EmptyState title="Nenhum usuário encontrado" />}

        {!carregando &&
          !erro &&
          usuarios?.map((usuario) => <UsuarioRow key={usuario.id} usuario={usuario} onChanged={carregar} />)}
      </div>
    </div>
  )
}
