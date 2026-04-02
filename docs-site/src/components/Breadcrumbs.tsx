import { Link, useLocation } from 'react-router-dom'
import { navigation } from '../data/navigation'

export function Breadcrumbs() {
  const location = useLocation()
  const currentPath = location.pathname

  if (currentPath === '/') return null

  // Find the section and item
  let sectionTitle = ''
  let pageTitle = ''

  for (const section of navigation) {
    for (const item of section.items) {
      if (item.path === currentPath) {
        sectionTitle = section.title
        pageTitle = item.title
        break
      }
    }
    if (pageTitle) break
  }

  if (!pageTitle) return null

  return (
    <nav className="breadcrumbs" aria-label="Breadcrumb">
      <Link to="/" className="breadcrumbs__link">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="14" height="14">
          <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
          <polyline points="9 22 9 12 15 12 15 22" />
        </svg>
        Home
      </Link>
      <svg className="breadcrumbs__separator" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
        <polyline points="9 18 15 12 9 6" />
      </svg>
      <span className="breadcrumbs__link">{sectionTitle}</span>
      <svg className="breadcrumbs__separator" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
        <polyline points="9 18 15 12 9 6" />
      </svg>
      <span className="breadcrumbs__current">{pageTitle}</span>
    </nav>
  )
}
