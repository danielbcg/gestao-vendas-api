package com.daniel.gestaovendasfacil.service.normalizer;

public class ClientResult {
    private final String cliente;
    private final String observacaoAtualizada;
    private final String codigoRastreioExtraido;
    private final boolean precisaRevisao;
    private final String motivoRevisao;

    public ClientResult(String cliente, String observacaoAtualizada, String codigoRastreioExtraido, boolean precisaRevisao, String motivoRevisao) {
        this.cliente = cliente;
        this.observacaoAtualizada = observacaoAtualizada;
        this.codigoRastreioExtraido = codigoRastreioExtraido;
        this.precisaRevisao = precisaRevisao;
        this.motivoRevisao = motivoRevisao;
    }

    public String getCliente() {
        return cliente;
    }

    public String getObservacaoAtualizada() {
        return observacaoAtualizada;
    }

    public String getCodigoRastreioExtraido() {
        return codigoRastreioExtraido;
    }

    public boolean isPrecisaRevisao() {
        return precisaRevisao;
    }

    public String getMotivoRevisao() {
        return motivoRevisao;
    }
}

