# Nexus

Sistema de gestão de logística reversa desenvolvido para controlar o
recebimento, conferência, classificação e aprovação de ativos
retornados.

O projeto substitui processos manuais baseados em planilhas por uma
aplicação web com autenticação, rastreabilidade e regras de negócio
automatizadas.

## Tecnologias

Camada                 Tecnologia
  ---------------------- -----------------------
Backend                Java 21 + Spring Boot
Banco                  MySQL
Migrations             Flyway
Segurança              Spring Security + JWT
Leitura de planilhas   Apache POI
Build                  Maven

## Arquitetura

-   Spring Boot
-   Arquitetura em camadas (Controller → Service → Repository)
-   DTOs para comunicação da API
-   Banco versionado com Flyway
-   Autenticação JWT
-   Tratamento global de exceções

## Funcionalidades

-   Login e autenticação JWT
-   CRUD de produtos
-   Importação de planilhas Excel
-   Conferência física dos itens
-   Comparação entre expectativa e recebimento (contraprova)
-   Aprovação ou reprovação de lotes
-   Histórico de operações

## Estrutura do projeto

``` text
config/
security/
exception/
dto/
domain/
resources/db/migration/
```

## Executando

``` bash
mvn spring-boot:run
```

## Principais endpoints

-   Autenticação
-   Produtos
-   Lotes
-   Importação de planilhas
-   Conferência
-   Contraprova
-   Aprovação de lotes

## Roadmap

-   Dashboard
-   Relatórios
-   Convite de usuários por e-mail
-   Mapper dedicado
-   Bean Validation
-   Controle de permissões

------------------------------------------------------------------------