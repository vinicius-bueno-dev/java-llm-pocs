import { CodeBlock } from '../components/CodeBlock'
import { Table } from '../components/Table'
import { PageNav } from '../components/PageNav'
import { TOC } from '../components/TOC'
import type { TOCItem } from '../components/TOC'

const tocItems: TOCItem[] = [
  { id: 'what', title: 'O que é?' },
  { id: 'commands', title: 'Comandos Essenciais' },
  { id: 'slash', title: 'Slash Commands' },
  { id: 'files', title: 'Leitura de Arquivos' },
  { id: 'claudemd', title: 'CLAUDE.md' },
  { id: 'shortcuts', title: 'Atalhos' },
]

export function ClaudeCodeBasics() {
  return (
    <>
      <main className="app-content">
        <div className="section-header">
          <div className="section-header__overline">Claude Code</div>
          <h1>Básico</h1>
        </div>

        <h2 id="what">O que é?</h2>
        <p>
          CLI oficial da Anthropic para usar o Claude direto no terminal, com acesso ao contexto real dos seus arquivos.
          Diferente de um chat comum: ele lê, edita e executa no seu repositório.
        </p>

        <h2 id="commands">Comandos Essenciais</h2>
        <CodeBlock language="bash" code={`# Sessão interativa
claude

# Prompt direto (não-interativo)
claude -p "Explique este arquivo" --file src/Main.java

# Ajuda
claude --help`} />

        <h2 id="slash">Slash Commands</h2>
        <p>Comandos disponíveis dentro da sessão interativa:</p>
        <Table
          headers={['Comando', 'Descrição']}
          rows={[
            [<code>/help</code>, 'Lista comandos disponíveis'],
            [<code>/clear</code>, 'Limpa o contexto da conversa'],
            [<code>/compact</code>, 'Comprime histórico para liberar contexto'],
            [<code>/cost</code>, 'Mostra custo da sessão atual'],
            [<code>/status</code>, 'Status da sessão e modelo em uso'],
            [<code>/memory</code>, 'Gerencia memórias persistentes'],
            [<code>/doctor</code>, 'Diagnostica problemas de configuração'],
            [<code>/login</code>, 'Autentica com a Anthropic'],
          ]}
        />

        <h2 id="files">Leitura de Arquivos</h2>
        <ul>
          <li>Mencione arquivos pelo nome — Claude os lê automaticamente</li>
          <li>Use <code>@arquivo.java</code> para referência explícita</li>
          <li>O contexto persiste durante toda a sessão</li>
        </ul>

        <h2 id="claudemd">CLAUDE.md</h2>
        <p>
          Arquivo carregado automaticamente em toda sessão. Define convenções, arquitetura e instruções persistentes.
        </p>

        <h3>Hierarquia de carregamento</h3>
        <Table
          headers={['Localização', 'Escopo']}
          rows={[
            [<code>~/.claude/CLAUDE.md</code>, 'Global — todas as sessões'],
            [<code>./CLAUDE.md</code>, 'Projeto — ao abrir o diretório'],
            [<code>./subdir/CLAUDE.md</code>, 'Subdiretório — contexto específico'],
          ]}
        />

        <h2 id="shortcuts">Atalhos</h2>
        <Table
          headers={['Atalho', 'Ação']}
          rows={[
            [<code>Ctrl+C</code>, 'Cancela a resposta atual'],
            [<code>Ctrl+D</code>, 'Encerra a sessão'],
            [<code>↑ / ↓</code>, 'Navega no histórico de mensagens'],
          ]}
        />

        <PageNav currentPath="/claude-code/basics" />
      </main>
      <TOC items={tocItems} />
    </>
  )
}
