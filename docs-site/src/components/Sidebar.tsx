import { useState } from 'react'
import { NavLink } from 'react-router-dom'
import { navigation } from '../data/navigation'

interface SidebarProps {
  open: boolean
  onClose: () => void
}

export function Sidebar({ open, onClose }: SidebarProps) {
  return (
    <>
      <div
        className={`sidebar-overlay${open ? ' open' : ''}`}
        onClick={onClose}
      />
      <aside className={`app-sidebar${open ? ' open' : ''}`}>
        <nav>
          {navigation.map((section) => (
            <NavSection key={section.title} section={section} onNavigate={onClose} />
          ))}
        </nav>
      </aside>
    </>
  )
}

function NavSection({
  section,
  onNavigate,
}: {
  section: (typeof navigation)[0]
  onNavigate: () => void
}) {
  const [expanded, setExpanded] = useState(true)

  return (
    <div className="nav-section">
      <button
        className="nav-section__title"
        onClick={() => setExpanded(!expanded)}
      >
        {section.title}
        <svg
          className={`nav-section__chevron${expanded ? ' expanded' : ''}`}
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
        >
          <polyline points="9 18 15 12 9 6" />
        </svg>
      </button>
      {expanded && (
        <div className="nav-section__items">
          {section.items.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              end={item.path === '/'}
              className={({ isActive }) =>
                `nav-link${isActive ? ' active' : ''}`
              }
              onClick={onNavigate}
            >
              {item.title}
            </NavLink>
          ))}
        </div>
      )}
    </div>
  )
}
