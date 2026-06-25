# Nexus

Sistema interno de gestão e triagem de ativos retornados (reversa logística) da WAPS Solutions, desenvolvido para o time responsável pelo processamento de equipamentos de telecomunicações devolvidos por clientes via Claro.

## Sobre o projeto

O Nexus centraliza o fluxo de recebimento, triagem, classificação, conferência física e aprovação de equipamentos (celulares e demais ativos) devolvidos por clientes corporativos. Substitui o controle manual feito anteriormente em planilhas Excel e o sistema legado Najason (acesso via RDP compartilhado, instável), trazendo rastreabilidade, autenticação de usuários e regras de negócio automatizadas.

## Stack tecnológica

| Camada | Tecnologia |
|---|---|
| Backend | Java 21 + Spring Boot 4.1.0 |
| Frontend | Angular (planejado) |
| Banco de dados | MySQL 8.4 |
| Migrations | Flyway 12.4.0 |
| Autenticação | Spring Security + JWT (jjwt 0.12.6) |
| Leitura de planilhas | Apache POI 5.5.1 |
| Build | Maven |

## Arquitetura

Estrutura atual do projeto (pacotes e arquivos efetivamente implementados):

```
src/main/java/br/com/waps/nexus/
│
├── config/
│   ├── SecurityConfig.java
│   └── CorsConfig.java
│
├── security/
│   ├── JwtService.java
│   ├── JwtAuthFilter.java
│   └── UserDetailsServiceImpl.java
│
├── exception/
│   └── GlobalExceptionHandler.java
│
├── dto/
│   ├── ContraprovaItemDTO.java
│   └── ContraprovaResumoDTO.java
│
├── domain/
│   ├── usuario/
│   │   ├── Usuario.java
│   │   └── UsuarioRepository.java
│   │
│   ├── auth/
│   │   ├── AuthController.java
│   │   ├── AuthService.java
│   │   ├── LoginRequest.java
│   │   └── LoginResponse.java
│   │
│   ├── produto/
│   │   ├── Produto.java
│   │   ├── ProdutoRepository.java
│   │   ├── ProdutoService.java
│   │   └── ProdutoController.java
│   │
│   ├── lote/
│   │   ├── triagem/
│   │   │   ├── LoteTriagem.java
│   │   │   ├── LoteTriagemRepository.java
│   │   │   ├── LoteTriagemService.java
│   │   │   └── LoteTriagemController.java
│   │   │
│   │   ├── recebido/
│   │   │   ├── LoteRecebido.java
│   │   │   ├── LoteRecebidoRepository.java
│   │   │   ├── LoteRecebidoService.java
│   │   │   └── LoteRecebidoController.java
│   │   │
│   │   └── aprovacao/
│   │       ├── StatusAprovacao.java
│   │       ├── LoteAprovacao.java
│   │       ├── LoteAprovacaoRepository.java
│   │       ├── LoteAprovacaoService.java
│   │       └── LoteAprovacaoController.java
│   │
│   ├── contraprova/
│   │   ├── ContraprovaService.java
│   │   └── ContraprovaController.java
│   │
│   ├── defeito/
│   │   ├── DefeitoConstatado.java
│   │   └── DefeitoConstatadoRepository.java
│   │
│   └── importacao/
│       ├── ImportacaoService.java
│       ├── ImportacaoController.java
│       └── dto/
│           ├── PreviewArmResponse.java
│           ├── ItemPreviewDTO.java
│           ├── ConfirmarArmRequest.java
│           └── ItemConfirmadoDTO.java
│
└── NexusApplication.java

src/main/resources/
├── db/migration/
│   ├── V1__create_schema.sql                          → tabelas base
│   ├── V2__insert_usuarios.sql                         → usuário admin inicial
│   ├── V3__add_campos_triagem.sql                      → campos de triagem (cor, estética, triador...)
│   ├── V4__add_dados_arm.sql                           → campos de importação ARM
│   ├── V5__add_defeito_constatado.sql                  → tabela de defeitos + campos status_item/defeito_constatado_id
│   ├── V6__add_resetado_produto.sql                    → coluna resetado em tb_produto
│   ├── V7__create_tb_lote_recebido.sql                 → tabela de conferência física
│   ├── V8__add_status_item_resetado_lote_recebido.sql  → campos próprios em tb_lote_recebido
│   └── V9__create_tb_lote_aprovacao.sql                → tabela de aprovação/reprovação do lote
└── application.properties
```

> Documentação detalhada de arquitetura, regras de negócio, fluxo de trabalho e histórico de bugs está em [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md), [`docs/CURRENT_FEATURE.md`](docs/CURRENT_FEATURE.md) e [`docs/features/`](docs/features/).

### Planejado, ainda não implementado

- DTOs de entrada/saída dedicados para `Produto` e `LoteRecebido` (hoje os Controllers expõem a entidade diretamente) — **próxima feature**
- `BusinessException`/`ResourceNotFoundException` dedicadas (hoje só `RuntimeException` genérica)
- Relatório compilado de lotes em Excel (`domain/relatorio/`)
- Regra de bloqueio de aprovação com divergências de Contraprova não resolvidas
- Migração da base histórica (planilha `REVERSA_CLARO`, ~31k itens) para o banco
- Frontend Angular seguindo identidade visual da WAPS Solutions
- Fluxo de convite por e-mail para criação de usuários (substituindo cadastro direto)
- Dashboard/BI para visualização de dados de triagem
- Papéis de usuário (multi-setor)

## Funcionalidades implementadas

### Autenticação
- Login com usuário e senha (hash BCrypt)
- Geração e validação de token JWT (expiração de 8 horas)
- Proteção de rotas via Spring Security (`/api/auth/**` público, demais rotas autenticadas)

### Gestão de Produtos (CRUD)
- Cadastro completo de ativos em triagem, incluindo dados patrimoniais, financeiros, de situação e campos específicos de triagem (cor, capacidade, estética, classificação)
- Campo `triador` preenchido automaticamente a partir do usuário autenticado (não é input manual)

### Importação de planilha ARM
Fluxo em duas etapas para entrada de novos lotes de triagem a partir das planilhas de Autorização de Retorno de Material (ARM) enviadas pelos clientes:

1. **Preview** (`POST /api/produtos/importar-arm/preview`): o gestor envia o arquivo `.xlsx` e o número de protocolo (gerado pela Claro). O sistema lê as abas `ARM` (dados do cliente) e `ROMANEIO` (lista de itens), valida cada IMEI (15 dígitos, obrigatório) e retorna uma prévia sem gravar nada no banco.
2. **Confirmação** (`POST /api/produtos/importar-arm/confirmar`): após revisão e eventuais correções pelo gestor, os dados são persistidos — criação do `LoteTriagem` (com protocolo único, dados do cliente e quantidade esperada) e dos `Produto`s vinculados, com status inicial `Pendente`.

### Classificação automática (A/B/C/D)
Calculada em `ProdutoService.calcularClassificacao()` a cada gravação (`salvar()`), com base em `statusItem`, `defeitoConstatado`, `resetado` e `estetica`. Reusada por `LoteRecebido` via instância temporária de `Produto`, evitando duplicação de regra.

### Conferência física (LoteRecebido)
Registro manual pelo triador durante a conferência física dos itens recebidos — independente do `Produto` importado via ARM, permitindo comparação posterior (Contraprova).

### Contraprova
`GET /api/contraprova/lote/{loteTriagemId}` — cruza `Produto` (expectativa, da ARM) × `LoteRecebido` (realidade, da conferência física) por número de série, classificando cada item em `OK`, `RECEBIDO_NAO_PREVISTO`, `PREVISTO_NAO_RECEBIDO` ou `DIVERGENCIA_CLASSIFICACAO`. Calculada em tempo real (sem snapshot), refletindo qualquer correção feita nos dados.

### Aprovação de lote (LoteAprovacao)
Decisão final do gestor sobre o lote, após a Contraprova — aprovação ou reprovação (com motivo obrigatório), com histórico de múltiplas decisões por lote.

## Modelo de dados (principais tabelas)

| Tabela | Descrição |
|---|---|
| `tb_usuario` | Usuários do sistema |
| `tb_produto` | Ativos em processo de triagem (expectativa, vindos da ARM) |
| `tb_lote_triagem` | Lotes de recebimento, vinculados a um protocolo único da Claro |
| `tb_lote_recebido` | Conferência física manual (realidade, registrada pelo triador) |
| `tb_lote_aprovacao` | Histórico de decisões de aprovação/reprovação por lote |
| `tb_defeito_constatado` | Tabela de apoio com os tipos de defeito catalogados |


## Como executar localmente

### Pré-requisitos
- Java 21
- Maven
- MySQL 8.4+

### Executar

```bash
mvn spring-boot:run
```

As migrations do Flyway são aplicadas automaticamente na primeira inicialização, incluindo a criação de um usuário administrador padrão.

### Endpoints principais

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/auth/login` | Autenticação, retorna token JWT |
| GET | `/api/produtos` | Lista todos os produtos |
| GET | `/api/produtos/{id}` | Busca produto por ID |
| POST | `/api/produtos` | Cria novo produto |
| PUT | `/api/produtos/{id}` | Atualiza produto |
| DELETE | `/api/produtos/{id}` | Remove produto |
| GET | `/api/lotes-triagem` | Lista todos os lotes |
| GET | `/api/lotes-triagem/{id}` | Busca lote por ID |
| POST | `/api/produtos/importar-arm/preview` | Prévia de importação de planilha ARM |
| POST | `/api/produtos/importar-arm/confirmar` | Confirma e grava a importação |
| POST | `/api/lote-recebido/registrar-conferencia` | Registra conferência física manual |
| GET | `/api/lote-recebido/lote/{loteTriagemId}` | Lista conferências de um lote |
| GET | `/api/lote-recebido/{id}` | Busca conferência por ID |
| GET | `/api/contraprova/lote/{loteTriagemId}` | Cruza Produto × LoteRecebido, aponta divergências |
| POST | `/api/lote-aprovacao` | Registra decisão (aprovação/reprovação) do gestor |
| GET | `/api/lote-aprovacao/lote/{loteTriagemId}` | Lista histórico de decisões de um lote |

---

Desenvolvido internamente pela equipe de TI da WAPS Solutions.
