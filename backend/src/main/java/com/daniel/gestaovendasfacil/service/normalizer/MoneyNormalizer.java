package com.daniel.gestaovendasfacil.service.normalizer;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MoneyNormalizer {

    private static final Pattern SOMA_PATTERN = Pattern.compile(
            "([+-]?\\d+(?:[.,]\\d+)?)\\s*\\+\\s*([+-]?\\d+(?:[.,]\\d+)?)"
    );

    public NormalizationResult<BigDecimal> normalize(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return NormalizationResult.revisar(BigDecimal.ZERO, "Valor não informado");
        }

        String input = raw.trim();

        // Checar padrão de soma N+M (ex: 35+12 frete)
        Matcher somaMatcher = SOMA_PATTERN.matcher(input);
        if (somaMatcher.find()) {
            try {
                BigDecimal n1 = parseSingleValue(somaMatcher.group(1));
                BigDecimal n2 = parseSingleValue(somaMatcher.group(2));
                BigDecimal total = n1.add(n2).setScale(2, RoundingMode.HALF_UP);
                return NormalizationResult.revisar(total, "possível frete incluído");
            } catch (Exception e) {
                return NormalizationResult.revisar(BigDecimal.ZERO, "Expressão de valor inválida (" + input + ")");
            }
        }

        try {
            BigDecimal val = parseSingleValue(input);
            return NormalizationResult.ok(val.setScale(2, RoundingMode.HALF_UP));
        } catch (Exception e) {
            return NormalizationResult.revisar(BigDecimal.ZERO, "Valor inválido (" + input + ")");
        }
    }

    private BigDecimal parseSingleValue(String text) {
        String cleaned = text.replaceAll("[R$\\s]", "");
        // Manter sinal negativo se existir
        boolean isNegative = cleaned.startsWith("-");
        if (isNegative) {
            cleaned = cleaned.substring(1);
        }
        // Substituir vírgula por ponto
        cleaned = cleaned.replace(",", ".");
        // Remover quaisquer caracteres não numéricos restantes exceto ponto
        cleaned = cleaned.replaceAll("[^0-9.]", "");
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Vazio");
        }
        BigDecimal bd = new BigDecimal(cleaned);
        return isNegative ? bd.negate() : bd;
    }
}

