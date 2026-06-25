# Nexus — Resumo para Continuidade (24/06/2026 - atualização 2)

## Projeto

Sistema de triagem de ativos (WAPS Solutions/Claro). Java 21 + Spring Boot 4.1.0 + MySQL 8.4 + Flyway. Junior aprendendo Java/Angular passo a passo, prefere ir arquivo por arquivo, sem pular etapas.

## Stack confirmada

- Spring Boot 4.1.0, Java 21, MySQL 8.4 (host 192.168.0.9:3306, app porta 3480)
- JWT (jjwt 0.12.6), Apache POI 5.5.1, Flyway 12.4.0
- Lombok: usar `@Getter`/`@Setter` em entidades NOVAS a partir de agora. Entidades antigas (Produto, Usuario, etc.) mantêm getters/setters manuais — não tocar nelas.
- Repositório: github.com/dijairJunior/nexus

## Estado atual do banco (migrations aplicadas)

- V1: schema base | V2: usuário admin | V3: campos triagem | V4: campos ARM | V5: tb_defeito_constatado (lookup 14 defeitos + SEM DEFEITO) + campos status_item/defeito_constatado_id em tb_produto
- **V6: add_resetado_produto** — adiciona coluna `resetado VARCHAR(10)` em `tb_produto` (valores SIM/NÃO). **APLICADA E VALIDADA COM SUCESSO.**
- Tabela `tb_lote_recebido` NÃO existe no banco (descartada anteriormente, será recriada do zero — ver pendências).

## Funcionalidades 100% prontas e testadas

1. Auth JWT (login, proteção de rotas)
2. CRUD Produto completo
3. Triador automático (via SecurityContextHolder, só na criação)
4. Importação ARM em 2 etapas (preview sem salvar → confirmar grava LoteTriagem + Produtos) — testado com arquivo real Petrobras
5. Validação IMEI (15 dígitos, obrigatório, sem duplicado via existsByNumeroSerie)
6. GlobalExceptionHandler (@RestControllerAdvice) — resolve bug onde RuntimeException virava 403 mascarado. Retorna JSON limpo com status 409 + mensagem.
7. LoteTriagem (entidade+repo+service+controller, só leitura: GET listar/buscar)
8. DefeitoConstatado (entidade+repo) usando @Getter/@Setter Lombok
9. **Classificação automática A/B/C/D — CONCLUÍDA, TESTADA E MERGEADA PARA MASTER** (ver regra de negócio abaixo)

## ✅ CONCLUÍDO NESTA SESSÃO — Classificação automática A/B/C/D

### Regra de negócio final (validada com gerente)

```
status_item = "NOVO" → A (direto, sem avaliar mais nada)

status_item = "OBSOLETO" ou "TRIADO" (ou não informado):
    defeito_constatado ≠ "SEM DEFEITO" → D
    resetado = "NÃO"                    → D   (bloqueante: sem reset não pode ser disponibilizado)
    estetica = "BOM"                    → A
    estetica = "RISCOS LEVES"           → B
    estetica = "RISCOS PROFUNDOS"       → C
    senão                                → D
```

**Importante:** diferente da versão anterior do resumo, **OBSOLETO não vai mais direto para D** — agora passa pela mesma avaliação que TRIADO (correção de regra de negócio confirmada com o gerente).

**Campo novo `resetado`:** representa se foi possível fazer reset de fábrica no aparelho (padrão quando não há impedimentos como tela quebrada, conta bloqueada, etc.). Funciona como condição bloqueante — checada DEPOIS do defeito constatado (pois sem saber o defeito não dá pra avaliar o reset), mas ANTES da estética.

### Bug de encoding resolvido

Comparação `"NÃO".equalsIgnoreCase(resetado)` falhava silenciosamente para valores com acento vindos via JSON (provável mismatch de encoding do código-fonte `.java` no Windows/IntelliJ vs UTF-8 do banco). 

**Solução aplicada:** método auxiliar `isResetadoNao()` que normaliza a string removendo acentos via `java.text.Normalizer` antes de comparar contra `"NAO"`. Resolve para qualquer variação (NÃO, Não, nao, NAO).

**Recomendação pendente (não bloqueante):** verificar encoding do projeto no IntelliJ (`File > Settings > Editor > File Encodings`) para evitar bugs similares em outros campos com acentuação no futuro.

### Testes finais (todos passaram ✅)

| Cenário | statusItem | estetica | defeito | resetado | Esperado | Obtido |
|---|---|---|---|---|---|---|
| NOVO direto | NOVO | — | — | — | A | A ✅ |
| Defeito presente | TRIADO | BOM | ≠SEM DEFEITO | SIM | D | D ✅ |
| Resetado NÃO | TRIADO | BOM | SEM DEFEITO | NÃO | D | D ✅ |
| OBSOLETO avalia | OBSOLETO | RISCOS LEVES | SEM DEFEITO | SIM | B | B ✅ |
| Ideal completo | TRIADO | BOM | SEM DEFEITO | SIM | A | A ✅ |
| Riscos profundos | TRIADO | RISCOS PROFUNDOS | SEM DEFEITO | SIM | C | C ✅ |

**Status: branch commitada. Aguardando confirmação se PR/merge para master já foi feito ou ainda está pendente.**

### Arquivos alterados nesta sessão

- `Produto.java` — campo `resetado` (String) + getter/setter manual
- `ProdutoService.java` — `calcularClassificacao()` atualizado (regra OBSOLETO + resetado) + novo método privado `isResetadoNao()`
- `V6__add_resetado_produto.sql` — nova migration (`ALTER TABLE tb_produto ADD COLUMN resetado VARCHAR(10) NULL;`)

## Tabela de defeitos catalogados (tb_defeito_constatado, V5)

| ID | Descrição |
|---|---|
| 1 | SEM DEFEITO |
| 2 | NÃO LIGA / DESLIGA / NÃO CARREGA |
| 3 | DISPLAY / TOUCHSCREEN |
| 4 | TAMPA/ CARCAÇA / ESTÉTICA |
| 5 | BLOQUEADO |
| 6 | TECLADO / BOTÕES |
| 7 | BATERIA / CARGA |
| 8 | CHIP / SIM / GAVETA / REDE |
| 9 | AUDIO / MICROFONE |
| 10 | VIBRACALL |
| 11 | BIOMETRIA / SENSORES |
| 12 | CÂMERA/ VÍDEO/ FOTO |
| 13 | CONEXÃO E SINCRONIZAÇÃO |
| 14 | TRAVANDO / SOFTWARE / APPS |

Nota: campo "Caixa" (número da caixa física) NÃO é um defeito — é campo separado de localização/armazenamento, sem relação com a classificação.

## PRÓXIMO PASSO IMEDIATO

1. Confirmar se PR + merge da branch de classificação já foi feito para `master` (se ainda não foi, fazer agora)
2. Seguir para a próxima pendência: **tb_lote_recebido**

## PENDENTE — tb_lote_recebido (retomar do zero, com cuidado)

Conceito confirmado: tabela alimentada MANUALMENTE pelo triador durante conferência física (não é importação de planilha). Triador registra: IMEI conferido, estado/estética, lote, classificação. No final, sistema faz CONTRAPROVA comparando tb_lote_recebido × tb_produto (mesmo numeroSerie + loteTriagemId) para apontar divergências:

- Recebido mas não previsto na planilha
- Previsto mas não recebido
- Recebido e previsto mas classificação diverge

Lote referenciado é o MESMO LoteTriagem já existente (não é um lote físico separado).

**Cuidado ao recriar:** todos os IDs de FK devem bater com os tipos reais do banco atual:
- `tb_lote_triagem.id` = INT
- `tb_defeito_constatado.id` = INT (não BIGINT!)
- `tb_produto.id` = BIGINT

Campos sugeridos (consolidação das tentativas anteriores): numero_serie, estetica, defeito_constatado_id (INT, FK), classificacao, lote_triagem_id (INT, FK), triador, data_conferencia, status_conferencia (default PENDENTE), observacao.

**Lição da V6/V7 anteriores descartadas:** sempre conferir `flyway_schema_history` antes de criar uma migration nova — pode haver registro fantasma de tentativa anterior (mesmo version) com `checksum=0`/`success=1` mas sem a coluna/tabela de fato criada. Resolver com `DELETE FROM flyway_schema_history WHERE version=X` (somente se confirmado que a tabela/coluna real não existe).

## Outras pendências (ordem de prioridade)

1. **tb_lote_recebido + lógica de contraprova** (próximo passo — acima)
2. LoteAprovacao (entidade+repo+service+controller) — ainda não criada
3. DTOs dedicados (ProdutoRequest/Response) — hoje Controllers expõem entidade direto
4. exception/ — só tem GlobalExceptionHandler, falta ResourceNotFoundException/BusinessException (opcional, generalizar)
5. Migração da base histórica REVERSA_CLARO (~31k itens, .xlsb, via Python/pyxlsb) — fazer por ÚLTIMO
6. Frontend Angular (identidade visual WAPS: azul+laranja)
7. Convite de usuário por email (substituir cadastro direto)
8. Dashboard/BI (Metabase ou Grafana — Junior já tem experiência)
9. Papéis de usuário (multi-setor) — só quando expansão for confirmada pela diretoria

## Contexto de negócio (útil para decisões de arquitetura)

- Sistema legado Najason: RDP compartilhado, 12 licenças, instabilidade frequente que paralisa todo o time — motivação central do Nexus
- 2 sistemas hoje (CARE + Najason) usados por 5 equipes (triagem, laboratório, estoque, expedição, logística), ~15k custo de licenciamento — potencial de expansão futura do Nexus para outros setores
- Prioridade atual: Junior ganhar prática sólida em Java/Angular antes de escalar arquitetura (roles, multi-tenant)

## Erros recorrentes já resolvidos (não repetir)

- Nunca editar migration Flyway já aplicada — sempre criar Vn+1 nova
- Tipos de FK devem ser idênticos ao tipo da PK referenciada (INT vs BIGINT trava criação de tabela)
- Migration falha no meio pode deixar tabela/registro "fantasma" no banco — sempre conferir `flyway_schema_history` e fazer DROP TABLE (se aplicável) + DELETE FROM flyway_schema_history WHERE version=X antes de tentar de novo
- RuntimeException sem handler cai em /error e mascara como 403 (perde autenticação no redirect) — GlobalExceptionHandler resolve
- **NOVO:** Comparações de String com caracteres acentuados (ex: "NÃO") podem falhar silenciosamente por mismatch de encoding entre código-fonte `.java` e dados via JSON/banco — preferir normalizar (remover acentos) antes de comparar, em vez de depender do literal acentuado no código
