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
2. ~~LoteAprovacao~~ — ✅ concluído, aguardando merge
3. **DTOs dedicados para `Produto`/`LoteRecebido`** ← próxima feature (branch já definida acima)
4. `BusinessException`/`ResourceNotFoundException` dedicadas — antes da migração grande, para erros mais específicos/rastreáveis
5. Avaliar (não decidido): Mapper dedicado (Entity↔DTO) e Bean Validation (`@Valid`/`@NotNull`) — melhorias sugeridas pelo tech lead, tratar como features próprias a discutir
6. Migração da base histórica REVERSA_CLARO (~31k itens, .xlsb, via Python/pyxlsb) — só depois de DTO + Exception estarem prontos
7. Frontend Angular (identidade visual WAPS: azul+laranja)
8. Convite de usuário por email (substituir cadastro direto)
9. Dashboard/BI (Metabase ou Grafana)
10. Papéis de usuário (multi-setor) — só quando expansão for confirmada pela diretoria

## Notas da sessão atual

- Refactor de pacotes concluído: `domain/lote/` dividido em `triagem/`, `recebido/`, `aprovacao/` — compilando sem erros.
- Estrutura de documentação `docs/` criada nesta sessão (este arquivo + `ARCHITECTURE.md` + `features/`), substituindo o modelo anterior de um único `CONTINUIDADE_NEXUS_vN.md` crescente.
