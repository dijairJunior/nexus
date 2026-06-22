# Nexus

Sistema interno de gestão e triagem de ativos retornados (reversa logística) da WAPS Solutions, desenvolvido para o time responsável pelo processamento de equipamentos de telecomunicações devolvidos por clientes via Claro.

## Sobre o projeto

O Nexus centraliza o fluxo de recebimento, triagem, classificação e destinação de equipamentos (celulares e demais ativos) devolvidos por clientes corporativos. Substitui o controle manual feito anteriormente em planilhas Excel, trazendo rastreabilidade, autenticação de usuários e regras de negócio automatizadas.

## Stack tecnológica

| Camada | Tecnologia |
|---|---|
| Backend | Java 21 + Spring Boot 4.1.0 |
| Frontend | Angular |
| Banco de dados | MySQL 8.4 |
| Migrations | Flyway |
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
│   │   ├── LoteTriagem.java
│   │   ├── LoteTriagemRepository.java
│   │   ├── LoteTriagemService.java
│   │   └── LoteTriagemController.java
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
│   ├── V1__create_schema.sql          → tabelas base
│   ├── V2__insert_usuarios.sql        → usuário admin inicial
│   ├── V3__add_campos_triagem.sql     → campos de triagem (cor, estética, triador...)
│   └── V4__add_dados_arm.sql          → campos de importação ARM
└── application.properties
```

### Planejado, ainda não implementado

- `domain/lote/LoteAprovacao.java` (+ Repository/Service/Controller)
- `exception/GlobalExceptionHandler.java`, `ResourceNotFoundException.java`, `BusinessException.java` — tratamento de erros padronizado
- DTOs de entrada/saída dedicados para `Produto` (hoje os Controllers expõem a entidade diretamente)
- `tb_defeito_constatado` e `tb_lote_recebido`

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

## Modelo de dados (principais tabelas)

| Tabela | Descrição |
|---|---|
| `tb_usuario` | Usuários do sistema |
| `tb_produto` | Ativos em processo de triagem |
| `tb_lote_triagem` | Lotes de recebimento, vinculados a um protocolo único da Claro |
| `tb_lote_aprovacao` | Lotes após aprovação na triagem |

## Pendências conhecidas

- Validação de IMEI duplicado entre lotes diferentes
- Classificação automática A/B/C/D conforme regra de negócio (status do item, defeito constatado, estética)
- Tabelas de apoio `tb_defeito_constatado` e `tb_lote_recebido`
- Fluxo de convite por e-mail para criação de usuários (substituindo cadastro direto)
- Dashboard/BI para visualização de dados de triagem
- Migração da base histórica (planilha `REVERSA_CLARO`) para o banco
- Frontend Angular seguindo identidade visual da WAPS Solutions

## Como executar localmente

### Pré-requisitos
- Java 21
- Maven
- MySQL 8.4+

### Configuração

Edite `src/main/resources/application.properties` com as credenciais do seu banco:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/triagem_ativos?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Sao_Paulo
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
```

> **Atenção:** nunca commite credenciais reais neste arquivo. Use variáveis de ambiente (`${DB_PASSWORD}`) ou um `application-local.properties` listado no `.gitignore`.

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

---

Desenvolvido internamente pela equipe de TI da WAPS Solutions.
