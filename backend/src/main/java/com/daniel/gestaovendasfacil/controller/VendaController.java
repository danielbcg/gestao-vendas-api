package com.daniel.gestaovendasfacil.controller;

import com.daniel.gestaovendasfacil.dto.TotaisVendaDTO;
import com.daniel.gestaovendasfacil.dto.VendaRequestDTO;
import com.daniel.gestaovendasfacil.model.FormaPagamento;
import com.daniel.gestaovendasfacil.model.Venda;
import com.daniel.gestaovendasfacil.service.ExportacaoService;
import com.daniel.gestaovendasfacil.service.VendaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class VendaController {

    private final VendaService vendaService;
    private final ExportacaoService exportacaoService;

    public VendaController(VendaService vendaService, ExportacaoService exportacaoService) {
        this.vendaService = vendaService;
        this.exportacaoService = exportacaoService;
    }

    @GetMapping("/vendas")
    public ResponseEntity<List<Venda>> listarVendas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) FormaPagamento formaPagamento,
            @RequestParam(required = false) String produto,
            @RequestParam(required = false) String texto
    ) {
        List<Venda> vendas = vendaService.listarComFiltros(dataInicio, dataFim, cliente, formaPagamento, produto, texto);
        return ResponseEntity.ok(vendas);
    }

    @GetMapping("/vendas/{id}")
    public ResponseEntity<Venda> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(vendaService.buscarPorId(id));
    }

    @PostMapping("/vendas")
    public ResponseEntity<Venda> criarVenda(@Valid @RequestBody VendaRequestDTO dto) {
        Venda novaVenda = vendaService.criarVenda(dto);
        return ResponseEntity.ok(novaVenda);
    }

    @PutMapping("/vendas/{id}")
    public ResponseEntity<Venda> editarVenda(@PathVariable Long id, @Valid @RequestBody VendaRequestDTO dto) {
        Venda vendaAtualizada = vendaService.editarVenda(id, dto);
        return ResponseEntity.ok(vendaAtualizada);
    }

    @GetMapping("/vendas/totais")
    public ResponseEntity<TotaisVendaDTO> calcularTotais(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) FormaPagamento formaPagamento,
            @RequestParam(required = false) String produto,
            @RequestParam(required = false) String texto
    ) {
        TotaisVendaDTO totais = vendaService.calcularTotais(dataInicio, dataFim, cliente, formaPagamento, produto, texto);
        return ResponseEntity.ok(totais);
    }

    @GetMapping("/exportacao")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) FormaPagamento formaPagamento,
            @RequestParam(required = false) String produto,
            @RequestParam(required = false) String texto
    ) throws IOException {
        byte[] arquivoBytes = exportacaoService.exportarParaExcel(dataInicio, dataFim, cliente, formaPagamento, produto, texto);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "vendas_padronizadas.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(arquivoBytes);
    }
}

