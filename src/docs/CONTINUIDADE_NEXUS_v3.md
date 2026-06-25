# Nexus — Resumo para Continuidade (24/06/2026 - atualização 3)

## Projeto

Sistema de triagem de ativos (WAPS Solutions/Claro). Java 21 + Spring Boot 4.1.0 + MySQL 8.4 + Flyway. Junior aprendendo Java/Angular passo a passo, prefere ir arquivo por arquivo, sem pular etapas.

## Stack confirmada

- Spring Boot 4.1.0, Java 21, MySQL 8.4 (host 192.168.0.9:3306, app porta 3480)
- JWT (jjwt 0.12.6), Apache POI 5.5.1, Flyway 12.4.0
- Lombok: `@Getter`/`@Setter` em entidades NOVAS. Entidades antigas (Produto, Usuario) mantêm getters/setters manuais.
- Repositório: github.com/dijairJunior/nexus

## Estrutura de pastas (domain-driven)

```
src/main/java/br/com/waps/nexus/
├── config/ (SecurityConfig, CorsConfig)
├── security/ (JwtService, JwtAuthFilter, UserDetailsServiceImpl)
├── domain/
│   ├── usuario/, auth/, produto/
│   ├── lote/
│   │   ├── LoteTriagem.java (+ Repository, Service, Controller)
│   │   └── LoteRecebido.java (+ Repository, Service, Controller) ← NOVO NESTA SESSÃO
│   └── importacao/
├── dto/
└── exceptions/ (GlobalExceptionHandler)
```

## Estado atual do banco (migrations aplicadas)

V1-V5: schema base, usuário admin, campos triagem/ARM, tb_defeito_constatado.
- **V6: add_resetado_produto** — coluna `resetado` em `tb_produto`. ✅
- **V7: create_tb_lote_recebido** — cria tabela `tb_lote_recebido`. ✅ NESTA SESSÃO
- **V8: add_status_item_resetado_lote_recebido** — adiciona `status_item`/`resetado` em `tb_lote_recebido`. ✅ NESTA SESSÃO

## ✅ CONCLUÍDO NESTA SESSÃO — feature/lote-recebido (recriada do zero)

### Decisão de design importante

`tb_lote_recebido` **não reusa** `statusItem`/`resetado` do `Produto` original — tem seus **próprios campos** (`status_item`, `resetado`), informados pelo triador na conferência física. Motivo: desacoplar a conferência da planilha importada, permitindo que a contraprova futura compare os dois conjuntos de dados de forma independente.

### Schema final `tb_lote_recebido`

| Campo | Tipo | Obs |
|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | |
| numero_serie | VARCHAR(20) NOT NULL | IMEI, 15 dígitos |
| estetica | VARCHAR(30) | BOM / RISCOS LEVES / RISCOS PROFUNDOS |
| defeito_constatado_id | INT FK | → tb_defeito_constatado |
| classificacao | VARCHAR(5) | A/B/C/D, calculado |
| status_item | VARCHAR(20) | NOVO/OBSOLETO/TRIADO — campo próprio, não vem do Produto |
| resetado | VARCHAR(10) | SIM/NÃO — campo próprio, não vem do Produto |
| lote_triagem_id | INT NOT NULL FK | → tb_lote_triagem |
| triador | VARCHAR(100) | |
| data_conferencia | DATETIME | `@CreationTimestamp` — Hibernate gera, não usa DEFAULT do banco para retorno |
| status_conferencia | VARCHAR(20) | default PENDENTE |
| observacao | VARCHAR(255) | |

### Arquivos criados/alterados

- `V7__create_tb_lote_recebido.sql`
- `V8__add_status_item_resetado_lote_recebido.sql`
- `LoteRecebido.java` — entidade, `@Getter`/`@Setter` Lombok, `@CreationTimestamp` em `dataConferencia`
- `LoteRecebidoRepository.java` — `findByLoteTriagemId`, `existsByNumeroSerieAndLoteTriagemId`, `findByNumeroSerieAndLoteTriagemId`, `findByLoteTriagemIdAndStatusConferencia`
- `LoteRecebidoService.java` — `registrarConferencia()`, `listarPorLote()`, `buscarPorId()`, `calcularClassificacao()` (reusa `ProdutoService.calcularClassificacao()` via `Produto` temporário)
- `LoteRecebidoController.java` — 3 endpoints

### Endpoints (todos testados e validados no Insomnia)

- `POST /api/lote-recebido/registrar-conferencia`
- `GET /api/lote-recebido/lote/{loteTriagemId}`
- `GET /api/lote-recebido/{id}`

### Bugs encontrados e corrigidos nesta sessão

1. **UTF-8 BOM** no arquivo `LoteRecebido.java` — IDE não reconhecia a classe ("cannot resolve symbol"). Resolvido removendo o BOM.
2. **`dataConferencia` voltando `null` na resposta** — campo estava `insertable = false` (confiando só no `DEFAULT CURRENT_TIMESTAMP` do MySQL), e o Hibernate não relê o valor gerado pelo banco após o insert. **Solução:** troca para `@CreationTimestamp` (Hibernate gera o valor em memória e já retorna preenchido).
3. **`classificacao` sempre `null`** (bug mais importante) — `calcularClassificacao()` calculava o valor mas o resultado nunca era atribuído via `loteRecebido.setClassificacao(classificacao)`; ficava preso na variável local e se perdia. Corrigido adicionando a linha do setter.

### Testes finais (todos passaram ✅)

| Cenário | statusItem | estetica | defeito | resetado | Esperado | Obtido |
|---|---|---|---|---|---|---|
| NOVO direto | NOVO | — | — | — | A | A ✅ |
| Defeito presente | TRIADO | BOM | ≠SEM DEFEITO | SIM | D | D ✅ |
| Resetado NÃO (acento) | TRIADO | BOM | SEM DEFEITO | NÃO | D | D ✅ |
| OBSOLETO avalia | OBSOLETO | RISCOS LEVES | SEM DEFEITO | SIM | B | B ✅ |
| Ideal completo | TRIADO | BOM | SEM DEFEITO | SIM | A | A ✅ |
| Riscos profundos | TRIADO | RISCOS PROFUNDOS | SEM DEFEITO | SIM | C | C ✅ |
| IMEI inválido (<15 dígitos) | — | — | — | — | 409 | 409 ✅ |
| Lote inexistente | — | — | — | — | 409 | 409 ✅ |
| IMEI duplicado no lote | — | — | — | — | 409 | 409 ✅ |
| GET /lote/{id} | — | — | — | — | lista completa | ✅ |
| GET /{id} | — | — | — | — | registro único | ✅ |

**Nota:** os primeiros 8 registros de teste (IDs 1-8) ficaram com `classificacao: null` no banco — foram criados ANTES da correção do bug #3. Não é necessário corrigir, é só histórico de teste local.

**Status: pronto para commit + push + PR + merge para master.**

## PRÓXIMO PASSO IMEDIATO

1. Commit + push da branch `feature/lote-recebido`
2. Abrir PR e mergear para `master`
3. Seguir para: **lógica de contraprova** (comparar `tb_lote_recebido` × `tb_produto` por `numeroSerie` + `loteTriagemId`)
   - Recebido mas não previsto na planilha
   - Previsto mas não recebido
   - Recebido e previsto mas classificação diverge

## Outras pendências (ordem de prioridade)

1. **Lógica de contraprova** (próximo passo — acima)
2. LoteAprovacao (entidade+repo+service+controller) — ainda não criada
3. DTOs dedicados (ProdutoRequest/Response) — hoje Controllers expõem entidade direto
4. exception/ — só tem GlobalExceptionHandler, falta ResourceNotFoundException/BusinessException (opcional, generalizar)
5. Migração da base histórica REVERSA_CLARO (~31k itens, .xlsb, via Python/pyxlsb) — fazer por ÚLTIMO
6. Frontend Angular (identidade visual WAPS: azul+laranja)
7. Convite de usuário por email (substituir cadastro direto)
8. Dashboard/BI (Metabase ou Grafana)
9. Papéis de usuário (multi-setor) — só quando expansão for confirmada pela diretoria

## Contexto de negócio

- Sistema legado Najason: RDP compartilhado, 12 licenças, instabilidade frequente que paralisa todo o time — motivação central do Nexus
- 2 sistemas hoje (CARE + Najason) usados por 5 equipes, ~15k custo de licenciamento — potencial de expansão futura do Nexus
- Prioridade atual: Junior ganhar prática sólida em Java/Angular antes de escalar arquitetura

## Erros recorrentes já resolvidos (não repetir)

- Nunca editar migration Flyway já aplicada — sempre criar Vn+1 nova
- Tipos de FK devem ser idênticos ao tipo da PK referenciada (INT vs BIGINT trava criação de tabela)
- Migration falha no meio pode deixar registro "fantasma" — sempre conferir `flyway_schema_history` antes de criar nova
- RuntimeException sem handler cai em /error e mascara como 403 — GlobalExceptionHandler resolve
- Comparações de String acentuada (ex: "NÃO") podem falhar por encoding — normalizar (remover acentos) antes de comparar
- **NOVO:** UTF-8 BOM no início de arquivo `.java` impede o IDE/javac de reconhecer a classe — salvar sempre sem BOM
- **NOVO:** Campo com `insertable = false` confiando em `DEFAULT` do banco não retorna o valor gerado na mesma transação do `save()` — usar `@CreationTimestamp` (Hibernate) quando precisar do valor de volta imediatamente
- **NOVO:** Calcular um valor em variável local e esquecer de aplicar via setter no objeto é um bug fácil de passar despercebido — sempre conferir que o resultado de cálculos intermediários é efetivamente atribuído antes do `save()`
