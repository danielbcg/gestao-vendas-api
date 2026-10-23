package com.daniel.gestaovendasfacil.controller;

import com.daniel.gestaovendasfacil.dto.ImportacaoRelatorioDTO;
import com.daniel.gestaovendasfacil.service.ImportacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class ImportacaoController {

    private final ImportacaoService importacaoService;

    public ImportacaoController(ImportacaoService importacaoService) {
        this.importacaoService = importacaoService;
    }

    @PostMapping("/importacao")
    public ResponseEntity<ImportacaoRelatorioDTO> importarPlanilha(@RequestParam("arquivo") MultipartFile arquivo) throws Exception {
        if (arquivo.isEmpty()) {
            throw new IllegalArgumentException("O arquivo enviado está vazio.");
        }
        ImportacaoRelatorioDTO relatorio = importacaoService.importarPlanilha(arquivo.getInputStream());
        return ResponseEntity.ok(relatorio);
    }
}

