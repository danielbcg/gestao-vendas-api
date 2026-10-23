package com.daniel.gestaovendasfacil.repository;

import com.daniel.gestaovendasfacil.model.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Long>, JpaSpecificationExecutor<Venda> {
}

