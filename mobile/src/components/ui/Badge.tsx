import type { ReactNode } from 'react'

type Tone = 'amber' | 'green' | 'red' | 'gray' | 'gold'

interface BadgeProps {
  tone: Tone
  children: ReactNode
}

const TONE_CLASSES: Record<Tone, string> = {
  amber: 'bg-warning/15 text-warning',
  green: 'bg-success/15 text-success',
  red: 'bg-error/15 text-error',
  gray: 'bg-text-secondary/15 text-text-secondary',
  gold: 'bg-accent-gold/15 text-accent-gold',
}

export function Badge({ tone, children }: BadgeProps) {
  return (
    <span className={`rounded-full px-2.5 py-1 text-xs font-medium ${TONE_CLASSES[tone]}`}>{children}</span>
  )
}
