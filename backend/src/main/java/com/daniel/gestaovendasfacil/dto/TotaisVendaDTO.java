package com.daniel.gestaovendasfacil.dto;

import java.math.BigDecimal;

public class TotaisVendaDTO {
    private BigDecimal totalVendas = BigDecimal.ZERO;
    private BigDecimal totalDevolucoes = BigDecimal.ZERO;
    private BigDecimal totalGeral = BigDecimal.ZERO;
    private long quantidadeRegistros = 0;

    public TotaisVendaDTO() {
    }

    public TotaisVendaDTO(BigDecimal totalVendas, BigDecimal totalDevolucoes, BigDecimal totalGeral, long quantidadeRegistros) {
        this.totalVendas = totalVendas;
        this.totalDevolucoes = totalDevolucoes;
        this.totalGeral = totalGeral;
        this.quantidadeRegistros = quantidadeRegistros;
    }

    public BigDecimal getTotalVendas() {
        return totalVendas;
    }

    public void setTotalVendas(BigDecimal totalVendas) {
        this.totalVendas = totalVendas;
    }

    public BigDecimal getTotalDevolucoes() {
        return totalDevolucoes;
    }

    public void setTotalDevolucoes(BigDecimal totalDevolucoes) {
        this.totalDevolucoes = totalDevolucoes;
    }

    public BigDecimal getTotalGeral() {
        return totalGeral;
    }

    public void setTotalGeral(BigDecimal totalGeral) {
        this.totalGeral = totalGeral;
    }

    public long getQuantidadeRegistros() {
        return quantidadeRegistros;
    }

    public void setQuantidadeRegistros(long quantidadeRegistros) {
        this.quantidadeRegistros = quantidadeRegistros;
    }
}

