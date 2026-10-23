package com.daniel.gestaovendasfacil.service.normalizer;

import com.daniel.gestaovendasfacil.model.TipoVenda;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Locale;

@Component
public class LineClassifier {

    public TipoVenda classify(String fullLineText, BigDecimal valor) {
        String cleanText = clean(fullLineText);

        if (cleanText.contains("comprei") || cleanText.contains("fornecedor") || cleanText.contains("estoque")) {
            return TipoVenda.COMPRA;
        }

        if (valor != null && valor.compareTo(BigDecimal.ZERO) < 0) {
            return TipoVenda.DEVOLUCAO;
        }

        return TipoVenda.VENDA;
    }

    private String clean(String text) {
        if (text == null) {
            return "";
        }
        String nfd = Normalizer.normalize(text, Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}

