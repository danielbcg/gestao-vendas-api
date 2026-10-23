package com.daniel.gestaovendasfacil.dto;

import com.daniel.gestaovendasfacil.model.BandeiraCartao;
import com.daniel.gestaovendasfacil.model.FormaEntrega;
import com.daniel.gestaovendasfacil.model.FormaPagamento;
import com.daniel.gestaovendasfacil.model.TipoVenda;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class VendaRequestDTO {

    @NotNull(message = "O tipo da venda é obrigatório.")
    private TipoVenda tipo = TipoVenda.VENDA;

    @NotNull(message = "A data da venda é obrigatória.")
    private LocalDate data;

    @NotBlank(message = "O produto é obrigatório.")
    private String produto;

    @NotNull(message = "O valor é obrigatório.")
    private BigDecimal valor;

    private String cliente;

    @NotNull(message = "A forma de pagamento é obrigatória.")
    private FormaPagamento formaPagamento;

    private BandeiraCartao bandeiraCartao;

    private Integer parcelas;

    @NotNull(message = "A forma de entrega é obrigatória.")
    private FormaEntrega formaEntrega;

    private String codigoRastreio;

    private String observacao;

    private Boolean precisaRevisao;

    private String motivoRevisao;

    private Boolean sugestaoIaAceita;

    public VendaRequestDTO() {
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
        this.cliente = cliente;
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

    public Boolean getPrecisaRevisao() {
        return precisaRevisao;
    }

    public void setPrecisaRevisao(Boolean precisaRevisao) {
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

