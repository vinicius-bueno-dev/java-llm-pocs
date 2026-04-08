import { useState, useEffect, useCallback, useRef } from 'react'
import { useNavigate } from 'react-router-dom'

interface SearchEntry {
  title: string
  section: string
  path: string
  keywords: string[]
}

const searchIndex: SearchEntry[] = [
  {
    title: 'Home',
    section: 'Inicio',
    path: '/',
    keywords: ['home', 'inicio', 'java', 'spring', 'localstack', 'terraform', 'poc', 'setup'],
  },
  {
    title: 'Claude Code — Basico',
    section: 'Claude Code',
    path: '/claude-code/basics',
    keywords: ['claude', 'code', 'cli', 'basico', 'comandos', 'hooks', 'slash', 'terminal', 'anthropic'],
  },
  {
    title: 'LocalStack — Conceitos',
    section: 'LocalStack & Terraform',
    path: '/localstack/concepts',
    keywords: ['localstack', 'aws', 'docker', 'conceitos', 'servicos', 's3', 'sqs', 'dynamodb', 'lambda'],
  },
  {
    title: 'Terraform + LocalStack',
    section: 'LocalStack & Terraform',
    path: '/localstack/terraform',
    keywords: ['terraform', 'iac', 'infra', 'provider', 'modulos', 'hcl', 'apply', 'plan', 'init'],
  },
  // Section anchors
  {
    title: 'Stack Tecnologica',
    section: 'Home',
    path: '/#stack',
    keywords: ['stack', 'java', 'spring', 'maven', 'aws', 'sdk'],
  },
  {
    title: 'Setup Rapido',
    section: 'Home',
    path: '/#setup',
    keywords: ['setup', 'instalar', 'rodar', 'docker', 'compose', 'terraform', 'init'],
  },
  {
    title: 'POCs Planejadas',
    section: 'Home',
    path: '/#pocs',
    keywords: ['poc', 's3', 'sqs', 'dynamodb', 'lambda', 'event', 'driven'],
  },
  {
    title: 'Pre-requisitos',
    section: 'Home',
    path: '/#prereqs',
    keywords: ['prerequisitos', 'java', 'docker', 'terraform', 'git', 'versao'],
  },
  {
    title: 'Roadmap de Servicos AWS',
    section: 'POCs',
    path: '/pocs/roadmap',
    keywords: ['roadmap', 'servicos', 'aws', 'planejamento', 'progresso', 's3', 'sqs', 'dynamodb', 'lambda', 'sns', 'kinesis', 'eventbridge', 'cloudwatch', 'secrets', 'iam', 'kms', 'api gateway', 'step functions', 'elasticache', 'cloudfront', 'ses'],
  },
  {
    title: 'POC S3 Storage',
    section: 'POCs',
    path: '/pocs/s3-storage',
    keywords: ['s3', 'storage', 'bucket', 'upload', 'download', 'presigned', 'versioning', 'multipart', 'encryption', 'cors', 'lifecycle', 'notification', 'tagging', 'acl', 'policy', 'website', 'poc', 'crud'],
  },
]

function fuzzyMatch(query: string, entry: SearchEntry): number {
  const q = query.toLowerCase()
  const title = entry.title.toLowerCase()
  const section = entry.section.toLowerCase()

  if (title.includes(q)) return 100
  if (section.includes(q)) return 80

  const keywords = entry.keywords.join(' ')
  if (keywords.includes(q)) return 60

  // Character-by-character fuzzy
  let score = 0
  let qi = 0
  for (let i = 0; i < title.length && qi < q.length; i++) {
    if (title[i] === q[qi]) {
      score += 10
      qi++
    }
  }
  return qi === q.length ? score : 0
}

interface CommandPaletteProps {
  open: boolean
  onClose: () => void
}

export function CommandPalette({ open, onClose }: CommandPaletteProps) {
  const [query, setQuery] = useState('')
  const [selectedIndex, setSelectedIndex] = useState(0)
  const inputRef = useRef<HTMLInputElement>(null)
  const listRef = useRef<HTMLDivElement>(null)
  const navigate = useNavigate()

  const results = query.trim()
    ? searchIndex
        .map((entry) => ({ entry, score: fuzzyMatch(query, entry) }))
        .filter((r) => r.score > 0)
        .sort((a, b) => b.score - a.score)
        .map((r) => r.entry)
    : searchIndex.filter((e) => !e.path.includes('#'))

  useEffect(() => {
    if (open) {
      setQuery('')
      setSelectedIndex(0)
      setTimeout(() => inputRef.current?.focus(), 50)
    }
  }, [open])

  useEffect(() => {
    setSelectedIndex(0)
  }, [query])

  // Scroll selected item into view
  useEffect(() => {
    if (!listRef.current) return
    const items = listRef.current.querySelectorAll('.cmd-palette__item')
    items[selectedIndex]?.scrollIntoView({ block: 'nearest' })
  }, [selectedIndex])

  const handleNavigate = useCallback(
    (path: string) => {
      if (path.includes('#')) {
        const [route, hash] = path.split('#')
        navigate(route || '/')
        setTimeout(() => {
          document.getElementById(hash)?.scrollIntoView({ behavior: 'smooth' })
        }, 100)
      } else {
        navigate(path)
      }
      onClose()
    },
    [navigate, onClose],
  )

  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent) => {
      switch (e.key) {
        case 'ArrowDown':
          e.preventDefault()
          setSelectedIndex((i) => Math.min(i + 1, results.length - 1))
          break
        case 'ArrowUp':
          e.preventDefault()
          setSelectedIndex((i) => Math.max(i - 1, 0))
          break
        case 'Enter':
          e.preventDefault()
          if (results[selectedIndex]) {
            handleNavigate(results[selectedIndex].path)
          }
          break
        case 'Escape':
          e.preventDefault()
          onClose()
          break
      }
    },
    [results, selectedIndex, handleNavigate, onClose],
  )

  if (!open) return null

  return (
    <div className="cmd-palette__overlay" onClick={onClose}>
      <div className="cmd-palette" onClick={(e) => e.stopPropagation()} onKeyDown={handleKeyDown}>
        <div className="cmd-palette__input-wrapper">
          <svg className="cmd-palette__search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          <input
            ref={inputRef}
            className="cmd-palette__input"
            type="text"
            placeholder="Buscar na documentacao..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
          <kbd className="cmd-palette__kbd">Esc</kbd>
        </div>
        <div className="cmd-palette__results" ref={listRef}>
          {results.length === 0 ? (
            <div className="cmd-palette__empty">
              Nenhum resultado para "<strong>{query}</strong>"
            </div>
          ) : (
            results.map((entry, i) => (
              <button
                key={entry.path}
                className={`cmd-palette__item${i === selectedIndex ? ' selected' : ''}`}
                onClick={() => handleNavigate(entry.path)}
                onMouseEnter={() => setSelectedIndex(i)}
              >
                <svg className="cmd-palette__item-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                  {entry.path.includes('#') ? (
                    <><line x1="4" y1="9" x2="20" y2="9" /><line x1="4" y1="15" x2="14" y2="15" /><path d="M5 3v4" /><path d="M19 3v4" /></>
                  ) : (
                    <><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" /><polyline points="14 2 14 8 20 8" /></>
                  )}
                </svg>
                <div className="cmd-palette__item-text">
                  <span className="cmd-palette__item-title">{entry.title}</span>
                  <span className="cmd-palette__item-section">{entry.section}</span>
                </div>
                <svg className="cmd-palette__item-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <line x1="5" y1="12" x2="19" y2="12" /><polyline points="12 5 19 12 12 19" />
                </svg>
              </button>
            ))
          )}
        </div>
        <div className="cmd-palette__footer">
          <span><kbd>↑↓</kbd> navegar</span>
          <span><kbd>↵</kbd> abrir</span>
          <span><kbd>Esc</kbd> fechar</span>
        </div>
      </div>
    </div>
  )
}
