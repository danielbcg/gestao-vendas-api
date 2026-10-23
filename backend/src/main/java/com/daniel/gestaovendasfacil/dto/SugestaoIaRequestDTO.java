package com.daniel.gestaovendasfacil.dto;

public class SugestaoIaRequestDTO {
    private Long vendaId;
    private String linhaOriginal;
    private String campo;
    private String motivoRevisao;

    public SugestaoIaRequestDTO() {
    }

    public Long getVendaId() {
        return vendaId;
    }

    public void setVendaId(Long vendaId) {
        this.vendaId = vendaId;
    }

    public String getLinhaOriginal() {
        return linhaOriginal;
    }

    public void setLinhaOriginal(String linhaOriginal) {
        this.linhaOriginal = linhaOriginal;
    }

    public String getCampo() {
        return campo;
    }

    public void setCampo(String campo) {
        this.campo = campo;
    }

    public String getMotivoRevisao() {
        return motivoRevisao;
    }

    public void setMotivoRevisao(String motivoRevisao) {
        this.motivoRevisao = motivoRevisao;
    }
}

