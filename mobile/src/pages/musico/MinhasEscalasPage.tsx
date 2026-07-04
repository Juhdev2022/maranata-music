import { PageHeader } from '../../components/layout/PageHeader'
import { EmptyState } from '../../components/ui/EmptyState'

export function MinhasEscalasPage() {
  return (
    <div>
      <PageHeader title="Minhas escalas" />
      <EmptyState title="Em construção" description="Chega no próximo milestone." />
    </div>
  )
}
