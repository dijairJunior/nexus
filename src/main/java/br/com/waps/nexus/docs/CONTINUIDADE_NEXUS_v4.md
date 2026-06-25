# Nexus — Resumo para Continuidade (24/06/2026 - atualização 4)

## Projeto

Sistema de triagem de ativos (WAPS Solutions/Claro). Java 21 + Spring Boot 4.1.0 + MySQL 8.4 + Flyway. Junior aprendendo Java/Angular passo a passo, prefere ir arquivo por arquivo, sem pular etapas.

## Stack confirmada

- Spring Boot 4.1.0, Java 21, MySQL 8.4 (host 192.168.0.9:3306, app porta 3480)
- JWT (jjwt 0.12.6), Apache POI 5.5.1, Flyway 12.4.0
- Lombok: `@Getter`/`@Setter` adotado também em DTOs novos (não só entidades) a partir desta sessão.
- Repositório: github.com/dijairJunior/nexus

## ⚠️ Situação das branches `main`/`master` — RESOLVIDA NESTA SESSÃO

O repositório tinha `main` e `master` desalinhadas (GitHub trata `main` como default via `origin/HEAD`). A branch `feature/lote-recebido` (trabalho da sessão anterior) tinha sido criada a partir de `master`, nunca mergeada.

**Ação tomada:**
```bash
git checkout main
git pull origin main
git merge lote-recebido
git push origin main
```
Merge sem conflitos. `main` agora contém o código completo do `LoteRecebido` (entidade, repo, service, controller) + migrations V7/V8. Banco já tinha V7/V8 aplicadas e registradas em `flyway_schema_history` como `success` — sem necessidade de reaplicar nada.

**Lição:** ao criar branches novas a partir de agora, sempre confirmar `git branch -a` e basear a partir de `main` (não `master`) até decidirem deprecar uma das duas definitivamente.

## ✅ CONCLUÍDO NESTA SESSÃO — feature/contraprova (em andamento, branch aberta, ainda sem PR)

Branch `feature/contraprova` criada a partir de `main` atualizada (pós-merge do `lote-recebido`).

### Objetivo da feature

Cruzar `tb_lote_recebido` (conferência física manual) × `tb_produto` (planilha ARM importada) por `numeroSerie` + `loteTriagemId`, classificando cada IMEI em 4 categorias.

### Arquivos criados

| Arquivo | Pasta |
|---|---|
| `ContraprovaItemDTO.java` | `dto/` |
| `ContraprovaResumoDTO.java` | `dto/` |
| `ContraprovaService.java` | `domain/contraprova/` |
| `ContraprovaController.java` | `domain/contraprova/` |

**Decisão de convenção:** essa foi a primeira feature a efetivamente usar a pasta `dto/` (antes vazia) — Controllers de `Produto`/`LoteRecebido` ainda expõem entidade direto (pendência separada, item 3 da lista).

### Enum `StatusContraprova` (em `ContraprovaItemDTO`)

```java
OK,
RECEBIDO_NAO_PREVISTO,       // existe em lote_recebido mas não em produto
PREVISTO_NAO_RECEBIDO,       // existe em produto mas não em lote_recebido
DIVERGENCIA_CLASSIFICACAO    // existe nos dois, classificação diferente
```

### `ContraprovaService.gerarContraprova(Integer loteTriagemId)`

- Valida lote existente via `LoteTriagemRepository.existsById()` → `RuntimeException` (intercetada pelo `GlobalExceptionHandler`, retorna 409) se não existir
- Busca `ProdutoRepository.findByLoteTriagemId()` e `LoteRecebidoRepository.findByLoteTriagemId()`
- Monta `Map<numeroSerie, Produto>` e `Map<numeroSerie, LoteRecebido>`
- Une as chaves (`LinkedHashSet`) e classifica cada IMEI nas 4 categorias
- Retorna `ContraprovaResumoDTO` com contadores + lista de itens

### Endpoint

`GET /api/contraprova/lote/{loteTriagemId}` → testado, compila e responde corretamente (estrutura JSON validada manualmente).

### Bugs corrigidos durante a implementação (revisão de código colado pelo usuário)

1. Campo `loteTriagemRepository` usado no construtor mas nunca declarado na classe
2. `existsById(loteTriagemId.longValue())` — `LoteTriagemRepository` é `JpaRepository<LoteTriagem, Integer>`, não `Long`; corrigido para `existsById(loteTriagemId)`
3. Typo no enum: `StatusContraprova.DIVERGENCIA_CLASSIFICADA` não existe (correto: `DIVERGENCIA_CLASSIFICACAO`)
4. Setter errado: `setTotalPrevistoRecebido()` não existe no DTO (correto: `setTotalPrevistoNaoRecebido()`)
5. Variável com acento (`totalPrevistoNãoRecebido`) — risco de bug de encoding, padronizado sem acento

### UTF-8 BOM — causa raiz identificada e corrigida nesta sessão

O bug recorrente de BOM em arquivos `.java` (já tinha afetado 4 arquivos antes desta correção) tinha **duas causas possíveis**: configuração do IntelliJ e/ou comportamento padrão do PowerShell 5.1 ao escrever arquivos.

**Correção aplicada no IntelliJ** (`File > Settings > Editor > File Encodings`):
- Global Encoding: UTF-8
- Project Encoding: UTF-8
- **"Create UTF-8 files": alterado para "with NO BOM"** ← campo crítico que estava causando o problema
- Confirmado visualmente pelo usuário, problema resolvido após a mudança

**Nota para o futuro:** se o BOM reaparecer, suspeitar do PowerShell 5.1 (`Out-File`, `>`, `Set-Content` sem encoding explícito adicionam BOM por padrão) — usar `-Encoding utf8NoBOM` (PowerShell 7+) ou `[System.IO.File]::WriteAllText` com `UTF8Encoding(false)` no 5.1.

## 🐛 BUG DESCOBERTO NESTA SESSÃO — pendente, NÃO resolver na branch atual

Ao tentar validar os 4 cenários da contraprova usando o lote de teste `loteTriagemId = 1`, descobriu-se que **todos os produtos desse lote têm `tb_produto.classificacao = NULL`**:

```sql
SELECT numero_serie, classificacao FROM tb_produto WHERE lote_triagem_id = 1;
-- 357799158860805 | NULL
-- 357799158838892 | NULL
```

**Hipóteses (não investigadas ainda):**
- Lote de teste antigo, importado ANTES da feature de classificação automática existir (mais provável, já que lote 1 tende a ser um dos primeiros testes)
- Bug real na importação ARM: o fluxo de import pode não estar chamando `calcularClassificacao()` / não estar persistindo o valor calculado nos produtos criados via planilha

**Decisão tomada:** não investigar nem corrigir dentro de `feature/contraprova` — está fora do escopo (é um problema de importação ARM, não de cruzamento de dados). Vira **nova feature/issue separada**.

**Quando essa nova feature for aberta, os primeiros passos devem ser:**
1. Confirmar se o problema é específico do lote 1 (teste antigo) rodando a mesma query em lotes importados mais recentemente
2. Se outros lotes também tiverem `classificacao NULL`, inspecionar o serviço de importação ARM (`importacao/`) para confirmar se `ProdutoService.calcularClassificacao()` está sendo chamado e se o resultado está sendo persistido via setter antes do `save()` — **atenção, já é o terceiro bug do tipo "calculou mas não aplicou o setter" neste projeto** (ver histórico em "Erros recorrentes")
3. Se for só dado de teste antigo, decidir: ignorar (são registros de teste, sem impacto em produção) ou rodar `UPDATE` manual de correção pontual

## Teste manual realizado nesta sessão (parcial)

```json
POST /api/lote-recebido/registrar-conferencia
{
  "numeroSerie": "111111111111111",
  "estetica": "RISCOS PROFUNDOS",
  "defeitoConstatadoId": 1,
  "statusItem": "TRIADO",
  "resetado": "SIM",
  "loteTriagemId": 1,
  "triador": "Junior",
  "observacao": "teste contraprova - divergencia"
}
```
→ Classificação calculada corretamente: `C`. Porém esse IMEI **não existe** em `tb_produto` do lote 1 — então na prática esse registro testa o cenário `RECEBIDO_NAO_PREVISTO`, não divergência como planejado originalmente.

**Cenários ainda não validados de fato:** `OK`, `DIVERGENCIA_CLASSIFICACAO` real (com produto não-NULL), `PREVISTO_NAO_RECEBIDO`.

## PRÓXIMO PASSO IMEDIATO

1. Commit do trabalho atual em `feature/contraprova` (pendente, usuário ia commitar ao sair)
2. Rodar `GET /api/contraprova/lote/1` mesmo com o dado NULL, só para confirmar que a lógica de cruzamento em si funciona (mesmo que todos os itens comparáveis caiam em `DIVERGENCIA_CLASSIFICACAO` por causa do NULL)
3. Para validar os 4 cenários de verdade, escolher uma destas opções:
   - Usar um lote diferente, importado já com classificação automática funcionando
   - Rodar `UPDATE` manual pontual nos 2 produtos do lote 1 para fins de teste local
4. Validado → commit final → abrir PR `feature/contraprova` → `main`
5. Abrir nova feature/issue separada para investigar o bug de `classificacao NULL` na importação ARM

## Outras pendências (ordem de prioridade)

1. Bug `classificacao NULL` na importação ARM (NOVO — ver seção acima)
2. LoteAprovacao (entidade+repo+service+controller) — ainda não criada
3. DTOs dedicados para `Produto`/`LoteRecebido` (Controllers hoje expõem entidade direto) — `dto/` já tem padrão iniciado pela contraprova
4. exception/ — só tem GlobalExceptionHandler, falta ResourceNotFoundException/BusinessException (opcional, generalizar)
5. Migração da base histórica REVERSA_CLARO (~31k itens, .xlsb, via Python/pyxlsb) — fazer por ÚLTIMO
6. Frontend Angular (identidade visual WAPS: azul+laranja)
7. Convite de usuário por email (substituir cadastro direto)
8. Dashboard/BI (Metabase ou Grafana)
9. Papéis de usuário (multi-setor) — só quando expansão for confirmada pela diretoria
10. Decidir definitivamente sobre duplicação `main`/`master` (deprecar uma das duas)

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
- UTF-8 BOM no início de arquivo `.java` impede o IDE/javac de reconhecer a classe — **causa raiz resolvida nesta sessão**: IntelliJ configurado para "Create UTF-8 files with NO BOM"; se reaparecer, suspeitar do PowerShell 5.1
- Campo com `insertable = false` confiando em `DEFAULT` do banco não retorna o valor gerado na mesma transação do `save()` — usar `@CreationTimestamp` (Hibernate) quando precisar do valor de volta imediatamente
- Calcular um valor em variável local e esquecer de aplicar via setter no objeto antes do `save()` — **já ocorreu 2x no projeto** (LoteRecebido.classificacao na sessão anterior; risco real de ocorrer de novo na investigação do bug de importação ARM)
- Branches `main`/`master` divergentes — resolvido via merge manual nesta sessão; `main` é a branch oficial a partir de agora
