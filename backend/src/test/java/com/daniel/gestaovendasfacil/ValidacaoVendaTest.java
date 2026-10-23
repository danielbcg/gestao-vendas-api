package com.daniel.gestaovendasfacil;

import com.daniel.gestaovendasfacil.dto.VendaRequestDTO;
import com.daniel.gestaovendasfacil.model.FormaEntrega;
import com.daniel.gestaovendasfacil.model.FormaPagamento;
import com.daniel.gestaovendasfacil.model.TipoVenda;
import com.daniel.gestaovendasfacil.model.Venda;
import com.daniel.gestaovendasfacil.service.VendaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ValidacaoVendaTest {

    @Autowired
    private VendaService vendaService;

    @Test
    @DisplayName("Crédito sem parcelas deve assumir 1 parcela por padrão")
    void testCreditoSemParcelasUsaUma() {
        VendaRequestDTO dto = new VendaRequestDTO();
        dto.setTipo(TipoVenda.VENDA);
        dto.setData(LocalDate.now());
        dto.setProduto("colar de perolas");
        dto.setValor(new BigDecimal("50.00"));
        dto.setFormaPagamento(FormaPagamento.CREDITO);
        dto.setParcelas(null); // Sem parcelas informadas
        dto.setFormaEntrega(FormaEntrega.LOJA);

        Venda vendaCriada = vendaService.criarVenda(dto);
        assertNotNull(vendaCriada.getId());
        assertEquals(1, vendaCriada.getParcelas());
    }

    @Test
    @DisplayName("Correios sem código de rastreio deve lançar erro na API")
    void testCorreiosSemRastreioInvalido() {
        VendaRequestDTO dto = new VendaRequestDTO();
        dto.setTipo(TipoVenda.VENDA);
        dto.setData(LocalDate.now());
        dto.setProduto("pulseira prata");
        dto.setValor(new BigDecimal("60.00"));
        dto.setFormaPagamento(FormaPagamento.PIX);
        dto.setFormaEntrega(FormaEntrega.CORREIOS);
        dto.setCodigoRastreio(null); // Ausente!

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            vendaService.criarVenda(dto);
        });

        assertTrue(ex.getMessage().contains("rastreio é obrigatório"));
    }
}

