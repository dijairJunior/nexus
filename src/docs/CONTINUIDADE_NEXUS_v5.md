# Nexus — Resumo para Continuidade (25/06/2026 - atualização 5)

## Projeto

Sistema de triagem de ativos (WAPS Solutions/Claro). Java 21 + Spring Boot 4.1.0 + MySQL 8.4 + Flyway. Junior aprendendo Java/Angular passo a passo, prefere ir arquivo por arquivo, sem pular etapas.

## Stack confirmada

- Spring Boot 4.1.0, Java 21, MySQL 8.4 (host 192.168.0.9:3306, app porta 3480)
- JWT (jjwt 0.12.6), Apache POI 5.5.1, Flyway 12.4.0
- Lombok: `@Getter`/`@Setter` adotado em entidades e DTOs novos
- Repositório: github.com/dijairJunior/nexus

## ⚠️ Refactor de pastas (nesta sessão)

`domain/lote/` foi dividido em subpacotes:
```
domain/lote/
├── triagem/   (LoteTriagem + Repository/Service/Controller)
├── recebido/  (LoteRecebido + Repository/Service/Controller)
└── aprovacao/ (LoteAprovacao + Repository/Service/Controller — NOVO)
```
Feito via Refactor → Move do IntelliJ, sem quebrar imports. Confirmado compilando.

## ✅ ITEM RESOLVIDO — Bug "classificacao NULL" na importação ARM (NÃO é bug)

Investigação a partir da branch `feature/contraprova` (sessão anterior). Causa raiz identificada lendo o código real de `ImportacaoService`:

- `confirmarImportacao()` usa `produtoRepository.saveAll(produtos)` direto — **não passa** por `ProdutoService.salvar()`, que é onde `calcularClassificacao()` roda.
- Isso é esperado: o `ItemConfirmadoDTO` da planilha ARM não tem `estetica`/`defeitoConstatadoId`/`statusItem`/`resetado` — só dados de identificação (numeroSerie, modelo, NF, centro de distribuição). Sem esses campos não haveria como calcular classificação de qualquer forma.
- `statusCadastro = "Pendente"` é o sinal de que o produto foi importado mas ainda não passou pela triagem física.

**Conclusão:** classificação só existe depois que alguém atualiza o produto via `ProdutoService.salvar()` (rota de edição/triagem). Lote 1 nunca passou por isso — é dado de teste antigo. **Não é bug, não precisa correção de código.** ⚠️ Resta como alerta documentado: import ARM não classifica produtos automaticamente, por design.

## 🧠 Modelo mental consolidado (clarificação de fluxo, importante registrar)

Existem duas "fotos" do mesmo IMEI, propositalmente em tabelas separadas:

| Tabela | Quem alimenta | Representa |
|---|---|---|
| `Produto` | Gestor (import ARM) | O que **deveria** chegar (expectativa) |
| `LoteRecebido` | Triagem (conferência física) | O que **realmente** chegou (realidade) |

A Contraprova compara os dois por `numeroSerie` + `loteTriagemId`. Não é redundância de design — é o que viabiliza a comparação (se fosse update no mesmo registro, não haveria "antes" x "depois").

## ✅ CONCLUÍDO NESTA SESSÃO — feature/lote-aprovacao

### Conceito confirmado com o usuário

- Aprovação é do **lote inteiro** (não por IMEI individual), acontece **após** a triagem/contraprova.
- Quem aprova: **sempre o gestor** (sem validação de role/Usuario por enquanto — projeto ainda não tem campo de papel na entidade `Usuario`; ficou registrado como confiança no chamador do endpoint por agora).
- Gestor pode **aprovar OU reprovar** (com motivo obrigatório na reprovação).
- Um lote pode ter **múltiplos registros de aprovação** (histórico) — ex: reprovado → corrigido → aprovado depois. Não é uma decisão única travada.

### Arquivos criados (`domain/lote/aprovacao/`)

| Arquivo | Função |
|---|---|
| `StatusAprovacao.java` | enum `APROVADO`, `REPROVADO` |
| `LoteAprovacao.java` | entidade (`@CreationTimestamp` em `dataAprovacao`) |
| `LoteAprovacaoRepository.java` | `findByLoteTriagemId()` |
| `LoteAprovacaoService.java` | `registrarDecisao()` + `listarPorLote()` |
| `LoteAprovacaoController.java` | `POST /api/lote-aprovacao` + `GET /api/lote-aprovacao/lote/{id}` |

### Migration V9 — `create_tb_lote_aprovacao.sql`

```sql
CREATE TABLE tb_lote_aprovacao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_triagem_id INT NOT NULL,
    gestor VARCHAR(100) NOT NULL,
    status_aprovacao VARCHAR(20) NOT NULL,
    motivo VARCHAR(500),
    data_aprovacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lote_aprovacao_lote_triagem
        FOREIGN KEY (lote_triagem_id) REFERENCES tb_lote_triagem(id)
);
```

### Endpoints testados e validados

| Cenário | Request | Resultado |
|---|---|---|
| Aprovar (sem motivo) | `POST` statusAprovacao=APROVADO | 200 ✅ |
| Reprovar com motivo | `POST` statusAprovacao=REPROVADO + motivo | 200 ✅ |
| Reprovar sem motivo | `POST` statusAprovacao=REPROVADO sem motivo | 409 "Motivo é obrigatório para reprovação" ✅ |
| statusAprovacao null | `POST` statusAprovacao=null | 409 (erro de constraint do banco — teste intencional, não há validação de campo obrigatório no Service) |
| Histórico | `GET /api/lote-aprovacao/lote/1` | retorna lista com as 3 decisões em ordem cronológica ✅ |

## 🐛 Bugs encontrados e corrigidos nesta sessão

1. **Tabela órfã `tb_lote_aprovacao` do schema V1** — a tabela já existia desde a migration V1 (`create_schema.sql`), criada como esqueleto simétrico a `tb_lote_triagem`, nunca implementada em código Java (sem Service/Controller/Repository). Tinha schema antigo (`numero`, `descricao`, `data_aprov`, `criado_em`) e uma FK em `tb_produto.lote_aprovacao_id`, ambos nunca usados (confirmado: 0 registros, 0 produtos vinculados).
   - **Sintoma:** `DROP TABLE` falhou por FK; depois `flyway_schema_history` mostrou V9 com `success=1` mas `checksum=0` e schema antigo intacto (Flyway "achou" que rodou mas não aplicou nada — causa exata não 100% explicada, não bloqueante).
   - **Correção:** `ALTER TABLE tb_produto DROP FOREIGN KEY` + `DROP COLUMN lote_aprovacao_id` + `DROP TABLE tb_lote_aprovacao` + `DELETE FROM flyway_schema_history WHERE version='9'` + restart para a V9 real rodar.
   - **Efeito colateral:** campo `loteAprovacaoId` órfão também existia em `Produto.java` (entidade antiga, nunca usado em lógica) — comentado (não excluído, por decisão do usuário) para resolver erro de schema validation do Hibernate.

2. **`;` (ponto e vírgula) sobrando após anotação `@GeneratedValue(...)`** em `LoteAprovacao.java` — causava `illegal start of type`. Anotações Java nunca terminam com `;`.

3. **Arquivo `StatusAprovacao.java` esquecido** (só foi referenciado no `LoteAprovacao`, nunca criado de fato) — causava `cannot find symbol: class StatusAprovacao`.

4. **Erro de digitação `ç` em vez de `c`** em `@RequestMapping("/api/lote-aprovação")` — visualmente quase idêntico a `/api/lote-aprovacao`, causou `NoResourceFoundException` (404 disfarçado de 500) porque a URL chamada no Insomnia não tinha acento. **Erro fácil de não notar mesmo relendo várias vezes** — não é falta de atenção, é como o olho humano processa texto familiar.

5. **`GlobalExceptionHandler` sem captura genérica de `Exception`** — só tinha handler pra `RuntimeException`, então qualquer outro tipo de exceção (ex: erro de desserialização JSON) escapava para o redirect padrão `/error`, que roda como anônimo e retorna 403 em vez do erro real. **Corrigido** adicionando handler genérico:
   ```java
   @ExceptionHandler(Exception.class)
   public ResponseEntity<Map<String, Object>> tratarException(Exception ex) {
       Map<String, Object> corpo = new HashMap<>();
       corpo.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
       corpo.put("mensagem", ex.getMessage());
       corpo.put("timestamp", LocalDateTime.now());
       return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(corpo);
   }
   ```

6. **Mensagem de erro trocada** — `Service` lançava `"Motivo é obrigatório para aprovação"` no cenário de **reprovação** sem motivo (mensagem logicamente invertida). Corrigido para `"...para reprovação"`.

## 🏗️ Arquitetura (seção de referência rápida — sugerida pelo tech lead/ChatGPT)

**Fluxo de camadas (back-end):**
```
Controller → Service → Repository → MySQL
```

**Fluxo de dados (entrada/saída da API):**
```
Request JSON → DTO → Service → Entity → Repository → MySQL
MySQL → Repository → Entity → Service → DTO → Response JSON
```

**Status atual:** `Produto` e `LoteRecebido` ainda expõem a Entity direto no Controller (sem DTO intermediário) — é a próxima feature planejada (ver Pendências). `Contraprova` e `LoteAprovacao` já seguem o fluxo completo com DTO/Entity separados (Contraprova) ou usam a Entity diretamente por simplicidade (LoteAprovacao, pendente de DTO também).

**Fluxo de negócio ponta a ponta:**
```
ARM (planilha) → Produto (expectativa)
                      ↓
            Triagem física → LoteRecebido (realidade)
                      ↓
                Contraprova (compara os dois)
                      ↓
                LoteAprovacao (decisão do gestor, com histórico)
```

## 🌳 Branches implementadas (histórico)

| Branch | Status | Feature |
|---|---|---|
| `feature/importacao-arm` | Mergeada | Import ARM (preview + confirmar) |
| `feature/classificacao-automatica` | Mergeada | Cálculo A/B/C/D |
| `feature/lote-recebido` | Mergeada | Conferência física manual |
| `feature/contraprova` | Mergeada | Cruzamento Produto × LoteRecebido |
| `feature/lote-aprovacao` | Em PR (aguardando merge) | Aprovação/reprovação do lote pelo gestor |
| `main` | Branch oficial | — |

## 📋 Regras de Negócio (consolidado — referência rápida)

**Produto (ARM):**
- Criado via import da planilha ARM, sem classificação inicial.
- `statusCadastro = "Pendente"` até passar pela triagem física.
- Classificação A/B/C/D só é calculada quando o produto é atualizado via `ProdutoService.salvar()` (rota de edição/triagem) — nunca no import.

**Classificação A/B/C/D:**
```
statusItem = "NOVO" → A (direto)
statusItem = "TRIADO" ou "OBSOLETO" (ou não informado):
    defeito ≠ "SEM DEFEITO" → D
    resetado = "NÃO"        → D (bloqueante)
    estetica = "BOM"               → A
    estetica = "RISCOS LEVES"      → B
    estetica = "RISCOS PROFUNDOS"  → C
    senão                          → D
```

**LoteRecebido:**
- Alimentado manualmente pelo triador durante conferência física.
- Não reusa campos do `Produto` original — campos próprios e independentes, propositalmente.

**Contraprova:**
- Compara `Produto` × `LoteRecebido` por `numeroSerie` + `loteTriagemId`.
- 4 categorias: `OK`, `RECEBIDO_NAO_PREVISTO`, `PREVISTO_NAO_RECEBIDO`, `DIVERGENCIA_CLASSIFICACAO`.

**LoteAprovacao:**
- Aprovação é do lote inteiro (não por IMEI).
- Sempre o gestor decide (sem validação de role ainda).
- Pode aprovar ou reprovar; motivo obrigatório na reprovação.
- Permite histórico — múltiplos registros por lote (reprovado → corrigido → aprovado depois).

## 👥 Papéis no desenvolvimento do Nexus

**Junior — Product Owner + Desenvolvedor**
Responsável pelo produto: entende a necessidade do negócio, decide o que entra ou não no sistema, implementa e testa o código, faz os commits e aprova as mudanças. **É o único que altera a branch.** Nenhuma alteração entra no projeto sem sua validação.

**ChatGPT — Tech Lead / Arquiteto**
Responsabilidades: definir arquitetura, revisar decisões de longo prazo, revisar código (code review), garantir consistência entre módulos, definir padrões do projeto, priorizar backlog técnico, explicar conceitos e justificar decisões.
Consultar principalmente: antes de iniciar uma feature, após concluir uma feature, em dúvidas arquiteturais, para revisão do PR antes do merge.

**Claude — Pair Programmer / Especialista em Implementação**
Responsabilidades: implementação arquivo por arquivo, debug em tempo real, investigação de erros, refatorações pontuais, ajustes de compilação, correções rápidas durante o desenvolvimento.
Consultar principalmente: durante a implementação, durante testes, em erros de compilação/execução.

**Fluxo de trabalho:**
```
Definição da feature
        │
        ▼
ChatGPT revisa arquitetura
        │
        ▼
Junior aprova a abordagem
        │
        ▼
Claude implementa junto com Junior
        │
        ▼
Testes locais
        │
        ▼
Resumo da feature (.md)
        │
        ▼
ChatGPT faz code review e revisão arquitetural
        │
        ▼
Merge para main
```

**Regras do projeto:**
1. Apenas uma feature ativa por branch.
2. Nunca implementar a mesma feature em conversas paralelas.
3. Antes de mudar arquitetura, sincronizar o estado atual do projeto.
4. Toda feature termina com: testes, documentação `.md`, revisão técnica, merge.
5. O `.md` da feature é a fonte oficial de contexto para continuar o desenvolvimento.

## PRÓXIMO PASSO IMEDIATO

1. Commit final em `feature/lote-aprovacao` (mensagem corrigida)
2. Push + abrir PR `feature/lote-aprovacao` → `main`
3. Merge
4. Deletar branch local e remota após merge

## 🌱 Nova branch sugerida para próxima implementação

```bash
git checkout main
git pull origin main
git checkout -b feature/dtos-produto-lote-recebido
```

**Objetivo da próxima feature:** criar DTOs dedicados para `Produto` e `LoteRecebido` (Request/Response), já que os Controllers hoje expõem a entidade JPA direto. O padrão de pasta `dto/` já foi iniciado na feature de contraprova (`ContraprovaItemDTO`, `ContraprovaResumoDTO`) — esta feature estende o mesmo padrão para os dois Controllers mais antigos do projeto.

## Outras pendências (ordem de prioridade — reordenada com input do tech lead/ChatGPT)

1. ~~Bug classificacao NULL~~ — ✅ esclarecido, não é bug
2. ~~LoteAprovacao~~ — ✅ concluído nesta sessão
3. **DTOs dedicados para `Produto`/`LoteRecebido`** ← próxima feature (branch já criada abaixo)
4. **`BusinessException`/`ResourceNotFoundException`** dedicadas (hoje só `RuntimeException` genérica + handler para `Exception`) — antes da migração grande, pra ter erros mais específicos/rastreáveis
5. **Avaliar (não decidido ainda):** Mapper dedicado (Entity↔DTO) e Bean Validation (`@Valid`/`@NotNull` etc. nos DTOs) — são melhorias de robustez sugeridas pelo tech lead, tratar como features próprias a discutir, não bloqueantes para seguir
6. Migração da base histórica REVERSA_CLARO (~31k itens, .xlsb, via Python/pyxlsb) — só depois de DTO + Exception estarem prontos, pela escala do volume
7. Frontend Angular (identidade visual WAPS: azul+laranja)
8. Convite de usuário por email (substituir cadastro direto)
9. Dashboard/BI (Metabase ou Grafana)
10. Papéis de usuário (multi-setor) — só quando expansão for confirmada pela diretoria
11. Decidir definitivamente sobre duplicação `main`/`master` (já resolvido — manter `main` como oficial)

**Nota:** diagramas de arquitetura/fluxo/entidades (sugeridos pelo tech lead) foram conscientemente postergados para quando o Angular começar — desenhar agora, sem frontend, tende a ficar abstrato.

## Contexto de negócio

- Sistema legado Najason: RDP compartilhado, 12 licenças, instabilidade frequente que paralisa todo o time — motivação central do Nexus
- 2 sistemas hoje (CARE + Najason) usados por 5 equipes — potencial de expansão futura do Nexus
- Prioridade atual: Junior ganhar prática sólida em Java/Angular antes de escalar arquitetura

## Erros recorrentes já resolvidos (não repetir — lista consolidada)

- Nunca editar migration Flyway já aplicada — sempre criar Vn+1 nova
- Tipos de FK devem ser idênticos ao tipo da PK referenciada (INT vs BIGINT trava criação de tabela)
- Migration falha no meio pode deixar registro "fantasma" — sempre conferir `flyway_schema_history` antes de criar nova
- RuntimeException sem handler cai em /error e mascara como 403 — GlobalExceptionHandler resolve (agora com handler genérico também para `Exception`)
- Comparações de String acentuada (ex: "NÃO") podem falhar por encoding — normalizar (remover acentos) antes de comparar
- UTF-8 BOM no início de arquivo `.java` impede o IDE/javac de reconhecer a classe — IntelliJ configurado para "Create UTF-8 files with NO BOM"
- Campo com `insertable = false` confiando em `DEFAULT` do banco não retorna o valor gerado na mesma transação do `save()` — usar `@CreationTimestamp` (Hibernate)
- Calcular um valor em variável local e esquecer de aplicar via setter no objeto antes do `save()` — já ocorreu 2x no projeto
- Branches `main`/`master` divergentes — resolvido via merge manual; `main` é a branch oficial
- **NOVO:** Anotações Java (`@GeneratedValue`, etc.) nunca terminam com `;` — só declarações de variável/método
- **NOVO:** Erros de digitação visualmente sutis (acento, caractere especial) em `@RequestMapping`/paths de URL são difíceis de notar relendo o próprio código — conferir copiando a URL exata do erro, não só lendo a anotação
- **NOVO:** Tabelas/colunas "órfãs" de migrations antigas (V1) podem existir sem nenhum código Java associado — sempre verificar `DESCRIBE`/`SHOW CREATE TABLE` antes de assumir que uma tabela nova vai ser criada do zero
