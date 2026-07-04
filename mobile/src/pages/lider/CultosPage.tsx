import { PageHeader } from '../../components/layout/PageHeader'
import { EmptyState } from '../../components/ui/EmptyState'

export function CultosPage() {
  return (
    <div>
      <PageHeader title="Cultos" />
      <EmptyState title="Em construção" description="Chega no próximo milestone." />
    </div>
  )
}
