import type { SelectHTMLAttributes } from 'react'
import { useId } from 'react'

interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label: string
  error?: string
}

export function Select({ label, error, id, className = '', children, ...props }: SelectProps) {
  const generatedId = useId()
  const selectId = id ?? generatedId

  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={selectId} className="text-sm text-text-secondary">
        {label}
      </label>
      <select
        id={selectId}
        className={`rounded-btn border bg-surface px-3.5 py-2.5 text-text-primary outline-none transition focus:ring-2 focus:ring-accent ${
          error ? 'border-error' : 'border-border'
        } ${className}`}
        {...props}
      >
        {children}
      </select>
      {error && <span className="text-sm text-error">{error}</span>}
    </div>
  )
}
