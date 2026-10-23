package com.daniel.gestaovendasfacil.repository;

import com.daniel.gestaovendasfacil.model.FormaPagamento;
import com.daniel.gestaovendasfacil.model.Venda;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VendaSpecification {

    public static Specification<Venda> comFiltros(
            LocalDate dataInicio,
            LocalDate dataFim,
            String cliente,
            FormaPagamento formaPagamento,
            String produto,
            String texto
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (dataInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("data"), dataInicio));
            }
            if (dataFim != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("data"), dataFim));
            }
            if (cliente != null && !cliente.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("cliente")), "%" + cliente.trim().toLowerCase() + "%"));
            }
            if (formaPagamento != null) {
                predicates.add(cb.equal(root.get("formaPagamento"), formaPagamento));
            }
            if (produto != null && !produto.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("produto")), "%" + produto.trim().toLowerCase() + "%"));
            }
            if (texto != null && !texto.isBlank()) {
                String likePattern = "%" + texto.trim().toLowerCase() + "%";
                Predicate pCliente = cb.like(cb.lower(root.get("cliente")), likePattern);
                Predicate pProduto = cb.like(cb.lower(root.get("produto")), likePattern);
                Predicate pObs = cb.like(cb.lower(cb.coalesce(root.get("observacao"), "")), likePattern);
                Predicate pRastreio = cb.like(cb.lower(cb.coalesce(root.get("codigoRastreio"), "")), likePattern);
                predicates.add(cb.or(pCliente, pProduto, pObs, pRastreio));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

