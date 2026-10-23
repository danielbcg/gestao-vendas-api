package com.daniel.gestaovendasfacil;

import com.daniel.gestaovendasfacil.dto.ImportacaoRelatorioDTO;
import com.daniel.gestaovendasfacil.repository.VendaRepository;
import com.daniel.gestaovendasfacil.service.ImportacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ImportacaoIntegracaoTest {

    @Autowired
    private ImportacaoService importacaoService;

    @Autowired
    private VendaRepository vendaRepository;

    @BeforeEach
    void setUp() {
        vendaRepository.deleteAll();
    }

    @Test
    @DisplayName("Importação de integração da planilha de exemplo inteira")
    void testImportacaoCompleta() throws Exception {
        File file = new File("../data/vendas_maria.xlsx");
        if (!file.exists()) {
            file = new File("data/vendas_maria.xlsx");
        }
        assertTrue(file.exists(), "Arquivo vendas_maria.xlsx deve existir!");

        ImportacaoRelatorioDTO relatorio;
        try (FileInputStream fis = new FileInputStream(file)) {
            relatorio = importacaoService.importarPlanilha(fis);
        }

        System.out.println("=== RELATÓRIO DE IMPORTAÇÃO ===");
        System.out.println("Total de linhas lidas: " + relatorio.getTotalLinhasLidas());
        System.out.println("Importadas automaticamente: " + relatorio.getImportadasAutomaticamente());
        System.out.println("Importadas com REVISAR: " + relatorio.getImportadasComRevisao().size());
        System.out.println("Ignoradas (duplicadas/vazias): " + relatorio.getIgnoradas().size());

        assertTrue(relatorio.getTotalLinhasLidas() > 0, "Deve ler as linhas da planilha");
        assertTrue(relatorio.getImportadasAutomaticamente() > 0, "Deve importar automaticamente linhas limpas");
        assertFalse(relatorio.getImportadasComRevisao().isEmpty(), "Deve identificar linhas que precisam de revisão");
        assertFalse(relatorio.getIgnoradas().isEmpty(), "Deve detectar linhas duplicadas ignoradas");

        // Conferir que as vendas foram persistidas no repositório
        long totalBanco = vendaRepository.count();
        assertEquals(relatorio.getImportadasAutomaticamente() + relatorio.getImportadasComRevisao().size(), totalBanco);
    }
}

