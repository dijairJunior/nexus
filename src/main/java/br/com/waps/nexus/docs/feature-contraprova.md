# Feature: Contraprova

**Status:** Mergeada em `main`
**Branch:** `feature/contraprova`

## Objetivo

Cruzar `tb_lote_recebido` (conferência física manual) × `tb_produto` (planilha ARM importada) por `numeroSerie` + `loteTriagemId`, classificando cada IMEI em 4 categorias.

## Arquivos criados

| Arquivo | Pasta |
|---|---|
| `ContraprovaItemDTO.java` | `dto/` |
| `ContraprovaResumoDTO.java` | `dto/` |
| `ContraprovaService.java` | `domain/contraprova/` |
| `ContraprovaController.java` | `domain/contraprova/` |

Primeira feature a efetivamente usar a pasta `dto/` (antes vazia) — Controllers de `Produto`/`LoteRecebido` ainda expõem entidade direto.

## Enum `StatusContraprova`

```java
OK,
RECEBIDO_NAO_PREVISTO,       // existe em lote_recebido mas não em produto
PREVISTO_NAO_RECEBIDO,       // existe em produto mas não em lote_recebido
DIVERGENCIA_CLASSIFICACAO    // existe nos dois, classificação diferente
```

## Endpoint

`GET /api/contraprova/lote/{loteTriagemId}`

## Bugs corrigidos durante a implementação

1. Campo `loteTriagemRepository` usado no construtor mas nunca declarado na classe.
2. `existsById(loteTriagemId.longValue())` — `LoteTriagemRepository` é `JpaRepository<LoteTriagem, Integer>`, não `Long`; corrigido para `existsById(loteTriagemId)`.
3. Typo no enum: `DIVERGENCIA_CLASSIFICADA` não existe (correto: `DIVERGENCIA_CLASSIFICACAO`).
4. Setter errado: `setTotalPrevistoRecebido()` não existe no DTO (correto: `setTotalPrevistoNaoRecebido()`).
5. Variável com acento (`totalPrevistoNãoRecebido`) — padronizada sem acento.

## Investigação: "bug" `classificacao NULL` (resolvido — não é bug)

Ao validar os 4 cenários no lote de teste (`loteTriagemId = 1`), notou-se que os produtos desse lote tinham `tb_produto.classificacao = NULL`.

**Causa raiz identificada** (lendo o código de `ImportacaoService`):
- `confirmarImportacao()` usa `produtoRepository.saveAll(produtos)` direto — não passa por `ProdutoService.salvar()`, onde `calcularClassificacao()` roda.
- O `ItemConfirmadoDTO` da planilha ARM não tem `estetica`/`defeitoConstatadoId`/`statusItem`/`resetado` — sem esses campos não haveria como calcular classificação de qualquer forma.
- `statusCadastro = "Pendente"` é o sinal de que o produto foi importado mas ainda não passou pela triagem física.

**Conclusão:** classificação só existe depois que o produto é atualizado via `ProdutoService.salvar()` (rota de triagem/edição). Lote 1 nunca passou por isso — dado de teste antigo. **Não é bug — comportamento esperado por design.**

## Testes realizados

Validado manualmente via Insomnia no lote 1, com 2 produtos ajustados via `UPDATE` pontual (`classificacao = 'A'` e `'B'`) e 16 registros de teste em `LoteRecebido`, cobrindo os 4 cenários:

| Cenário | Confirmado |
|---|---|
| `OK` | ✅ |
| `DIVERGENCIA_CLASSIFICACAO` | ✅ |
| `RECEBIDO_NAO_PREVISTO` | ✅ |
| `PREVISTO_NAO_RECEBIDO` | ✅ |
