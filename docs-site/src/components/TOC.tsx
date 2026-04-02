import { useScrollSpy } from '../hooks/useScrollSpy'

export interface TOCItem {
  id: string
  title: string
}

interface TOCProps {
  items: TOCItem[]
}

export function TOC({ items }: TOCProps) {
  const activeId = useScrollSpy(items.map((i) => i.id))

  if (items.length === 0) return null

  return (
    <div className="app-toc">
      <div className="toc__title">Nesta página</div>
      <ul className="toc__list">
        {items.map((item) => (
          <li key={item.id} className="toc__item">
            <a
              href={`#${item.id}`}
              className={`toc__link${activeId === item.id ? ' active' : ''}`}
              onClick={(e) => {
                e.preventDefault()
                document.getElementById(item.id)?.scrollIntoView({ behavior: 'smooth' })
              }}
            >
              {item.title}
            </a>
          </li>
        ))}
      </ul>
    </div>
  )
}
