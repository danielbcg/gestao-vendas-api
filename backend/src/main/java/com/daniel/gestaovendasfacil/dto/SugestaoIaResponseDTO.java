package com.daniel.gestaovendasfacil.dto;

public class SugestaoIaResponseDTO {
    private boolean disponivel;
    private String mensagem;
    private String campo;
    private String valorOriginal;
    private String valorSugerido;
    private String explicacao;

    public SugestaoIaResponseDTO() {
    }

    public static SugestaoIaResponseDTO indisponivel(String mensagem) {
        SugestaoIaResponseDTO dto = new SugestaoIaResponseDTO();
        dto.setDisponivel(false);
        dto.setMensagem(mensagem);
        return dto;
    }

    public static SugestaoIaResponseDTO disponivel(String campo, String valorOriginal, String valorSugerido, String explicacao) {
        SugestaoIaResponseDTO dto = new SugestaoIaResponseDTO();
        dto.setDisponivel(true);
        dto.setCampo(campo);
        dto.setValorOriginal(valorOriginal);
        dto.setValorSugerido(valorSugerido);
        dto.setExplicacao(explicacao);
        return dto;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    public void setDisponivel(boolean disponivel) {
        this.disponivel = disponivel;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
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

    public String getValorSugerido() {
        return valorSugerido;
    }

    public void setValorSugerido(String valorSugerido) {
        this.valorSugerido = valorSugerido;
    }

    public String getExplicacao() {
        return explicacao;
    }

    public void setExplicacao(String explicacao) {
        this.explicacao = explicacao;
    }
}

