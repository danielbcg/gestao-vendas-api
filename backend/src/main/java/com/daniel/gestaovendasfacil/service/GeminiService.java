package com.daniel.gestaovendasfacil.service;

import com.daniel.gestaovendasfacil.dto.SugestaoIaResponseDTO;
import com.daniel.gestaovendasfacil.model.FormaEntrega;
import com.daniel.gestaovendasfacil.model.FormaPagamento;
import com.daniel.gestaovendasfacil.model.Venda;
import com.daniel.gestaovendasfacil.repository.VendaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    private final String apiKey;
    private final String model;
    private final VendaRepository vendaRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    private static final Set<String> CAMPOS_VALIDOS = Set.of(
            "data", "valor", "forma_pagamento", "forma_entrega", "cliente", "produto"
    );

    public GeminiService(
            @Value("${gemini.api.key:}") String apiKey,
            @Value("${gemini.model:gemini-flash-lite-latest}") String model,
            VendaRepository vendaRepository,
            ObjectMapper objectMapper
    ) {
        String cleanKey = apiKey != null ? apiKey.trim() : "";
        if (cleanKey.startsWith("\"") && cleanKey.endsWith("\"") && cleanKey.length() > 1) {
            cleanKey = cleanKey.substring(1, cleanKey.length() - 1).trim();
        }
        if (cleanKey.startsWith("'") && cleanKey.endsWith("'") && cleanKey.length() > 1) {
            cleanKey = cleanKey.substring(1, cleanKey.length() - 1).trim();
        }
        this.apiKey = cleanKey;
        this.model = (model != null && !model.isBlank()) ? model.trim() : "gemini-flash-lite-latest";
        this.vendaRepository = vendaRepository;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public boolean isConfigurado() {
        return !apiKey.isEmpty() && !apiKey.equalsIgnoreCase("sua_chave_aqui");
    }

    public SugestaoIaResponseDTO sugerirCorrecao(Long vendaId, String linhaOriginal, String campo, String motivoRevisao) {
        if (!isConfigurado()) {
            log.warn("Tentativa de pedir sugestão da IA, mas a chave GEMINI_API_KEY não está configurada.");
            return SugestaoIaResponseDTO.indisponivel("IA não configurada");
        }

        String valorOriginal = "";
        String contextoLinha = linhaOriginal;
        String motivo = motivoRevisao;

        if (vendaId != null) {
            Optional<Venda> vendaOpt = vendaRepository.findById(vendaId);
            if (vendaOpt.isPresent()) {
                Venda v = vendaOpt.get();
                if (motivo == null || motivo.isBlank()) {
                    motivo = v.getMotivoRevisao();
                }
                contextoLinha = String.format(
                        "Data: %s, Produto: %s, Cliente: %s, Valor: %s, Forma Pagamento: %s, Bandeira: %s, Parcelas: %s, Forma Entrega: %s, Rastreio: %s, Observação: %s",
                        v.getData(), v.getProduto(), v.getCliente(), v.getValor(), v.getFormaPagamento(),
                        v.getBandeiraCartao(), v.getParcelas(), v.getFormaEntrega(), v.getCodigoRastreio(), v.getObservacao()
                );
                valorOriginal = obterValorOriginalCampo(v, campo);
            }
        }

        String prompt = "Você é um assistente de inteligência artificial para uma pequena loja de acessórios de Maria.\n" +
                "Uma linha importada de uma planilha manual precisa de revisão.\n" +
                "Contexto da linha: " + contextoLinha + "\n" +
                "Motivo da revisão: " + (motivo != null ? motivo : "Não especificado") + "\n" +
                (campo != null ? "Campo em dúvida: " + campo + "\n" : "") +
                "Por favor, sugira uma correção segura e coerente.\n" +
                "Responda SOMENTE um objeto JSON válido, sem texto antes ou depois, sem markdown, no formato:\n" +
                "{\"campo\": \"...\", \"valorSugerido\": \"...\", \"explicacao\": \"...\"}\n" +
                "Os campos possíveis são EXCLUSIVAMENTE: data, valor, forma_pagamento, forma_entrega, cliente, produto.\n" +
                "Se for data, retorne no formato YYYY-MM-DD.\n" +
                "Se for forma_pagamento, use um destes: PIX, DINHEIRO, DEBITO, CREDITO, FIADO, MAQUININHA, PAGAR_DEPOIS.\n" +
                "Se for forma_entrega, use um destes: LOJA, PRESENCIAL, CORREIOS, SEDEX, MOTOBOY, RETIRADA.\n" +
                "Se for valor, use apenas números positivos com ponto decimal.";

        try {
            String respostaJson = chamarGeminiComRetry(prompt);
            return validarEParsarResposta(respostaJson, valorOriginal);
        } catch (HttpTimeoutException e) {
            log.warn("Falha na chamada ao Gemini: tempo limite de requisição excedido (timeout). Motivo: {}", e.getMessage());
            return SugestaoIaResponseDTO.indisponivel("A sugestão não está disponível agora. Você pode preencher a venda manualmente.");
        } catch (Exception e) {
            log.warn("Falha na obtenção da sugestão do Gemini: {}", sanitizar(e.getMessage()));
            return SugestaoIaResponseDTO.indisponivel("A sugestão não está disponível agora. Você pode preencher a venda manualmente.");
        }
    }

    public SugestaoIaResponseDTO validarEParsarResposta(String rawResposta, String valorOriginal) {
        try {
            String jsonLimpo = limparMarkdownJson(rawResposta);
            JsonNode root;
            try {
                root = objectMapper.readTree(jsonLimpo);
            } catch (Exception e) {
                log.warn("Falha na validação da sugestão do Gemini: resposta não é um JSON válido. Resposta: {}", sanitizar(rawResposta));
                return SugestaoIaResponseDTO.indisponivel("A sugestão não está disponível agora. Você pode preencher a venda manualmente.");
            }

            if (!root.has("campo") || !root.has("valorSugerido") || !root.has("explicacao")) {
                log.warn("Falha na validação da sugestão do Gemini: JSON não contém os campos obrigatórios (campo, valorSugerido, explicacao). JSON recebido: {}", sanitizar(jsonLimpo));
                return SugestaoIaResponseDTO.indisponivel("A sugestão não está disponível agora. Você pode preencher a venda manualmente.");
            }

            String campo = root.get("campo").asText().trim();
            String valorSugerido = root.get("valorSugerido").asText().trim();
            String explicacao = root.get("explicacao").asText().trim();

            if (!CAMPOS_VALIDOS.contains(campo)) {
                log.warn("Falha na validação da sugestão do Gemini: campo sugerido '{}' é inválido. Campos permitidos: {}", campo, CAMPOS_VALIDOS);
                return SugestaoIaResponseDTO.indisponivel("A sugestão não está disponível agora. Você pode preencher a venda manualmente.");
            }

            // Validações por tipo de campo
            switch (campo) {
                case "data" -> {
                    try {
                        LocalDate.parse(valorSugerido);
                    } catch (DateTimeParseException e) {
                        log.warn("Falha na validação da sugestão do Gemini: data sugerida '{}' não é válida no formato YYYY-MM-DD.", valorSugerido);
                        return SugestaoIaResponseDTO.indisponivel("A sugestão não está disponível agora. Você pode preencher a venda manualmente.");
                    }
                }
                case "valor" -> {
                    try {
                        new BigDecimal(valorSugerido);
                    } catch (Exception e) {
                        log.warn("Falha na validação da sugestão do Gemini: valor sugerido '{}' não é um número decimal válido.", valorSugerido);
                        return SugestaoIaResponseDTO.indisponivel("A sugestão não está disponível agora. Você pode preencher a venda manualmente.");
                    }
                }
                case "forma_pagamento" -> {
                    try {
                        FormaPagamento.valueOf(valorSugerido.toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException e) {
                        log.warn("Falha na validação da sugestão do Gemini: forma de pagamento sugerida '{}' não pertence aos enums válidos.", valorSugerido);
                        return SugestaoIaResponseDTO.indisponivel("A sugestão não está disponível agora. Você pode preencher a venda manualmente.");
                    }
                }
                case "forma_entrega" -> {
                    try {
                        FormaEntrega.valueOf(valorSugerido.toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException e) {
                        log.warn("Falha na validação da sugestão do Gemini: forma de entrega sugerida '{}' não pertence aos enums válidos.", valorSugerido);
                        return SugestaoIaResponseDTO.indisponivel("A sugestão não está disponível agora. Você pode preencher a venda manualmente.");
                    }
                }
                case "cliente", "produto" -> {
                    if (valorSugerido.isEmpty()) {
                        log.warn("Falha na validação da sugestão do Gemini: valor sugerido para o campo '{}' está vazio.", campo);
                        return SugestaoIaResponseDTO.indisponivel("A sugestão não está disponível agora. Você pode preencher a venda manualmente.");
                    }
                }
            }

            return SugestaoIaResponseDTO.disponivel(campo, valorOriginal, valorSugerido, explicacao);
        } catch (Exception e) {
            log.warn("Falha inesperada ao processar validação da resposta do Gemini: {}", sanitizar(e.getMessage()));
            return SugestaoIaResponseDTO.indisponivel("A sugestão não está disponível agora. Você pode preencher a venda manualmente.");
        }
    }

    private String chamarGeminiComRetry(String prompt) throws Exception {
    String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;

    Map<String, Object> part = Map.of("text", prompt);
    Map<String, Object> content = Map.of("parts", List.of(part));
    Map<String, Object> bodyMap = Map.of("contents", List.of(content));
    String requestBody = objectMapper.writeValueAsString(bodyMap);

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(30))
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    // Se 503 ou 429, retry uma vez após 3s
    if (response.statusCode() == 503 || response.statusCode() == 429) {
        log.warn("Gemini retornou status HTTP {}. Tentando novamente em 3 segundos...", response.statusCode());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    if (response.statusCode() >= 200 && response.statusCode() < 300) {
        JsonNode root = objectMapper.readTree(response.body());
        JsonNode candidates = root.get("candidates");
        if (candidates != null && candidates.isArray() && !candidates.isEmpty()) {
            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (parts.isArray() && !parts.isEmpty()) {
                return parts.get(0).path("text").asText();
            }
        }
        log.warn("Falha na resposta do Gemini: status HTTP 200 recebido, mas 'candidates' ou 'parts' está ausente ou vazio. Resposta: {}", sanitizar(response.body()));
        throw new RuntimeException("Resposta sem candidates válidos da API Gemini.");
    }

    log.warn("Falha na chamada ao Gemini: status HTTP {} retornado. Resposta da API: {}", response.statusCode(), sanitizar(response.body()));
    throw new RuntimeException("Falha na chamada ao Gemini API. Status HTTP: " + response.statusCode());
}

    private String sanitizar(String text) {
        if (text == null) {
            return "";
        }
        String limpo = text;
        if (apiKey != null && !apiKey.isEmpty()) {
            limpo = limpo.replace(apiKey, "***");
        }
        // Substituir qualquer ocorrência de key=... ou key="..." na mensagem por key=***
        limpo = limpo.replaceAll("(?i)(key=)[^&\\s\"'<>]+", "$1***");
        return limpo;
    }

    private String limparMarkdownJson(String text) {
        if (text == null) {
            return "";
        }
        String clean = text.trim();
        if (clean.startsWith("```json")) {
            clean = clean.substring(7);
        } else if (clean.startsWith("```")) {
            clean = clean.substring(3);
        }
        if (clean.endsWith("```")) {
            clean = clean.substring(0, clean.length() - 3);
        }
        return clean.trim();
    }

    private String obterValorOriginalCampo(Venda v, String campo) {
        if (campo == null) {
            return "";
        }
        return switch (campo) {
            case "data" -> v.getData() != null ? v.getData().toString() : "";
            case "valor" -> v.getValor() != null ? v.getValor().toPlainString() : "";
            case "forma_pagamento" -> v.getFormaPagamento() != null ? v.getFormaPagamento().name() : "";
            case "forma_entrega" -> v.getFormaEntrega() != null ? v.getFormaEntrega().name() : "";
            case "cliente" -> v.getCliente() != null ? v.getCliente() : "";
            case "produto" -> v.getProduto() != null ? v.getProduto() : "";
            default -> "";
        };
    }
}
