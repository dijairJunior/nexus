# Nexus — Resumo para Continuidade (24/06/2026)

## Projeto

Sistema de triagem de ativos (WAPS Solutions/Claro). Java 21 \+ Spring Boot 4.1.0 \+ MySQL 8.4 \+ Flyway. Junior aprendendo Java/Angular passo a passo, prefere ir arquivo por arquivo, sem pular etapas.

## Stack confirmada

- Spring Boot 4.1.0, Java 21, MySQL 8.4 (host 192.168.0.9:3306, app porta 3480\)  
- JWT (jjwt 0.12.6), Apache POI 5.5.1, Flyway 12.4.0  
- Lombok: usar `@Getter`/`@Setter` em entidades NOVAS a partir de agora. Entidades antigas (Produto, Usuario, etc.) mantêm getters/setters manuais — não tocar nelas.  
- Repositório: github.com/dijairJunior/nexus (trocou de GitLab para GitHub)

## Estado atual do banco (migrations aplicadas)

- V1: schema base | V2: usuário admin | V3: campos triagem | V4: campos ARM | V5: tb\_defeito\_constatado (lookup 14 defeitos \+ SEM DEFEITO) \+ campos status\_item/defeito\_constatado\_id em tb\_produto  
- **V6 e V7 foram descartadas** (tentativas de criar tb\_lote\_recebido que falharam — confusão entre duas conversas paralelas, tipos BIGINT vs INT incompatíveis). Tabela tb\_lote\_recebido NÃO existe mais no banco. Ficou pendente p/ retomar com calma depois.

## Funcionalidades 100% prontas e testadas

1. Auth JWT (login, proteção de rotas)  
2. CRUD Produto completo  
3. Triador automático (via SecurityContextHolder, só na criação)  
4. Importação ARM em 2 etapas (preview sem salvar → confirmar grava LoteTriagem \+ Produtos) — testado com arquivo real Petrobras  
5. Validação IMEI (15 dígitos, obrigatório, sem duplicado via existsByNumeroSerie)  
6. GlobalExceptionHandler (@RestControllerAdvice) — resolve bug onde RuntimeException virava 403 mascarado (redirecionamento para /error perdia autenticação). Retorna JSON limpo com status 409 \+ mensagem.  
7. LoteTriagem (entidade+repo+service+controller, só leitura: GET listar/buscar)  
8. DefeitoConstatado (entidade+repo) usando @Getter/@Setter Lombok

## EM ANDAMENTO — Classificação automática A/B/C/D (branch local recriada, sem nome ainda definido após exclusão da "classificacao-produto" antiga)

Regra de negócio (confirmada via dropdowns reais da planilha MODELO\_TRIAGEM\_WAPS.xlsm):

status\_item \= "NOVO"     → A (direto)

status\_item \= "OBSOLETO" → D (direto)

status\_item \= "TRIADO" (ou não informado):

    defeito\_constatado ≠ "SEM DEFEITO" → D

    estetica \= "BOM"               → A

    estetica \= "RISCOS LEVES"      → B

    estetica \= "RISCOS PROFUNDOS"  → C

    senão                          → D

Valores válidos de estética: exatamente `BOM`, `RISCOS LEVES`, `RISCOS PROFUNDOS` (sem variação).

Método `calcularClassificacao(Produto produto)` já implementado no `ProdutoService`, usa `DefeitoConstatadoRepository.findById()`. Chamado dentro de `salvar()`, FORA do `if (produto.getId() == null)` — roda em toda gravação (criação e atualização), para recalcular se estética/defeito mudarem depois.

**Campos novos em Produto.java** (adicionados via V5): `statusItem` (String), `defeitoConstatadoId` (Integer) — com getters/setters manuais (entidade antiga).

### Próximo passo imediato

Acabamos de resolver um erro de infra (MySQL recusando conexão do IP `192.168.0.122` — resolvido dando GRANT ao usuário `nexus_app` para `192.168.0.%`). A aplicação deve estar rodando normal agora.

**Falta testar a classificação no Insomnia.** Passos:

1. Confirmar ID de "SEM DEFEITO": `SELECT id, descricao FROM tb_defeito_constatado WHERE descricao = 'SEM DEFEITO';` (provavelmente id=1)  
2. Login (POST /api/auth/login, admin/admin123) → pegar token  
3. Criar produto teste:

POST /api/produtos

{

  "numero": 1,

  "codigoSap": "SAP-TESTE-CLASS",

  "descricao": "Teste classificação A",

  "quantidade": 1,

  "numeroSerie": "111111111111111",

  "statusItem": "TRIADO",

  "estetica": "BOM",

  "defeitoConstatadoId": 1,

  "statusCadastro": "Pendente"

}

4. Esperado: `"classificacao": "A"` na resposta  
5. Depois testar cenários B (RISCOS LEVES), C (RISCOS PROFUNDOS), D (defeito ≠ SEM DEFEITO, ou OBSOLETO)  
6. Se tudo OK: commit da branch, merge para master

## PENDENTE — tb\_lote\_recebido (retomar do zero, com cuidado)

Conceito confirmado: tabela alimentada MANUALMENTE pelo triador durante conferência física (não é importação de planilha). Triador registra: IMEI conferido, estado/estética, lote, classificação. No final, sistema faz CONTRAPROVA comparando tb\_lote\_recebido × tb\_produto (mesmo numeroSerie \+ loteTriagemId) para apontar divergências:

- Recebido mas não previsto na planilha  
- Previsto mas não recebido  
- Recebido e previsto mas classificação diverge

Lote referenciado é o MESMO LoteTriagem já existente (não é um lote físico separado).

**Cuidado ao recriar:** todos os IDs de FK devem bater com os tipos reais do banco atual:

- `tb_lote_triagem.id` \= INT  
- `tb_defeito_constatado.id` \= INT (não BIGINT\!)  
- `tb_produto.id` \= BIGINT

Campos sugeridos (consolidação das duas tentativas anteriores): numero\_serie, estetica, defeito\_constatado\_id (INT, FK), classificacao, lote\_triagem\_id (INT, FK), triador, data\_conferencia, status\_conferencia (default PENDENTE), observacao.

## Outras pendências (ordem de prioridade)

1. tb\_lote\_recebido \+ lógica de contraprova (acima)  
2. LoteAprovacao (entidade+repo+service+controller) — ainda não criada  
3. DTOs dedicados (ProdutoRequest/Response) — hoje Controllers expõem entidade direto  
4. exception/ — só tem GlobalExceptionHandler, falta ResourceNotFoundException/BusinessException (opcional, generalizar)  
5. Migração da base histórica REVERSA\_CLARO (\~31k itens, .xlsb, via Python/pyxlsb) — fazer por ÚLTIMO  
6. Frontend Angular (identidade visual WAPS: azul+laranja)  
7. Convite de usuário por email (substituir cadastro direto)  
8. Dashboard/BI (Metabase ou Grafana — Junior já tem experiência)  
9. Papéis de usuário (multi-setor) — só quando expansão for confirmada pela diretoria

## Contexto de negócio (útil para decisões de arquitetura)

- Sistema legado Najason: RDP compartilhado, 12 licenças, instabilidade frequente que paralisa todo o time — motivação central do Nexus  
- 2 sistemas hoje (CARE \+ Najason) usados por 5 equipes (triagem, laboratório, estoque, expedição, logística), \~15k custo de licenciamento — potencial de expansão futura do Nexus para outros setores  
- Prioridade atual: Junior ganhar prática sólida em Java/Angular antes de escalar arquitetura (roles, multi-tenant)

## Erros recorrentes já resolvidos (não repetir)

- Nunca editar migration Flyway já aplicada — sempre criar Vn+1 nova  
- Tipos de FK devem ser idênticos ao tipo da PK referenciada (INT vs BIGINT trava criação de tabela)  
- Migration falha no meio pode deixar tabela "fantasma" no banco — sempre fazer DROP TABLE \+ DELETE FROM flyway\_schema\_history WHERE version=X antes de tentar de novo  
- RuntimeException sem handler cai em /error e mascara como 403 (perde autenticação no redirect) — GlobalExceptionHandler resolve

