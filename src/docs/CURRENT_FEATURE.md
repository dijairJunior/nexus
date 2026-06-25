# Nexus — Estado Atual (CURRENT_FEATURE)

> Este arquivo é sobrescrito a cada sessão de trabalho. Reflete o que está em andamento **agora**. Para histórico de features já concluídas, ver `docs/features/`. Para arquitetura/regras/papéis fixos, ver `docs/ARCHITECTURE.md`.

**Última atualização:** 25/06/2026

## Feature em andamento

`feature/lote-aprovacao` — **concluída, em PR aguardando merge para `main`.**

## Próximo passo imediato

1. Commit final em `feature/lote-aprovacao` (mensagem de erro já corrigida: "obrigatório para reprovação")
2. Push + abrir PR `feature/lote-aprovacao` → `main`
3. Code review do tech lead (ChatGPT)
4. Merge
5. Deletar branch local e remota após merge

## Próxima feature planejada

```bash
git checkout main
git pull origin main
git checkout -b feature/dtos-produto-lote-recebido
```

**Objetivo:** criar DTOs dedicados para `Produto` e `LoteRecebido` (Request/Response), já que os Controllers hoje expõem a Entity JPA direto. O padrão de pasta `dto/` já foi iniciado na feature de contraprova (`ContraprovaItemDTO`, `ContraprovaResumoDTO`).

**Status:** aguardando revisão de arquitetura do tech lead antes de iniciar (ver Regra 1 do fluxo de trabalho em `ARCHITECTURE.md`).

## Pendências (ordem de prioridade atual)

1. ~~Bug classificacao NULL~~ — ✅ esclarecido, não é bug (ver `docs/features/feature-contraprova.md`)
2. ~~LoteAprovacao~~ — ✅ concluído, aguardando merge (não será reaberta — itens abaixo relacionados são complementos futuros, em features próprias)
3. **DTOs dedicados para `Produto`/`LoteRecebido`** ← próxima feature (branch já definida acima) — **aprovado pelo tech lead como prioridade 1**
4. `BusinessException`/`ResourceNotFoundException` dedicadas — **aprovado como prioridade 2**, antes do relatório
5. **Relatório compilado de lotes (Excel)** — **aprovado como prioridade 3**, com ajustes do tech lead (ver detalhes abaixo)
6. Migração da base histórica REVERSA_CLARO (~31k itens, .xlsb, via Python/pyxlsb) — prioridade 4
7. **Complemento futuro (última prioridade de backend) — Regra de bloqueio em `LoteAprovacao`:** impedir `APROVADO` enquanto existirem itens `PREVISTO_NAO_RECEBIDO`/`RECEBIDO_NAO_PREVISTO` não resolvidos na Contraprova do lote. Motivação: evitar cenário do sistema legado (189 produtos sem origem/quantidade rastreável). **Não é alteração da feature já concluída** — feature nova própria, ainda não detalhada.
8. Avaliar (não decidido): Mapper dedicado (Entity↔DTO) e Bean Validation (`@Valid`/`@NotNull`) — melhorias sugeridas pelo tech lead, tratar como features próprias a discutir, sem prioridade definida
9. Frontend Angular (identidade visual WAPS: azul+laranja)
10. Convite de usuário por email (substituir cadastro direto)
11. Dashboard/BI (Metabase ou Grafana)
12. Papéis de usuário (multi-setor) — só quando expansão for confirmada pela diretoria

## Notas da sessão atual

- Refactor de pacotes concluído: `domain/lote/` dividido em `triagem/`, `recebido/`, `aprovacao/` — compilando sem erros.
- Estrutura de documentação `docs/` criada nesta sessão (este arquivo + `ARCHITECTURE.md` + `features/`), substituindo o modelo anterior de um único `CONTINUIDADE_NEXUS_vN.md` crescente.

## 🆕 Feature em definição — Relatório compilado de lotes (Excel)

**Status:** Escopo aprovado pelo tech lead (ChatGPT), com ajustes. Prioridade 3 (depois de DTOs e Exceptions). Ainda não iniciada.

### Contexto / motivação

Discussão sobre rastreabilidade de dados, motivada por um problema observado no sistema legado (fora do Nexus): ~189 produtos sem origem/quantidade rastreável. O usuário quer um relatório que ajude a visualizar e auditar os dados de um lote de forma compilada, como mecanismo de prevenção desse tipo de problema dentro do Nexus.

Durante a discussão, também ficou claro que **a Contraprova já resolve "re-contraprova"** nativamente: ela é calculada on-demand (não armazena snapshot), então qualquer correção em `Produto` (via `PUT /api/produtos/{id}`, endpoint já existente) se reflete automaticamente na próxima chamada do `GET /api/contraprova/lote/{id}`. Não é necessária nenhuma feature nova para isso.

### Escopo aprovado

- **Formato:** Excel (.xlsx), via Apache POI (já é dependência do projeto)
- **Conteúdo:** tudo — Produto + LoteRecebido + Contraprova + status de LoteAprovacao
- **Seleção de seções:** parâmetros opcionais na URL, cada seção marcada vira uma aba separada no `.xlsx`
- **Escopo inicial:** só "por lote" (não fazer "todos os lotes consolidado" nesta primeira versão) — tech lead concordou, mais seguro e mais fácil de testar
- **Pacote:** `domain/relatorio/` com `RelatorioLoteController.java` + `RelatorioLoteService.java` (ajuste do tech lead)
- **Sem DTO próprio do relatório por agora** (ajuste do tech lead) — primeiro fazer os DTOs gerais de `Produto`/`LoteRecebido`, depois o `RelatorioLoteService` consome os Services já existentes (Produto, LoteRecebido, Contraprova, LoteAprovacao)

### Endpoint aprovado

```
GET /api/relatorio/lote/{loteTriagemId}/excel?produto=true&loteRecebido=true&contraprova=true&aprovacao=true
```

Cada parâmetro `true` gera uma aba no Excel: "Produto", "LoteRecebido", "Contraprova", "Aprovacao".

### Ordem de dependência confirmada

Esta feature depende de `feature/dtos-produto-lote-recebido` estar concluída primeiro (para o `RelatorioLoteService` já consumir Services desacoplados de Entity).

## 🆕 Complemento futuro registrado (última prioridade do backend) — Bloqueio de aprovação com divergências não resolvidas

**Importante:** isso NÃO é uma reabertura da feature `LoteAprovacao` (já concluída e em PR). É uma feature complementar separada, planejada para ser feita por último entre as pendências de backend.

Discutido nesta sessão: impedir que `LoteAprovacao` registre `APROVADO` enquanto existirem itens `PREVISTO_NAO_RECEBIDO`/`RECEBIDO_NAO_PREVISTO` não resolvidos na Contraprova do lote. Motivação: mesmo contexto do relatório acima (evitar itens órfãos/sem rastreabilidade, como ocorreu no sistema legado). Ainda não detalhada — só registrada como intenção futura.
