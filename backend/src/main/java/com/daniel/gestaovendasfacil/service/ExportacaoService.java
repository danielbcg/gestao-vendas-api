package com.daniel.gestaovendasfacil.service;

import com.daniel.gestaovendasfacil.model.FormaPagamento;
import com.daniel.gestaovendasfacil.model.Venda;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportacaoService {

    private final VendaService vendaService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ExportacaoService(VendaService vendaService) {
        this.vendaService = vendaService;
    }

    public byte[] exportarParaExcel(
            LocalDate dataInicio,
            LocalDate dataFim,
            String cliente,
            FormaPagamento formaPagamento,
            String produto,
            String texto
    ) throws IOException {
        List<Venda> vendas = vendaService.listarComFiltros(dataInicio, dataFim, cliente, formaPagamento, produto, texto);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Vendas Padronizadas");

            // Estilo do cabeçalho
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.TEAL.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

            // Cabeçalhos (originais + novos)
            String[] headers = {
                    "data", "oq vendeu", "pra quem", "valor", "como pagou", "cartao",
                    "vezes", "entrega ou loja", "rastreio", "obs",
                    "tipo", "parcelas", "bandeira", "origem", "precisa_revisao", "motivo_revisao"
            };

            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowIdx = 1;
            for (Venda v : vendas) {
                Row row = sheet.createRow(rowIdx++);

                // Colunas originais mapeadas
                row.createCell(0).setCellValue(v.getData() != null ? v.getData().format(DATE_FORMATTER) : "");
                row.createCell(1).setCellValue(v.getProduto() != null ? v.getProduto() : "");
                row.createCell(2).setCellValue(v.getCliente() != null ? v.getCliente() : "");
                row.createCell(3).setCellValue(v.getValor() != null ? v.getValor().doubleValue() : 0.0);
                row.createCell(4).setCellValue(v.getFormaPagamento() != null ? v.getFormaPagamento().name() : "");
                row.createCell(5).setCellValue(v.getBandeiraCartao() != null ? v.getBandeiraCartao().name() : "");
                row.createCell(6).setCellValue(v.getParcelas() != null ? v.getParcelas() : 1);
                row.createCell(7).setCellValue(v.getFormaEntrega() != null ? v.getFormaEntrega().name() : "");
                row.createCell(8).setCellValue(v.getCodigoRastreio() != null ? v.getCodigoRastreio() : "");
                row.createCell(9).setCellValue(v.getObservacao() != null ? v.getObservacao() : "");

                // Colunas novas
                row.createCell(10).setCellValue(v.getTipo() != null ? v.getTipo().name() : "");
                row.createCell(11).setCellValue(v.getParcelas() != null ? v.getParcelas() : 1);
                row.createCell(12).setCellValue(v.getBandeiraCartao() != null ? v.getBandeiraCartao().name() : "");
                row.createCell(13).setCellValue(v.getOrigem() != null ? v.getOrigem().name() : "");
                row.createCell(14).setCellValue(v.isPrecisaRevisao() ? "SIM" : "NAO");
                row.createCell(15).setCellValue(v.getMotivoRevisao() != null ? v.getMotivoRevisao() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}

