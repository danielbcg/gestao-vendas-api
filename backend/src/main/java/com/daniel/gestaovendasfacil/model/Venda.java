package com.daniel.gestaovendasfacil.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "venda")
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoVenda tipo = TipoVenda.VENDA;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private String produto;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private String cliente = "Não informado";

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false)
    private FormaPagamento formaPagamento = FormaPagamento.NAO_INFORMADO;

    @Enumerated(EnumType.STRING)
    @Column(name = "bandeira_cartao")
    private BandeiraCartao bandeiraCartao;

    @Column(name = "parcelas")
    private Integer parcelas = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_entrega", nullable = false)
    private FormaEntrega formaEntrega;

    @Column(name = "codigo_rastreio")
    private String codigoRastreio;

    @Column(name = "observacao", length = 1000)
    private String observacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrigemVenda origem = OrigemVenda.IMPORTACAO;

    @Column(name = "linha_original")
    private Integer linhaOriginal;

    @Column(name = "precisa_revisao", nullable = false)
    private boolean precisaRevisao = false;

    @Column(name = "motivo_revisao", length = 1000)
    private String motivoRevisao;

    @Column(name = "sugestao_ia_aceita")
    private Boolean sugestaoIaAceita;

    public Venda() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoVenda getTipo() {
        return tipo;
    }

    public void setTipo(TipoVenda tipo) {
        this.tipo = tipo;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getProduto() {
        return produto;
    }

    public void setProduto(String produto) {
        this.produto = produto;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = (cliente == null || cliente.isBlank()) ? "Não informado" : cliente;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public BandeiraCartao getBandeiraCartao() {
        return bandeiraCartao;
    }

    public void setBandeiraCartao(BandeiraCartao bandeiraCartao) {
        this.bandeiraCartao = bandeiraCartao;
    }

    public Integer getParcelas() {
        return parcelas;
    }

    public void setParcelas(Integer parcelas) {
        this.parcelas = parcelas;
    }

    public FormaEntrega getFormaEntrega() {
        return formaEntrega;
    }

    public void setFormaEntrega(FormaEntrega formaEntrega) {
        this.formaEntrega = formaEntrega;
    }

    public String getCodigoRastreio() {
        return codigoRastreio;
    }

    public void setCodigoRastreio(String codigoRastreio) {
        this.codigoRastreio = codigoRastreio;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public OrigemVenda getOrigem() {
        return origem;
    }

    public void setOrigem(OrigemVenda origem) {
        this.origem = origem;
    }

    public Integer getLinhaOriginal() {
        return linhaOriginal;
    }

    public void setLinhaOriginal(Integer linhaOriginal) {
        this.linhaOriginal = linhaOriginal;
    }

    public boolean isPrecisaRevisao() {
        return precisaRevisao;
    }

    public void setPrecisaRevisao(boolean precisaRevisao) {
        this.precisaRevisao = precisaRevisao;
    }

    public String getMotivoRevisao() {
        return motivoRevisao;
    }

    public void setMotivoRevisao(String motivoRevisao) {
        this.motivoRevisao = motivoRevisao;
    }

    public Boolean getSugestaoIaAceita() {
        return sugestaoIaAceita;
    }

    public void setSugestaoIaAceita(Boolean sugestaoIaAceita) {
        this.sugestaoIaAceita = sugestaoIaAceita;
    }
}

