import { useState, useEffect, useCallback } from 'react'
import { Routes, Route, useLocation } from 'react-router-dom'
import { useTheme } from './hooks/useTheme'
import { Header } from './components/Header'
import { Sidebar } from './components/Sidebar'
import { CommandPalette } from './components/CommandPalette'
import { ScrollProgress } from './components/ScrollProgress'
import { BackToTop } from './components/BackToTop'
import { Home } from './pages/Home'
import { ClaudeCodeBasics } from './pages/ClaudeCodeBasics'
import { LocalStackConcepts } from './pages/LocalStackConcepts'
import { TerraformLocalStack } from './pages/TerraformLocalStack'

export function App() {
  const { theme, toggle } = useTheme()
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [paletteOpen, setPaletteOpen] = useState(false)
  const location = useLocation()

  useEffect(() => {
    window.scrollTo(0, 0)
    setSidebarOpen(false)
  }, [location.pathname])

  // Global Ctrl+K shortcut
  const handleGlobalKeyDown = useCallback(
    (e: KeyboardEvent) => {
      if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
        e.preventDefault()
        setPaletteOpen((prev) => !prev)
      }
    },
    [],
  )

  useEffect(() => {
    window.addEventListener('keydown', handleGlobalKeyDown)
    return () => window.removeEventListener('keydown', handleGlobalKeyDown)
  }, [handleGlobalKeyDown])

  return (
    <div className="app-layout">
      <Header
        theme={theme}
        onToggleTheme={toggle}
        onToggleMenu={() => setSidebarOpen(!sidebarOpen)}
        onOpenSearch={() => setPaletteOpen(true)}
      />
      <ScrollProgress />
      <Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      <Routes>
        <Route path="/" element={<Home onOpenSearch={() => setPaletteOpen(true)} />} />
        <Route path="/claude-code/basics" element={<ClaudeCodeBasics />} />
        <Route path="/localstack/concepts" element={<LocalStackConcepts />} />
        <Route path="/localstack/terraform" element={<TerraformLocalStack />} />
      </Routes>
      <CommandPalette open={paletteOpen} onClose={() => setPaletteOpen(false)} />
      <BackToTop />
    </div>
  )
}
