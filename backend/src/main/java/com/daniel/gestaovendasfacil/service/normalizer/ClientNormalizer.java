package com.daniel.gestaovendasfacil.service.normalizer;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ClientNormalizer {

    private static final Pattern TELEFONE_PATTERN = Pattern.compile("^\\+?\\d{2}?\\s*\\(?\\d{2}\\)?\\s*\\d{4,5}[-\\s]?\\d{4}$|^\\d{8,13}$");
    private static final Pattern ERA_PATTERN = Pattern.compile("(?i)\\bera\\s+(?:a|o)?\\s*([a-zA-ZÀ-ÿ]+(?:\\s+[a-zA-ZÀ-ÿ]+)*)");

    public ClientResult normalize(String rawCliente, String rawObs) {
        String cleanClient = (rawCliente != null) ? rawCliente.trim() : "";
        String cleanObs = (rawObs != null) ? rawObs.trim() : "";
        String trackingCode = DeliveryNormalizer.extractTrackingCode(cleanClient);

        String nomeEra = extractNomeEra(cleanObs);

        boolean isTelefone = isTelefone(cleanClient);
        boolean isNaoLembro = isNaoLembro(cleanClient);
        boolean isTracking = trackingCode != null;

        if (isTracking || isTelefone || isNaoLembro) {
            String clientFinal = (nomeEra != null && !nomeEra.isBlank()) ? nomeEra : "Não informado";
            String novaObs;
            if (cleanObs.isEmpty()) {
                novaObs = cleanClient;
            } else if (!cleanObs.contains(cleanClient)) {
                novaObs = cleanClient + " - " + cleanObs;
            } else {
                novaObs = cleanObs;
            }

            String motivo;
            if (isTracking) {
                motivo = "Código de rastreio informado na coluna de cliente";
            } else if (isTelefone) {
                motivo = "Telefone informado na coluna de cliente";
            } else {
                motivo = "Cliente marcado como não lembro";
            }

            return new ClientResult(clientFinal, novaObs, isTracking ? trackingCode : null, true, motivo);
        }

        if (cleanClient.isEmpty()) {
            String clientFinal = (nomeEra != null && !nomeEra.isBlank()) ? nomeEra : "Não informado";
            return new ClientResult(clientFinal, cleanObs, null, false, null);
        }

        // Se tiver nome normal
        return new ClientResult(cleanClient, cleanObs, null, false, null);
    }

    private boolean isTelefone(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String digitsOnly = text.replaceAll("[^0-9]", "");
        if (digitsOnly.length() >= 8 && digitsOnly.length() <= 13) {
            return TELEFONE_PATTERN.matcher(text.trim()).matches() || digitsOnly.equals(text.trim());
        }
        return false;
    }

    private boolean isNaoLembro(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String clean = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
        return clean.equals("nao lembro") || clean.equals("nao me lembro") || clean.contains("nao lembro");
    }

    private String extractNomeEra(String obs) {
        if (obs == null || obs.isBlank()) {
            return null;
        }
        Matcher matcher = ERA_PATTERN.matcher(obs);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
}

