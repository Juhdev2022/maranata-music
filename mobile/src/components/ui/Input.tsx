import type { InputHTMLAttributes } from 'react'
import { useId } from 'react'

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string
  error?: string
}

export function Input({ label, error, id, className = '', ...props }: InputProps) {
  const generatedId = useId()
  const inputId = id ?? generatedId

  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={inputId} className="text-sm text-text-secondary">
        {label}
      </label>
      <input
        id={inputId}
        className={`rounded-btn border bg-surface px-3.5 py-2.5 text-text-primary outline-none transition focus:ring-2 focus:ring-accent ${
          error ? 'border-error' : 'border-border'
        } ${className}`}
        {...props}
      />
      {error && <span className="text-sm text-error">{error}</span>}
    </div>
  )
}
