package com.daniel.gestaovendasfacil.dto;

public class ItemIgnoradoDTO {
    private int linha;
    private String motivo;

    public ItemIgnoradoDTO() {
    }

    public ItemIgnoradoDTO(int linha, String motivo) {
        this.linha = linha;
        this.motivo = motivo;
    }

    public int getLinha() {
        return linha;
    }

    public void setLinha(int linha) {
        this.linha = linha;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}

