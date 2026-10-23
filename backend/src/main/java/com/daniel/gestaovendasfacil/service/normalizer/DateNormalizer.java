package com.daniel.gestaovendasfacil.service.normalizer;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DateNormalizer {

    private static final Pattern EXTENSO_PATTERN = Pattern.compile(
            "^(\\d{1,2})\\s+de\\s+([a-zA-Z]+)(?:\\s+de\\s+(\\d{2,4}))?$",
            Pattern.CASE_INSENSITIVE
    );

    public NormalizationResult<LocalDate> normalize(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return NormalizationResult.revisar(LocalDate.now(), "Data não informada");
        }

        String input = raw.trim();
        String normalized = removeAccents(input).toLowerCase(Locale.ROOT);

        if (normalized.equals("hj") || normalized.equals("hoje")) {
            return NormalizationResult.ok(LocalDate.now());
        }

        if (normalized.equals("ontem")) {
            return NormalizationResult.ok(LocalDate.now().minusDays(1));
        }

        if (normalized.matches("^(segunda|terca|quarta|quinta|sexta|sabado|domingo).*")) {
            return NormalizationResult.revisar(LocalDate.now(), "Dia da semana informado (" + input + ")");
        }

        // Formato por extenso: "12 de marco", "12 de marco de 2026"
        Matcher extensoMatcher = EXTENSO_PATTERN.matcher(normalized);
        if (extensoMatcher.matches()) {
            try {
                int dia = Integer.parseInt(extensoMatcher.group(1));
                int mes = parseMes(extensoMatcher.group(2));
                int ano = 2026;
                if (extensoMatcher.group(3) != null) {
                    ano = parseAno(extensoMatcher.group(3));
                }
                return NormalizationResult.ok(LocalDate.of(ano, mes, dia));
            } catch (Exception e) {
                return NormalizationResult.revisar(LocalDate.now(), "Data por extenso inválida (" + input + ")");
            }
        }

        // Formato ISO: yyyy-MM-dd
        if (normalized.matches("^\\d{4}-\\d{1,2}-\\d{1,2}$")) {
            try {
                String[] parts = normalized.split("-");
                int ano = Integer.parseInt(parts[0]);
                int mes = Integer.parseInt(parts[1]);
                int dia = Integer.parseInt(parts[2]);
                return NormalizationResult.ok(LocalDate.of(ano, mes, dia));
            } catch (Exception e) {
                return NormalizationResult.revisar(LocalDate.now(), "Data ISO inválida (" + input + ")");
            }
        }

        // Formato com barras ou hífens: d/m, d/m/y, m/d/y
        String separator = normalized.contains("/") ? "/" : (normalized.contains("-") ? "-" : null);
        if (separator != null) {
            String[] parts = normalized.split(Pattern.quote(separator));
            if (parts.length == 2) {
                // d/m sem ano -> usar 2026
                try {
                    int p1 = Integer.parseInt(parts[0]);
                    int p2 = Integer.parseInt(parts[1]);
                    int ano = 2026;

                    if (p1 > 12 && p2 <= 12) {
                        return NormalizationResult.ok(LocalDate.of(ano, p2, p1));
                    } else if (p2 > 12 && p1 <= 12) {
                        return NormalizationResult.ok(LocalDate.of(ano, p1, p2));
                    } else if (p1 <= 12 && p2 <= 12) {
                        // Ambiguidade como 12/3 ou 4/8: assumir dd/mm e marcar REVISAR
                        return NormalizationResult.revisar(LocalDate.of(ano, p2, p1), "Data com ambiguidade entre dia e mês (" + input + ")");
                    } else {
                        return NormalizationResult.revisar(LocalDate.now(), "Data inválida (" + input + ")");
                    }
                } catch (Exception e) {
                    return NormalizationResult.revisar(LocalDate.now(), "Formato de data inválido (" + input + ")");
                }
            } else if (parts.length == 3) {
                try {
                    int p1 = Integer.parseInt(parts[0]);
                    int p2 = Integer.parseInt(parts[1]);
                    int p3 = Integer.parseInt(parts[2]);

                    if (p1 > 1000) { // yyyy/MM/dd
                        return NormalizationResult.ok(LocalDate.of(p1, p2, p3));
                    }

                    int ano = parseAno(String.valueOf(p3));

                    // Caso onde p2 > 12: ex: 8/16/26 -> mês=8, dia=16, ano=2026
                    if (p2 > 12 && p1 <= 12) {
                        return NormalizationResult.ok(LocalDate.of(ano, p1, p2));
                    }

                    // Caso onde p1 > 12: ex: 16/08/2026 ou 19/09/2026 -> dia=16/19, mês=8/9
                    if (p1 > 12 && p2 <= 12) {
                        return NormalizationResult.ok(LocalDate.of(ano, p2, p1));
                    }

                    // Ambos <= 12 com ano informado: ex: 04/08/2026
                    // Se estiver no formato dd/MM/yyyy padrão brasileiro com 4 dígitos no ano ou 2 dígitos:
                    // Na planilha Maria escreve formato BR (04/08/2026, 10/09/2026).
                    // Para 3 partes com dia e mês <= 12 (ex: 04/08/2026):
                    // Se o usuário escreveu dd/MM/yyyy, é dd/MM/yyyy.
                    return NormalizationResult.ok(LocalDate.of(ano, p2, p1));
                } catch (Exception e) {
                    return NormalizationResult.revisar(LocalDate.now(), "Data inválida (" + input + ")");
                }
            }
        }

        return NormalizationResult.revisar(LocalDate.now(), "Formato de data desconhecido (" + input + ")");
    }

    private int parseAno(String raw) {
        int ano = Integer.parseInt(raw);
        if (ano < 100) {
            return 2000 + ano;
        }
        return ano;
    }

    private int parseMes(String nomeMes) {
        String m = nomeMes.toLowerCase(Locale.ROOT);
        return switch (m) {
            case "janeiro", "jan" -> 1;
            case "fevereiro", "fev" -> 2;
            case "marco", "mar" -> 3;
            case "abril", "abr" -> 4;
            case "maio", "mai" -> 5;
            case "junho", "jun" -> 6;
            case "julho", "jul" -> 7;
            case "agosto", "ago" -> 8;
            case "setembro", "set" -> 9;
            case "outubro", "out" -> 10;
            case "novembro", "nov" -> 11;
            case "dezembro", "dez" -> 12;
            default -> throw new IllegalArgumentException("Mês desconhecido: " + nomeMes);
        };
    }

    private String removeAccents(String text) {
        String nfd = Normalizer.normalize(text, Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{M}", "");
    }
}

