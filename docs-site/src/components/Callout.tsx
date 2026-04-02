import type { ReactNode } from 'react'

interface CalloutProps {
  type: 'tip' | 'warning' | 'info'
  title?: string
  children: ReactNode
}

const icons = {
  tip: (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M9 18h6" />
      <path d="M10 22h4" />
      <path d="M12 2a7 7 0 0 0-4 12.7V17h8v-2.3A7 7 0 0 0 12 2z" />
    </svg>
  ),
  warning: (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
      <line x1="12" y1="9" x2="12" y2="13" />
      <line x1="12" y1="17" x2="12.01" y2="17" />
    </svg>
  ),
  info: (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="10" />
      <line x1="12" y1="16" x2="12" y2="12" />
      <line x1="12" y1="8" x2="12.01" y2="8" />
    </svg>
  ),
}

const defaultTitles = {
  tip: 'Dica',
  warning: 'Atenção',
  info: 'Info',
}

export function Callout({ type, title, children }: CalloutProps) {
  return (
    <div className={`callout callout--${type}`}>
      <div className="callout__icon">{icons[type]}</div>
      <div className="callout__content">
        <div className="callout__title">{title || defaultTitles[type]}</div>
        {children}
      </div>
    </div>
  )
}
