import type { ButtonHTMLAttributes, ReactNode } from 'react'

type Variant = 'primary' | 'secondary' | 'ghost' | 'danger'
type Size = 'sm' | 'md' | 'lg'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant
  size?: Size
  children: ReactNode
}

const VARIANT_CLASSES: Record<Variant, string> = {
  primary: 'bg-accent text-text-primary hover:brightness-110 disabled:opacity-50',
  secondary: 'bg-transparent border border-border text-text-primary hover:bg-surface-2 disabled:opacity-50',
  ghost: 'bg-transparent text-text-primary hover:bg-surface disabled:opacity-50',
  danger: 'bg-error text-bg hover:brightness-110 disabled:opacity-50',
}

const SIZE_CLASSES: Record<Size, string> = {
  sm: 'text-sm px-3 py-1.5',
  md: 'text-base px-4 py-2.5',
  lg: 'text-lg px-5 py-3',
}

export function Button({ variant = 'primary', size = 'md', className = '', children, ...props }: ButtonProps) {
  return (
    <button
      className={`rounded-btn font-medium transition active:scale-95 ${VARIANT_CLASSES[variant]} ${SIZE_CLASSES[size]} ${className}`}
      {...props}
    >
      {children}
    </button>
  )
}
