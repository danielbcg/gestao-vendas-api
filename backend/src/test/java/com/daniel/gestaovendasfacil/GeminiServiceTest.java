package com.daniel.gestaovendasfacil;

import com.daniel.gestaovendasfacil.dto.SugestaoIaResponseDTO;
import com.daniel.gestaovendasfacil.repository.VendaRepository;
import com.daniel.gestaovendasfacil.service.GeminiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

public class GeminiServiceTest {

    private GeminiService geminiService;

    @BeforeEach
    void setUp() {
        VendaRepository vendaRepository = Mockito.mock(VendaRepository.class);
        geminiService = new GeminiService("chave-fake", "gemini-flash-lite-latest", vendaRepository, new ObjectMapper());
    }

    @Test
    @DisplayName("JSON válido retornado pela IA é aceito com sucesso")
    void testJsonValidoAceito() {
        String jsonValido = """
                ```json
                {
                  "campo": "forma_pagamento",
                  "valorSugerido": "PIX",
                  "explicacao": "Na coluna de observação constava que o cliente pagou por chave Pix."
                }
                ```
                """;

        SugestaoIaResponseDTO response = geminiService.validarEParsarResposta(jsonValido, "maquinha");
        assertTrue(response.isDisponivel());
        assertEquals("forma_pagamento", response.getCampo());
        assertEquals("PIX", response.getValorSugerido());
        assertEquals("maquinha", response.getValorOriginal());
        assertTrue(response.getExplicacao().contains("chave Pix"));
    }

    @Test
    @DisplayName("JSON com campo desconhecido ou tipo inválido é descartado")
    void testJsonInvalidoDescartado() {
        // Campo inválido
        String jsonCampoInvalido = """
                {
                  "campo": "campo_inexistente",
                  "valorSugerido": "qualquer",
                  "explicacao": "tentativa invalida"
                }
                """;
        SugestaoIaResponseDTO res1 = geminiService.validarEParsarResposta(jsonCampoInvalido, "original");
        assertFalse(res1.isDisponivel());
        assertEquals("A sugestão não está disponível agora. Você pode preencher a venda manualmente.", res1.getMensagem());

        // Enum de forma de entrega inexistente
        String jsonEnumInvalido = """
                {
                  "campo": "forma_entrega",
                  "valorSugerido": "TELETRANSPORTE",
                  "explicacao": "invalido"
                }
                """;
        SugestaoIaResponseDTO res2 = geminiService.validarEParsarResposta(jsonEnumInvalido, "original");
        assertFalse(res2.isDisponivel());

        // Texto que nem é JSON
        String textoNaoJson = "Olá, acho que você deveria colocar PIX.";
        SugestaoIaResponseDTO res3 = geminiService.validarEParsarResposta(textoNaoJson, "original");
        assertFalse(res3.isDisponivel());

        // Data em formato inválido
        String jsonDateInvalida = """
                {
                  "campo": "data",
                  "valorSugerido": "32/13/2026",
                  "explicacao": "data incorreta"
                }
                """;
        SugestaoIaResponseDTO res4 = geminiService.validarEParsarResposta(jsonDateInvalida, "original");
        assertFalse(res4.isDisponivel());

        // Valor não numérico
        String jsonValorInvalido = """
                {
                  "campo": "valor",
                  "valorSugerido": "cem_reais",
                  "explicacao": "valor nao numerico"
                }
                """;
        SugestaoIaResponseDTO res5 = geminiService.validarEParsarResposta(jsonValorInvalido, "original");
        assertFalse(res5.isDisponivel());
    }
}
