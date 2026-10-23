package com.daniel.gestaovendasfacil;

import com.daniel.gestaovendasfacil.model.BandeiraCartao;
import com.daniel.gestaovendasfacil.model.FormaEntrega;
import com.daniel.gestaovendasfacil.model.FormaPagamento;
import com.daniel.gestaovendasfacil.model.TipoVenda;
import com.daniel.gestaovendasfacil.service.normalizer.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class NormalizadoresTest {

    private DateNormalizer dateNormalizer;
    private MoneyNormalizer moneyNormalizer;
    private PaymentNormalizer paymentNormalizer;
    private DeliveryNormalizer deliveryNormalizer;
    private ClientNormalizer clientNormalizer;
    private ProductNormalizer productNormalizer;
    private LineClassifier lineClassifier;

    @BeforeEach
    void setUp() {
        dateNormalizer = new DateNormalizer();
        moneyNormalizer = new MoneyNormalizer();
        paymentNormalizer = new PaymentNormalizer();
        deliveryNormalizer = new DeliveryNormalizer();
        clientNormalizer = new ClientNormalizer();
        productNormalizer = new ProductNormalizer(new ObjectMapper());
        productNormalizer.carregarSinonimos();
        lineClassifier = new LineClassifier();
    }

    @Test
    @DisplayName("Normalização de data: 8/16/26, hj, ontem")
    void testDateNormalizer() {
        // "8/16/26" -> 16 de Agosto de 2026
        NormalizationResult<LocalDate> res1 = dateNormalizer.normalize("8/16/26");
        assertFalse(res1.isPrecisaRevisao());
        assertEquals(LocalDate.of(2026, 8, 16), res1.getValue());

        // "hj" -> hoje
        NormalizationResult<LocalDate> resHj = dateNormalizer.normalize("hj");
        assertFalse(resHj.isPrecisaRevisao());
        assertEquals(LocalDate.now(), resHj.getValue());

        // "ontem" -> hoje - 1 dia
        NormalizationResult<LocalDate> resOntem = dateNormalizer.normalize("ontem");
        assertFalse(resOntem.isPrecisaRevisao());
        assertEquals(LocalDate.now().minusDays(1), resOntem.getValue());
    }

    @Test
    @DisplayName("Normalização de valores: R$ 35, 28,00, 35+12 frete, -40")
    void testMoneyNormalizer() {
        // "R$ 35"
        NormalizationResult<BigDecimal> res1 = moneyNormalizer.normalize("R$ 35");
        assertFalse(res1.isPrecisaRevisao());
        assertEquals(new BigDecimal("35.00"), res1.getValue());

        // "28,00"
        NormalizationResult<BigDecimal> res2 = moneyNormalizer.normalize("28,00");
        assertFalse(res2.isPrecisaRevisao());
        assertEquals(new BigDecimal("28.00"), res2.getValue());

        // "35+12 frete" -> somar (47.00) e marcar REVISAR com possível frete incluído
        NormalizationResult<BigDecimal> resSoma = moneyNormalizer.normalize("35+12 frete");
        assertTrue(resSoma.isPrecisaRevisao());
        assertEquals(new BigDecimal("47.00"), resSoma.getValue());
        assertEquals("possível frete incluído", resSoma.getMotivoRevisao());

        // "-40" (devolução)
        NormalizationResult<BigDecimal> resDevolucao = moneyNormalizer.normalize("-40");
        assertFalse(resDevolucao.isPrecisaRevisao());
        assertEquals(new BigDecimal("-40.00"), resDevolucao.getValue());
    }

    @Test
    @DisplayName("Normalização de pagamentos: credito 3x visa, maquinha, depois paga")
    void testPaymentNormalizer() {
        // "credito 3x visa"
        PaymentResult res1 = paymentNormalizer.normalize("credito 3x visa", "", "");
        assertFalse(res1.isPrecisaRevisao());
        assertEquals(FormaPagamento.CREDITO, res1.getFormaPagamento());
        assertEquals(3, res1.getParcelas());
        assertEquals(BandeiraCartao.VISA, res1.getBandeiraCartao());

        // "maquinha"
        PaymentResult res2 = paymentNormalizer.normalize("maquinha", "visa", "3");
        assertFalse(res2.isPrecisaRevisao());
        assertEquals(FormaPagamento.MAQUININHA, res2.getFormaPagamento());
        assertEquals(3, res2.getParcelas());
        assertEquals(BandeiraCartao.VISA, res2.getBandeiraCartao());

        // "depois paga"
        PaymentResult res3 = paymentNormalizer.normalize("depois paga", "", "");
        assertFalse(res3.isPrecisaRevisao());
        assertEquals(FormaPagamento.PAGAR_DEPOIS, res3.getFormaPagamento());
    }

    @Test
    @DisplayName("Normalização de entrega: ela veio buscar")
    void testDeliveryNormalizer() {
        DeliveryResult res = deliveryNormalizer.normalize("ela veio buscar", null, null);
        assertFalse(res.isPrecisaRevisao());
        assertEquals(FormaEntrega.RETIRADA, res.getFormaEntrega());
    }

    @Test
    @DisplayName("Normalização de cliente: telefone em cliente e código de rastreio em cliente")
    void testClientNormalizer() {
        // Telefone em cliente: 31988887777
        ClientResult resTelefone = clientNormalizer.normalize("31988887777", "nao anotei o nome");
        assertTrue(resTelefone.isPrecisaRevisao());
        assertEquals("Não informado", resTelefone.getCliente());
        assertTrue(resTelefone.getObservacaoAtualizada().contains("31988887777"));
        assertEquals("Telefone informado na coluna de cliente", resTelefone.getMotivoRevisao());

        // Código de rastreio em cliente com "era a Juliana" na obs
        ClientResult resRastreio = clientNormalizer.normalize("AA998877665BR", "era a Juliana");
        assertTrue(resRastreio.isPrecisaRevisao());
        assertEquals("Juliana", resRastreio.getCliente());
        assertEquals("AA998877665BR", resRastreio.getCodigoRastreioExtraido());
        assertEquals("Código de rastreio informado na coluna de cliente", resRastreio.getMotivoRevisao());
    }

    @Test
    @DisplayName("Normalização de produto: brinc argola -> brinco argola")
    void testProductNormalizer() {
        String prod = productNormalizer.normalize("brinc argola");
        assertEquals("brinco argola", prod);

        String prodArgola = productNormalizer.normalize("argola dourada");
        assertEquals("argola dourada", prodArgola);
    }

    @Test
    @DisplayName("Classificação de tipo de linha: compra, devolução e venda")
    void testLineClassifier() {
        TipoVenda tipoCompra = lineClassifier.classify("comprei 20 argolas no fornecedor", new BigDecimal("200.00"));
        assertEquals(TipoVenda.COMPRA, tipoCompra);

        TipoVenda tipoDevolucao = lineClassifier.classify("devolveu pulseira", new BigDecimal("-40.00"));
        assertEquals(TipoVenda.DEVOLUCAO, tipoDevolucao);

        TipoVenda tipoVenda = lineClassifier.classify("brinco argola", new BigDecimal("35.00"));
        assertEquals(TipoVenda.VENDA, tipoVenda);
    }
}

