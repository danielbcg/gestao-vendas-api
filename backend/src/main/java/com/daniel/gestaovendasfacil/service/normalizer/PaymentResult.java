package com.daniel.gestaovendasfacil.service.normalizer;

import com.daniel.gestaovendasfacil.model.BandeiraCartao;
import com.daniel.gestaovendasfacil.model.FormaPagamento;

public class PaymentResult {
    private final FormaPagamento formaPagamento;
    private final BandeiraCartao bandeiraCartao;
    private final Integer parcelas;
    private final boolean precisaRevisao;
    private final String motivoRevisao;

    public PaymentResult(FormaPagamento formaPagamento, BandeiraCartao bandeiraCartao, Integer parcelas, boolean precisaRevisao, String motivoRevisao) {
        this.formaPagamento = formaPagamento;
        this.bandeiraCartao = bandeiraCartao;
        this.parcelas = parcelas;
        this.precisaRevisao = precisaRevisao;
        this.motivoRevisao = motivoRevisao;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public BandeiraCartao getBandeiraCartao() {
        return bandeiraCartao;
    }

    public Integer getParcelas() {
        return parcelas;
    }

    public boolean isPrecisaRevisao() {
        return precisaRevisao;
    }

    public String getMotivoRevisao() {
        return motivoRevisao;
    }
}

