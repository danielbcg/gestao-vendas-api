package com.daniel.gestaovendasfacil.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportacaoRelatorioDTO {
    private int totalLinhasLidas;
    private int importadasAutomaticamente;
    private List<ItemRevisaoDTO> importadasComRevisao = new ArrayList<>();
    private List<ItemIgnoradoDTO> ignoradas = new ArrayList<>();

    public ImportacaoRelatorioDTO() {
    }

    public int getTotalLinhasLidas() {
        return totalLinhasLidas;
    }

    public void setTotalLinhasLidas(int totalLinhasLidas) {
        this.totalLinhasLidas = totalLinhasLidas;
    }

    public int getImportadasAutomaticamente() {
        return importadasAutomaticamente;
    }

    public void setImportadasAutomaticamente(int importadasAutomaticamente) {
        this.importadasAutomaticamente = importadasAutomaticamente;
    }

    public List<ItemRevisaoDTO> getImportadasComRevisao() {
        return importadasComRevisao;
    }

    public void setImportadasComRevisao(List<ItemRevisaoDTO> importadasComRevisao) {
        this.importadasComRevisao = importadasComRevisao;
    }

    public List<ItemIgnoradoDTO> getIgnoradas() {
        return ignoradas;
    }

    public void setIgnoradas(List<ItemIgnoradoDTO> ignoradas) {
        this.ignoradas = ignoradas;
    }
}

