package br.com.waps.nexus.domain.lote.triagem;

import org.springframework.data.jpa.domain.Specification;

public class LoteTriagemSpecification {

    public static Specification<LoteTriagem> comFiltro(FiltroLote filtro) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (filtro.status() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), filtro.status()));
            }

            if (filtro.search() != null && !filtro.search().isBlank()) {
                String termo = "%" + filtro.search().trim() + "%";
                var porId = tryParseId(filtro.search(), cb, root);
                var porNumero = cb.like(root.get("numero").as(String.class), termo);
                var porTexto = cb.like(cb.lower(root.get("descricao")), termo.toLowerCase());

                var buscaOr = porId != null
                        ? cb.or(porId, porNumero, porTexto)
                        : cb.or(porNumero, porTexto);

                predicates = cb.and(predicates, buscaOr);
            }

            return predicates;
        };
    }

    private static jakarta.persistence.criteria.Predicate tryParseId(
            String search, jakarta.persistence.criteria.CriteriaBuilder cb,
            jakarta.persistence.criteria.Root<LoteTriagem> root) {
        try {
            Integer id = Integer.parseInt(search.trim());
            return cb.equal(root.get("id"), id);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
