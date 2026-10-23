package com.daniel.gestaovendasfacil.dto;

public class ItemRevisaoDTO {
    private int linha;
    private String campo;
    private String valorOriginal;
    private String motivo;
    private Long vendaId;

    public ItemRevisaoDTO() {
    }

    public ItemRevisaoDTO(int linha, String campo, String valorOriginal, String motivo, Long vendaId) {
        this.linha = linha;
        this.campo = campo;
        this.valorOriginal = valorOriginal;
        this.motivo = motivo;
        this.vendaId = vendaId;
    }

    public int getLinha() {
        return linha;
    }

    public void setLinha(int linha) {
        this.linha = linha;
    }

    public String getCampo() {
        return campo;
    }

    public void setCampo(String campo) {
        this.campo = campo;
    }

    public String getValorOriginal() {
        return valorOriginal;
    }

    public void setValorOriginal(String valorOriginal) {
        this.valorOriginal = valorOriginal;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Long getVendaId() {
        return vendaId;
    }

    public void setVendaId(Long vendaId) {
        this.vendaId = vendaId;
    }
}

