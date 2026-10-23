package com.daniel.gestaovendasfacil.dto;

public class CampoErroDTO {
    private String campo;
    private String motivo;

    public CampoErroDTO() {
    }

    public CampoErroDTO(String campo, String motivo) {
        this.campo = campo;
        this.motivo = motivo;
    }

    public String getCampo() {
        return campo;
    }

    public void setCampo(String campo) {
        this.campo = campo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}

