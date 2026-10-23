package com.daniel.gestaovendasfacil.service.normalizer;

import com.daniel.gestaovendasfacil.model.FormaEntrega;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DeliveryNormalizer {

    public static final Pattern RASTREIO_PATTERN = Pattern.compile("(?i)\\b([A-Z]{2}\\d{9}[A-Z]{2})\\b");

    public DeliveryResult normalize(String rawEntrega, String rawRastreio, String extraRastreio) {
        String cleanEntrega = clean(rawEntrega);
        String cleanRastreio = (rawRastreio != null && !rawRastreio.trim().isEmpty()) ? rawRastreio.trim() : null;

        boolean rastreioMovido = false;
        if (cleanRastreio == null && extraRastreio != null && !extraRastreio.trim().isEmpty()) {
            cleanRastreio = extraRastreio.trim();
            rastreioMovido = true;
        }

        if (cleanEntrega.isEmpty()) {
            return new DeliveryResult(
                    FormaEntrega.LOJA,
                    cleanRastreio,
                    true,
                    rastreioMovido ? "Código de rastreio extraído e entrega não informada" : "Forma de entrega não informada"
            );
        }

        FormaEntrega forma = null;
        if (cleanEntrega.contains("loja")) {
            forma = FormaEntrega.LOJA;
        } else if (cleanEntrega.contains("presencial")) {
            forma = FormaEntrega.PRESENCIAL;
        } else if (cleanEntrega.contains("sedex")) {
            forma = FormaEntrega.SEDEX;
        } else if (cleanEntrega.contains("correios")) {
            forma = FormaEntrega.CORREIOS;
        } else if (cleanEntrega.contains("motoboy")) {
            forma = FormaEntrega.MOTOBOY;
        } else if (cleanEntrega.contains("veio buscar") || cleanEntrega.contains("retirada")) {
            forma = FormaEntrega.RETIRADA;
        }

        if (forma == null) {
            return new DeliveryResult(
                    FormaEntrega.LOJA,
                    cleanRastreio,
                    true,
                    "Forma de entrega não reconhecida (" + rawEntrega + ")"
            );
        }

        if (rastreioMovido) {
            return new DeliveryResult(
                    forma,
                    cleanRastreio,
                    true,
                    "Código de rastreio encontrado fora da coluna de rastreio"
            );
        }

        if ((forma == FormaEntrega.CORREIOS || forma == FormaEntrega.SEDEX) && (cleanRastreio == null || cleanRastreio.isBlank())) {
            return new DeliveryResult(
                    forma,
                    null,
                    true,
                    "Código de rastreio ausente para " + forma.name()
            );
        }

        return new DeliveryResult(forma, cleanRastreio, false, null);
    }

    public static String extractTrackingCode(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        Matcher matcher = RASTREIO_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).toUpperCase(Locale.ROOT);
        }
        return null;
    }

    private String clean(String text) {
        if (text == null) {
            return "";
        }
        String nfd = Normalizer.normalize(text.trim(), Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}

