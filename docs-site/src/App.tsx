import { useState, useEffect } from 'react'
import { Routes, Route, useLocation } from 'react-router-dom'
import { useTheme } from './hooks/useTheme'
import { Header } from './components/Header'
import { Sidebar } from './components/Sidebar'
import { Home } from './pages/Home'
import { ClaudeCodeBasics } from './pages/ClaudeCodeBasics'
import { LocalStackConcepts } from './pages/LocalStackConcepts'
import { TerraformLocalStack } from './pages/TerraformLocalStack'

export function App() {
  const { theme, toggle } = useTheme()
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const location = useLocation()

  useEffect(() => {
    window.scrollTo(0, 0)
    setSidebarOpen(false)
  }, [location.pathname])

  return (
    <div className="app-layout">
      <Header
        theme={theme}
        onToggleTheme={toggle}
        onToggleMenu={() => setSidebarOpen(!sidebarOpen)}
      />
      <Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/claude-code/basics" element={<ClaudeCodeBasics />} />
        <Route path="/localstack/concepts" element={<LocalStackConcepts />} />
        <Route path="/localstack/terraform" element={<TerraformLocalStack />} />
      </Routes>
    </div>
  )
}
