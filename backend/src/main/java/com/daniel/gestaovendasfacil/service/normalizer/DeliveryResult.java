package com.daniel.gestaovendasfacil.service.normalizer;

import com.daniel.gestaovendasfacil.model.FormaEntrega;

public class DeliveryResult {
    private final FormaEntrega formaEntrega;
    private final String codigoRastreio;
    private final boolean precisaRevisao;
    private final String motivoRevisao;

    public DeliveryResult(FormaEntrega formaEntrega, String codigoRastreio, boolean precisaRevisao, String motivoRevisao) {
        this.formaEntrega = formaEntrega;
        this.codigoRastreio = codigoRastreio;
        this.precisaRevisao = precisaRevisao;
        this.motivoRevisao = motivoRevisao;
    }

    public FormaEntrega getFormaEntrega() {
        return formaEntrega;
    }

    public String getCodigoRastreio() {
        return codigoRastreio;
    }

    public boolean isPrecisaRevisao() {
        return precisaRevisao;
    }

    public String getMotivoRevisao() {
        return motivoRevisao;
    }
}

