package com.daniel.gestaovendasfacil.service.normalizer;

public class NormalizationResult<T> {
    private final T value;
    private final boolean precisaRevisao;
    private final String motivoRevisao;

    public NormalizationResult(T value, boolean precisaRevisao, String motivoRevisao) {
        this.value = value;
        this.precisaRevisao = precisaRevisao;
        this.motivoRevisao = motivoRevisao;
    }

    public static <T> NormalizationResult<T> ok(T value) {
        return new NormalizationResult<>(value, false, null);
    }

    public static <T> NormalizationResult<T> revisar(T value, String motivo) {
        return new NormalizationResult<>(value, true, motivo);
    }

    public T getValue() {
        return value;
    }

    public boolean isPrecisaRevisao() {
        return precisaRevisao;
    }

    public String getMotivoRevisao() {
        return motivoRevisao;
    }
}

