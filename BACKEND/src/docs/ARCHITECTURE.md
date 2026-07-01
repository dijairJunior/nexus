# Nexus — Arquitetura

> Este documento é a fonte da verdade sobre arquitetura, papéis e regras de negócio do projeto Nexus. Qualquer IA ou desenvolvedor humano novo no projeto deve ler este arquivo primeiro, antes de qualquer outro documento ou código.

## Sobre o projeto

Sistema de triagem de ativos (WAPS Solutions/Claro). Substitui o sistema legado Najason (RDP compartilhado, 12 licenças, instabilidade frequente que paralisava o time) por uma aplicação web acessível via browser padrão.

## Stack

- **Backend:** Java 21, Spring Boot 4.1.0
- **Banco:** MySQL 8.4 (host `192.168.0.9:3306`, schema `triagem_ativos`)
- **App:** porta `3480`
- **Migrations:** Flyway 12.4.0
- **Auth:** JWT (jjwt 0.12.6)
- **Import de planilhas:** Apache POI 5.5.1
- **Lombok:** `@Getter`/`@Setter` em entidades e DTOs novos. Entidades antigas (`Produto`, `Usuario`) mantêm getters/setters manuais — não alterar o padrão delas.
- **Repositório:** github.com/dijairJunior/nexus
- **Frontend (planejado):** Angular, identidade visual WAPS (azul + laranja)

## Estrutura de pastas (domain-driven)

```
src/main/java/br/com/waps/nexus/
├── config/          (SecurityConfig, CorsConfig)
├── security/        (JwtService, JwtAuthFilter, UserDetailsServiceImpl)
├── exception/        (GlobalExceptionHandler)
├── dto/              (DTOs — padrão iniciado na feature de contraprova)
├── domain/
│   ├── usuario/
│   ├── auth/
│   ├── produto/      (Produto + Repository/Service/Controller)
│   ├── importacao/   (ImportacaoService/Controller — import ARM)
│   ├── lote/
│   │   ├── triagem/    (LoteTriagem + Repository/Service/Controller)
│   │   ├── recebido/   (LoteRecebido + Repository/Service/Controller)
│   │   └── aprovacao/  (LoteAprovacao + Repository/Service/Controller)
│   └── contraprova/  (ContraprovaService/Controller)
```

## Arquitetura de camadas

**Fluxo de chamada (backend):**
```
Controller → Service → Repository → MySQL
```

**Fluxo de dados (entrada/saída da API) — modelo ideal:**
```
Request JSON → DTO → Service → Entity → Repository → MySQL
MySQL → Repository → Entity → Service → DTO → Response JSON
```

**Status atual (nem todos os módulos seguem o modelo ideal ainda):**

| Módulo | Usa DTO? | Observação |
|---|---|---|
| `Produto` | ❌ | Controller expõe Entity direto — pendência conhecida |
| `LoteRecebido` | ❌ | Controller expõe Entity direto — pendência conhecida |
| `LoteTriagem` | ❌ | Só leitura (GET), expõe Entity direto |
| `Contraprova` | ✅ | Primeiro módulo a usar DTOs dedicados (`ContraprovaItemDTO`, `ContraprovaResumoDTO`) |
| `LoteAprovacao` | ❌ | Expõe Entity direto — candidato à mesma feature de DTOs |
| `Importacao` | ✅ | Já usa DTOs (`PreviewArmResponse`, `ItemPreviewDTO`, `ConfirmarArmRequest`, `ItemConfirmadoDTO`) |

## Fluxo de negócio ponta a ponta

```
ARM (planilha, gestor importa)
        │
        ▼
   Produto (expectativa — o que deveria chegar)
        │
        ▼
   Triagem física (triador faz a conferência)
        │
        ▼
   LoteRecebido (realidade — o que de fato chegou)
        │
        ▼
   Contraprova (compara Produto × LoteRecebido)
        │
        ▼
   LoteAprovacao (gestor decide: aprova ou reprova, com histórico)
```

**Por que `Produto` e `LoteRecebido` são tabelas separadas (e não uma só com update):**
São duas "fotos" diferentes do mesmo IMEI — `Produto` é o que a ARM disse que devia chegar (expectativa, alimentada pelo gestor), `LoteRecebido` é o que a triagem física conferiu de verdade (realidade, alimentada pelo triador). Se fosse update no mesmo registro, não haveria "antes" e "depois" para a Contraprova comparar. Essa separação é intencional, não é redundância de design.

## Regras de negócio

### Produto (ARM)
- Criado via import de planilha ARM (`ImportacaoService.confirmarImportacao()`), usando `produtoRepository.saveAll()` direto — **não passa** por `ProdutoService.salvar()`.
- Por isso, produtos importados **nunca** têm classificação automática no momento do import — não é bug, é esperado, pois o `ItemConfirmadoDTO` da planilha não tem `estetica`/`defeitoConstatadoId`/`statusItem`/`resetado`.
- `statusCadastro = "Pendente"` até passar pela triagem física (edição via `ProdutoService.salvar()`).

### Classificação automática A/B/C/D
Calculada em `ProdutoService.calcularClassificacao()`, chamada dentro de `salvar()` (roda em toda gravação — criação e atualização, para recalcular se estética/defeito mudarem depois):

```
statusItem = "NOVO" → A (direto, sem avaliar mais nada)

statusItem = "OBSOLETO" ou "TRIADO" (ou não informado):
    defeito_constatado ≠ "SEM DEFEITO" → D
    resetado = "NÃO"                    → D   (bloqueante)
    estetica = "BOM"                     → A
    estetica = "RISCOS LEVES"            → B
    estetica = "RISCOS PROFUNDOS"        → C
    senão                                 → D
```
Valores válidos de estética (exatos, sem variação): `BOM`, `RISCOS LEVES`, `RISCOS PROFUNDOS`.

### LoteRecebido
- Alimentado **manualmente** pelo triador durante a conferência física (não é importação de planilha).
- Tem campos próprios (`statusItem`, `resetado`, etc.) — **não reusa** os do `Produto` original, propositalmente, para permitir a Contraprova comparar de forma independente.
- Referencia o mesmo `loteTriagemId` já existente (não é um lote físico separado).
- Reusa `ProdutoService.calcularClassificacao()` instanciando um `Produto` temporário — evita duplicar a lógica de negócio.

### Contraprova
- Cruza `tb_lote_recebido` × `tb_produto` por `numeroSerie` + `loteTriagemId`.
- 4 categorias possíveis por item:
  - `OK` — existe nos dois, classificação igual
  - `RECEBIDO_NAO_PREVISTO` — existe em `LoteRecebido` mas não em `Produto`
  - `PREVISTO_NAO_RECEBIDO` — existe em `Produto` mas não em `LoteRecebido`
  - `DIVERGENCIA_CLASSIFICACAO` — existe nos dois, classificação diferente

### LoteAprovacao
- Aprovação é do **lote inteiro** (não por IMEI individual), acontece **após** a triagem/contraprova.
- Quem aprova: **sempre o gestor** (sem validação de role/Usuario ainda — projeto não tem campo de papel na entidade `Usuario`; hoje confia em quem chama o endpoint).
- Gestor pode **aprovar OU reprovar** (motivo obrigatório na reprovação).
- Um lote pode ter **múltiplos registros de aprovação** (histórico) — ex: reprovado → corrigido → aprovado depois. Não é uma decisão única travada.

## Banco de dados — convenções e cuidados

**Tipos de FK (conferir sempre antes de criar relacionamento novo):**
- `tb_lote_triagem.id` = INT
- `tb_defeito_constatado.id` = INT (não BIGINT)
- `tb_produto.id` = BIGINT
- `tb_lote_recebido.id`, `tb_lote_aprovacao.id` = BIGINT

**Timestamps:** usar `@CreationTimestamp` (Hibernate) em vez de `insertable = false` confiando no `DEFAULT` do MySQL — `insertable = false` não retorna o valor gerado na mesma transação do `save()`, causando `null` na resposta da API.

**Encoding:** IntelliJ configurado para "Create UTF-8 files with NO BOM" (File > Settings > Editor > File Encodings). Se BOM reaparecer em arquivos `.java`, suspeitar do PowerShell 5.1 (`Out-File`, `>`, `Set-Content` sem encoding explícito) — usar `-Encoding utf8NoBOM` (PowerShell 7+) ou `UTF8Encoding(false)` no 5.1.

**Comparação de strings acentuadas:** `"NÃO".equalsIgnoreCase()` pode falhar por diferença de charset no Windows — usar helper `Normalizer`-based removendo diacríticos antes de comparar.

## Tratamento de erros

`GlobalExceptionHandler` (`@RestControllerAdvice`) — resolve o problema de `RuntimeException` sem handler cair em `/error`, que roda como anônimo e mascara o erro real como 403.

```java
@ExceptionHandler(RuntimeException.class)   // retorna 409 com JSON limpo
@ExceptionHandler(Exception.class)          // fallback genérico, retorna 500 com JSON limpo
```

**Pendência:** ainda não há `BusinessException`/`ResourceNotFoundException` dedicadas — tudo usa `RuntimeException` genérica.

## Branches (histórico)

| Branch | Status | Feature |
|---|---|---|
| `feature/importacao-arm` | Mergeada | Import ARM (preview + confirmar) |
| `feature/classificacao-automatica` | Mergeada | Cálculo A/B/C/D |
| `feature/lote-recebido` | Mergeada | Conferência física manual |
| `feature/contraprova` | Mergeada | Cruzamento Produto × LoteRecebido |
| `feature/lote-aprovacao` | Em PR (aguardando merge) | Aprovação/reprovação do lote pelo gestor |
| `main` | Branch oficial | — |

**Nota histórica:** o repositório teve `main`/`master` desalinhadas até 24/06/2026 (uma branch de feature foi criada a partir de `master` por engano). Resolvido via merge manual. `main` é a branch oficial a partir de então — sempre confirmar `git branch -a` e basear novas branches a partir de `main`.


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
Resumo da feature (.md em docs/features/)
        │
        ▼
ChatGPT faz code review e revisão arquitetural
        │
        ▼
Merge para main
```

**Regras do projeto:**
1. Apenas uma feature ativa por branch.
2. Nunca implementar a mesma feature em conversas paralelas (já causou conflito real de Flyway no passado — ver `docs/features/`).
3. Antes de mudar arquitetura, sincronizar o estado atual do projeto.
4. Toda feature termina com: testes, documentação `.md` em `docs/features/`, revisão técnica, merge.
5. Os arquivos em `docs/` são a fonte oficial de contexto para continuar o desenvolvimento — não a memória de conversas anteriores.

## Erros recorrentes (catálogo — não repetir)

- Nunca editar migration Flyway já aplicada — sempre criar `Vn+1` nova.
- Tipos de FK devem ser idênticos ao tipo da PK referenciada.
- Migration falha no meio pode deixar registro "fantasma" — sempre conferir `flyway_schema_history` antes de criar nova.
- `RuntimeException` sem handler cai em `/error` e mascara como 403 — `GlobalExceptionHandler` resolve.
- Comparações de String acentuada podem falhar por encoding — normalizar antes de comparar.
- UTF-8 BOM no início de `.java` impede o IDE/javac de reconhecer a classe.
- Campo com `insertable = false` confiando em `DEFAULT` do banco não retorna o valor gerado na mesma transação — usar `@CreationTimestamp`.
- Calcular um valor em variável local e esquecer de aplicar via setter antes do `save()` — já ocorreu múltiplas vezes no projeto.
- Anotações Java (`@GeneratedValue`, etc.) nunca terminam com `;` — só declarações de variável/método terminam.
- Erros de digitação visualmente sutis (acento, caractere especial) em `@RequestMapping`/paths de URL são difíceis de notar relendo o próprio código — conferir copiando a URL exata do erro, não só lendo a anotação.
- Tabelas/colunas "órfãs" de migrations antigas podem existir sem nenhum código Java associado — sempre conferir `DESCRIBE`/`SHOW CREATE TABLE` antes de assumir que uma tabela nova vai ser criada do zero.

## Pendências futuras (fora do escopo imediato)

- Diagramas de arquitetura/fluxo/relacionamento de entidades — postergados conscientemente para quando o Angular começar (desenhar sem frontend tende a ficar abstrato).
- Mapper dedicado (Entity↔DTO) e Bean Validation (`@Valid`/`@NotNull`) — melhorias de robustez sugeridas pelo tech lead, a avaliar como features próprias, não bloqueantes.
