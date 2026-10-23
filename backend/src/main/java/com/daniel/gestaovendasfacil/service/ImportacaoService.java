package com.daniel.gestaovendasfacil.service;

import com.daniel.gestaovendasfacil.dto.ImportacaoRelatorioDTO;
import com.daniel.gestaovendasfacil.dto.ItemIgnoradoDTO;
import com.daniel.gestaovendasfacil.dto.ItemRevisaoDTO;
import com.daniel.gestaovendasfacil.model.*;
import com.daniel.gestaovendasfacil.repository.VendaRepository;
import com.daniel.gestaovendasfacil.service.normalizer.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class ImportacaoService {

    private final VendaRepository vendaRepository;
    private final ProdutoService produtoService;
    private final DateNormalizer dateNormalizer;
    private final MoneyNormalizer moneyNormalizer;
    private final PaymentNormalizer paymentNormalizer;
    private final DeliveryNormalizer deliveryNormalizer;
    private final ClientNormalizer clientNormalizer;
    private final ProductNormalizer productNormalizer;
    private final LineClassifier lineClassifier;

    public ImportacaoService(
            VendaRepository vendaRepository,
            ProdutoService produtoService,
            DateNormalizer dateNormalizer,
            MoneyNormalizer moneyNormalizer,
            PaymentNormalizer paymentNormalizer,
            DeliveryNormalizer deliveryNormalizer,
            ClientNormalizer clientNormalizer,
            ProductNormalizer productNormalizer,
            LineClassifier lineClassifier
    ) {
        this.vendaRepository = vendaRepository;
        this.produtoService = produtoService;
        this.dateNormalizer = dateNormalizer;
        this.moneyNormalizer = moneyNormalizer;
        this.paymentNormalizer = paymentNormalizer;
        this.deliveryNormalizer = deliveryNormalizer;
        this.clientNormalizer = clientNormalizer;
        this.productNormalizer = productNormalizer;
        this.lineClassifier = lineClassifier;
    }

    @Transactional
    public ImportacaoRelatorioDTO importarPlanilha(InputStream inputStream) throws Exception {
        ImportacaoRelatorioDTO relatorio = new ImportacaoRelatorioDTO();
        Set<String> chavesUnicas = new HashSet<>();
        DataFormatter formatter = new DataFormatter();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();

            for (int r = 1; r <= lastRowNum; r++) {
                Row row = sheet.getRow(r);
                if (row == null || isLinhaVazia(row, formatter)) {
                    // Linha totalmente vazia: ignorar silenciosamente
                    continue;
                }

                relatorio.setTotalLinhasLidas(relatorio.getTotalLinhasLidas() + 1);

                String rawData = getCellValue(row, 0, formatter);
                String rawProduto = getCellValue(row, 1, formatter);
                String rawCliente = getCellValue(row, 2, formatter);
                String rawValor = getCellValue(row, 3, formatter);
                String rawComoPagou = getCellValue(row, 4, formatter);
                String rawCartao = getCellValue(row, 5, formatter);
                String rawVezes = getCellValue(row, 6, formatter);
                String rawEntrega = getCellValue(row, 7, formatter);
                String rawRastreio = getCellValue(row, 8, formatter);
                String rawObs = getCellValue(row, 9, formatter);

                // Montar texto completo da linha para classificação
                String fullLineText = String.join(" ", rawData, rawProduto, rawCliente, rawValor, rawComoPagou, rawCartao, rawVezes, rawEntrega, rawRastreio, rawObs);

                // 1. Normalizar Valor
                NormalizationResult<BigDecimal> resValor = moneyNormalizer.normalize(rawValor);
                BigDecimal valor = resValor.getValue();

                // 2. Classificar Tipo
                TipoVenda tipo = lineClassifier.classify(fullLineText, valor);

                // 3. Normalizar Produto e atualizar catálogo
                String produtoNormalizado = productNormalizer.normalize(rawProduto);
                produtoService.salvarOuObter(produtoNormalizado);

                // 4. Normalizar Cliente e Obs (checar rastreio e telefone)
                ClientResult resCliente = clientNormalizer.normalize(rawCliente, rawObs);
                String clienteFinal = resCliente.getCliente();
                String obsFinal = resCliente.getObservacaoAtualizada();

                // 5. Checar código de rastreio em cliente ou obs caso coluna rastreio esteja vazia
                String trackingFound = resCliente.getCodigoRastreioExtraido();
                if (trackingFound == null) {
                    trackingFound = DeliveryNormalizer.extractTrackingCode(obsFinal);
                }

                // 6. Normalizar Entrega e Rastreio
                DeliveryResult resEntrega = deliveryNormalizer.normalize(rawEntrega, rawRastreio, trackingFound);

                // 7. Normalizar Data
                NormalizationResult<LocalDate> resData = dateNormalizer.normalize(rawData);

                // 8. Normalizar Pagamento
                PaymentResult resPagamento = paymentNormalizer.normalize(rawComoPagou, rawCartao, rawVezes);

                // 9. Verificar Duplicata
                // Chave única: mesma data, produto, cliente, valor e forma de pagamento
                String duplicataKey = resData.getValue() + "|" + produtoNormalizado.toLowerCase() + "|" +
                        clienteFinal.toLowerCase() + "|" + valor.toPlainString() + "|" + resPagamento.getFormaPagamento().name();

                if (chavesUnicas.contains(duplicataKey)) {
                    relatorio.getIgnoradas().add(new ItemIgnoradoDTO(
                            r + 1,
                            "Linha duplicada (mesma data, produto, cliente, valor e forma de pagamento)"
                    ));
                    continue;
                }
                chavesUnicas.add(duplicataKey);

                // 10. Consolidar se precisa revisão
                boolean precisaRevisao = false;
                List<String> motivos = new ArrayList<>();
                String campoPrincipalRevisao = null;
                String valorOriginalRevisao = null;

                if (resData.isPrecisaRevisao()) {
                    precisaRevisao = true;
                    motivos.add(resData.getMotivoRevisao());
                    if (campoPrincipalRevisao == null) {
                        campoPrincipalRevisao = "data";
                        valorOriginalRevisao = rawData;
                    }
                }
                if (resValor.isPrecisaRevisao()) {
                    precisaRevisao = true;
                    motivos.add(resValor.getMotivoRevisao());
                    if (campoPrincipalRevisao == null) {
                        campoPrincipalRevisao = "valor";
                        valorOriginalRevisao = rawValor;
                    }
                }
                if (resCliente.isPrecisaRevisao()) {
                    precisaRevisao = true;
                    motivos.add(resCliente.getMotivoRevisao());
                    if (campoPrincipalRevisao == null) {
                        campoPrincipalRevisao = "cliente";
                        valorOriginalRevisao = rawCliente;
                    }
                }
                if (resPagamento.isPrecisaRevisao()) {
                    precisaRevisao = true;
                    motivos.add(resPagamento.getMotivoRevisao());
                    if (campoPrincipalRevisao == null) {
                        campoPrincipalRevisao = "forma_pagamento";
                        valorOriginalRevisao = rawComoPagou;
                    }
                }
                if (resEntrega.isPrecisaRevisao()) {
                    precisaRevisao = true;
                    motivos.add(resEntrega.getMotivoRevisao());
                    if (campoPrincipalRevisao == null) {
                        campoPrincipalRevisao = "forma_entrega";
                        valorOriginalRevisao = rawEntrega;
                    }
                }

                // 11. Instanciar e salvar Venda
                Venda venda = new Venda();
                venda.setTipo(tipo);
                venda.setData(resData.getValue());
                venda.setProduto(produtoNormalizado);
                venda.setValor(valor);
                venda.setCliente(clienteFinal);
                venda.setFormaPagamento(resPagamento.getFormaPagamento());
                venda.setBandeiraCartao(resPagamento.getBandeiraCartao());
                venda.setParcelas(resPagamento.getParcelas());
                venda.setFormaEntrega(resEntrega.getFormaEntrega());
                venda.setCodigoRastreio(resEntrega.getCodigoRastreio());
                venda.setObservacao(obsFinal.isEmpty() ? null : obsFinal);
                venda.setOrigem(OrigemVenda.IMPORTACAO);
                venda.setLinhaOriginal(r + 1); // 1-based correspondente à linha do Excel
                venda.setPrecisaRevisao(precisaRevisao);
                venda.setMotivoRevisao(precisaRevisao ? String.join("; ", motivos) : null);

                venda = vendaRepository.save(venda);

                if (precisaRevisao) {
                    relatorio.getImportadasComRevisao().add(new ItemRevisaoDTO(
                            r + 1,
                            campoPrincipalRevisao != null ? campoPrincipalRevisao : "geral",
                            valorOriginalRevisao != null ? valorOriginalRevisao : "",
                            String.join("; ", motivos),
                            venda.getId()
                    ));
                } else {
                    relatorio.setImportadasAutomaticamente(relatorio.getImportadasAutomaticamente() + 1);
                }
            }
        }

        return relatorio;
    }

    private boolean isLinhaVazia(Row row, DataFormatter formatter) {
        for (int c = 0; c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            String val = formatter.formatCellValue(cell);
            if (val != null && !val.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String getCellValue(Row row, int index, DataFormatter formatter) {
        Cell cell = row.getCell(index);
        if (cell == null) {
            return "";
        }
        return formatter.formatCellValue(cell).trim();
    }
}

