import { CodeBlock } from '../components/CodeBlock'
import { Table } from '../components/Table'
import { PageNav } from '../components/PageNav'
import { TOC } from '../components/TOC'
import { Breadcrumbs } from '../components/Breadcrumbs'
import { Footer } from '../components/Footer'
import type { TOCItem } from '../components/TOC'

const tocItems: TOCItem[] = [
  { id: 'what', title: 'O que e?' },
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
        <Breadcrumbs />
        <div className="section-header">
          <div className="section-header__overline">Claude Code</div>
          <h1>Basico</h1>
        </div>

        <h2 id="what" className="heading-anchor">
          <a href="#what" className="heading-anchor__link" aria-hidden="true">#</a>
          O que e?
        </h2>
        <p>
          CLI oficial da Anthropic para usar o Claude direto no terminal, com acesso ao contexto real dos seus arquivos.
          Diferente de um chat comum: ele le, edita e executa no seu repositorio.
        </p>

        <h2 id="commands" className="heading-anchor">
          <a href="#commands" className="heading-anchor__link" aria-hidden="true">#</a>
          Comandos Essenciais
        </h2>
        <CodeBlock language="bash" code={`# Sessao interativa
claude

# Prompt direto (nao-interativo)
claude -p "Explique este arquivo" --file src/Main.java

# Ajuda
claude --help`} />

        <h2 id="slash" className="heading-anchor">
          <a href="#slash" className="heading-anchor__link" aria-hidden="true">#</a>
          Slash Commands
        </h2>
        <p>Comandos disponiveis dentro da sessao interativa:</p>
        <Table
          headers={['Comando', 'Descricao']}
          rows={[
            [<code>/help</code>, 'Lista comandos disponiveis'],
            [<code>/clear</code>, 'Limpa o contexto da conversa'],
            [<code>/compact</code>, 'Comprime historico para liberar contexto'],
            [<code>/cost</code>, 'Mostra custo da sessao atual'],
            [<code>/status</code>, 'Status da sessao e modelo em uso'],
            [<code>/memory</code>, 'Gerencia memorias persistentes'],
            [<code>/doctor</code>, 'Diagnostica problemas de configuracao'],
            [<code>/login</code>, 'Autentica com a Anthropic'],
          ]}
        />

        <h2 id="files" className="heading-anchor">
          <a href="#files" className="heading-anchor__link" aria-hidden="true">#</a>
          Leitura de Arquivos
        </h2>
        <ul>
          <li>Mencione arquivos pelo nome — Claude os le automaticamente</li>
          <li>Use <code>@arquivo.java</code> para referencia explicita</li>
          <li>O contexto persiste durante toda a sessao</li>
        </ul>

        <h2 id="claudemd" className="heading-anchor">
          <a href="#claudemd" className="heading-anchor__link" aria-hidden="true">#</a>
          CLAUDE.md
        </h2>
        <p>
          Arquivo carregado automaticamente em toda sessao. Define convencoes, arquitetura e instrucoes persistentes.
        </p>

        <h3>Hierarquia de carregamento</h3>
        <Table
          headers={['Localizacao', 'Escopo']}
          rows={[
            [<code>~/.claude/CLAUDE.md</code>, 'Global — todas as sessoes'],
            [<code>./CLAUDE.md</code>, 'Projeto — ao abrir o diretorio'],
            [<code>./subdir/CLAUDE.md</code>, 'Subdiretorio — contexto especifico'],
          ]}
        />

        <h2 id="shortcuts" className="heading-anchor">
          <a href="#shortcuts" className="heading-anchor__link" aria-hidden="true">#</a>
          Atalhos
        </h2>
        <Table
          headers={['Atalho', 'Acao']}
          rows={[
            [<kbd>Ctrl+C</kbd>, 'Cancela a resposta atual'],
            [<kbd>Ctrl+D</kbd>, 'Encerra a sessao'],
            [<><kbd>↑</kbd> / <kbd>↓</kbd></>, 'Navega no historico de mensagens'],
          ]}
        />

        <PageNav currentPath="/claude-code/basics" />
        <Footer />
      </main>
      <TOC items={tocItems} />
    </>
  )
}
