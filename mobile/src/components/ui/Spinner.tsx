interface SpinnerProps {
  fullScreen?: boolean
}

export function Spinner({ fullScreen = false }: SpinnerProps) {
  const spinner = (
    <div className="h-6 w-6 animate-spin rounded-full border-2 border-border border-t-accent" />
  )

  if (!fullScreen) return spinner

  return (
    <div className="flex min-h-screen items-center justify-center bg-bg">
      {spinner}
    </div>
  )
}
