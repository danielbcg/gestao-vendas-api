package com.daniel.gestaovendasfacil.dto;

import java.util.ArrayList;
import java.util.List;

public class ErroRespostaDTO {
    private String mensagem;
    private List<CampoErroDTO> erros = new ArrayList<>();

    public ErroRespostaDTO() {
    }

    public ErroRespostaDTO(String mensagem) {
        this.mensagem = mensagem;
    }

    public ErroRespostaDTO(String mensagem, List<CampoErroDTO> erros) {
        this.mensagem = mensagem;
        this.erros = erros;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public List<CampoErroDTO> getErros() {
        return erros;
    }

    public void setErros(List<CampoErroDTO> erros) {
        this.erros = erros;
    }
}

