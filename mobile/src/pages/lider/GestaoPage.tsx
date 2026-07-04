import { PageHeader } from '../../components/layout/PageHeader'
import { EmptyState } from '../../components/ui/EmptyState'

export function GestaoPage() {
  return (
    <div>
      <PageHeader title="Gestão" />
      <EmptyState title="Em construção" description="Instrumentos e usuários chegam no próximo milestone." />
    </div>
  )
}
