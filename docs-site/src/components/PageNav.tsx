import { Link } from 'react-router-dom'
import { getAdjacentPages } from '../data/navigation'

interface PageNavProps {
  currentPath: string
}

export function PageNav({ currentPath }: PageNavProps) {
  const { prev, next } = getAdjacentPages(currentPath)

  if (!prev && !next) return null

  return (
    <nav className="page-nav">
      {prev ? (
        <Link to={prev.path} className="page-nav__link">
          <span className="page-nav__label">Anterior</span>
          <span className="page-nav__title">← {prev.title}</span>
        </Link>
      ) : (
        <div />
      )}
      {next ? (
        <Link to={next.path} className="page-nav__link page-nav__link--next">
          <span className="page-nav__label">Próximo</span>
          <span className="page-nav__title">{next.title} →</span>
        </Link>
      ) : (
        <div />
      )}
    </nav>
  )
}
