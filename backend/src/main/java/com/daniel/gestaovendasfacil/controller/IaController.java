package com.daniel.gestaovendasfacil.controller;

import com.daniel.gestaovendasfacil.dto.SugestaoIaRequestDTO;
import com.daniel.gestaovendasfacil.dto.SugestaoIaResponseDTO;
import com.daniel.gestaovendasfacil.service.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ia")
public class IaController {

    private final GeminiService geminiService;

    public IaController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> statusIa() {
        return ResponseEntity.ok(Map.of("configurado", geminiService.isConfigurado()));
    }

    @PostMapping("/sugestao")
    public ResponseEntity<SugestaoIaResponseDTO> pedirSugestao(@RequestBody SugestaoIaRequestDTO request) {
        SugestaoIaResponseDTO response = geminiService.sugerirCorrecao(
                request.getVendaId(),
                request.getLinhaOriginal(),
                request.getCampo(),
                request.getMotivoRevisao()
        );
        return ResponseEntity.ok(response);
    }
}

