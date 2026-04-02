# Claude Code — Básico

## O que é o Claude Code?

Claude Code é a CLI oficial da Anthropic para usar o Claude diretamente no terminal,
integrado ao seu código e repositório. Diferente de um chat comum, ele tem acesso
ao contexto real dos seus arquivos.

## Comandos Essenciais

```bash
# Iniciar sessão interativa
claude

# Executar um prompt direto (modo não-interativo)
claude -p "Explique este arquivo" --file src/Main.java

# Ver ajuda
claude --help
```

## Slash Commands no chat

| Comando | O que faz |
|---|---|
| `/help` | Lista todos os comandos disponíveis |
| `/clear` | Limpa o contexto da conversa |
| `/compact` | Comprime o histórico para liberar contexto |
| `/cost` | Mostra o custo da sessão atual |
| `/status` | Status da sessão e modelo em uso |
| `/memory` | Gerencia memórias persistentes |
| `/doctor` | Diagnostica problemas de configuração |
| `/login` | Autentica com a Anthropic |

## Como o Claude Code lê arquivos

- Você pode mencionar arquivos pelo nome e ele os lê automaticamente
- Ou usar `@arquivo.java` para referenciar explicitamente
- O contexto é mantido durante toda a sessão

## CLAUDE.md — O arquivo mais importante

O arquivo `CLAUDE.md` na raiz do projeto é carregado automaticamente em toda sessão.
Use-o para:
- Definir convenções do projeto
- Explicar a arquitetura
- Dar instruções persistentes ao Claude

Hierarquia de CLAUDE.md:
- `~/.claude/CLAUDE.md` — instruções globais (todas as sessões)
- `./CLAUDE.md` — instruções do projeto (carregado ao abrir o diretório)
- `./subdir/CLAUDE.md` — instruções específicas de um subdiretório

## Atalhos de Teclado

| Atalho | Ação |
|---|---|
| `Ctrl+C` | Cancela a resposta atual |
| `Ctrl+D` | Encerra a sessão |
| `↑` / `↓` | Navega no histórico de mensagens |

## Próximo: [02 — Hooks](./02-hooks.md)
