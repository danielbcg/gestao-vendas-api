package com.daniel.gestaovendasfacil.service;

import com.daniel.gestaovendasfacil.dto.TotaisVendaDTO;
import com.daniel.gestaovendasfacil.dto.VendaRequestDTO;
import com.daniel.gestaovendasfacil.model.FormaEntrega;
import com.daniel.gestaovendasfacil.model.FormaPagamento;
import com.daniel.gestaovendasfacil.model.OrigemVenda;
import com.daniel.gestaovendasfacil.model.TipoVenda;
import com.daniel.gestaovendasfacil.model.Venda;
import com.daniel.gestaovendasfacil.repository.VendaRepository;
import com.daniel.gestaovendasfacil.repository.VendaSpecification;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class VendaService {

    private final VendaRepository vendaRepository;
    private final ProdutoService produtoService;

    public VendaService(VendaRepository vendaRepository, ProdutoService produtoService) {
        this.vendaRepository = vendaRepository;
        this.produtoService = produtoService;
    }

    public List<Venda> listarComFiltros(
            LocalDate dataInicio,
            LocalDate dataFim,
            String cliente,
            FormaPagamento formaPagamento,
            String produto,
            String texto
    ) {
        Specification<Venda> spec = VendaSpecification.comFiltros(dataInicio, dataFim, cliente, formaPagamento, produto, texto);
        return vendaRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "data", "id"));
    }

    public Venda buscarPorId(Long id) {
        return vendaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Venda com ID " + id + " não encontrada."));
    }

    @Transactional
    public Venda criarVenda(VendaRequestDTO dto) {
        validarRegrasNegocio(dto);

        String produtoNormalizado = produtoService.salvarOuObter(dto.getProduto()).getNome();

        Venda venda = new Venda();
        venda.setTipo(dto.getTipo() != null ? dto.getTipo() : TipoVenda.VENDA);
        venda.setData(dto.getData());
        venda.setProduto(produtoNormalizado);
        venda.setValor(dto.getValor());
        venda.setCliente(dto.getCliente());
        venda.setFormaPagamento(dto.getFormaPagamento());
        venda.setBandeiraCartao(dto.getBandeiraCartao());

        // Regra: crédito sem parcelas usa 1
        if (dto.getFormaPagamento() == FormaPagamento.CREDITO) {
            venda.setParcelas(dto.getParcelas() != null && dto.getParcelas() > 0 ? dto.getParcelas() : 1);
        } else {
            venda.setParcelas(dto.getParcelas());
        }

        venda.setFormaEntrega(dto.getFormaEntrega());
        venda.setCodigoRastreio(dto.getCodigoRastreio());
        venda.setObservacao(dto.getObservacao());
        venda.setOrigem(OrigemVenda.FORMULARIO);
        venda.setPrecisaRevisao(dto.getPrecisaRevisao() != null ? dto.getPrecisaRevisao() : false);
        venda.setMotivoRevisao(dto.getMotivoRevisao());
        venda.setSugestaoIaAceita(dto.getSugestaoIaAceita());

        return vendaRepository.save(venda);
    }

    @Transactional
    public Venda editarVenda(Long id, VendaRequestDTO dto) {
        validarRegrasNegocio(dto);

        Venda venda = buscarPorId(id);
        String produtoNormalizado = produtoService.salvarOuObter(dto.getProduto()).getNome();

        venda.setTipo(dto.getTipo() != null ? dto.getTipo() : TipoVenda.VENDA);
        venda.setData(dto.getData());
        venda.setProduto(produtoNormalizado);
        venda.setValor(dto.getValor());
        venda.setCliente(dto.getCliente());
        venda.setFormaPagamento(dto.getFormaPagamento());
        venda.setBandeiraCartao(dto.getBandeiraCartao());

        if (dto.getFormaPagamento() == FormaPagamento.CREDITO) {
            venda.setParcelas(dto.getParcelas() != null && dto.getParcelas() > 0 ? dto.getParcelas() : 1);
        } else {
            venda.setParcelas(dto.getParcelas());
        }

        venda.setFormaEntrega(dto.getFormaEntrega());
        venda.setCodigoRastreio(dto.getCodigoRastreio());
        venda.setObservacao(dto.getObservacao());

        // Ao editar para corrigir, se precisaRevisao não for especificado como true, consideramos resolvido
        if (dto.getPrecisaRevisao() != null) {
            venda.setPrecisaRevisao(dto.getPrecisaRevisao());
            venda.setMotivoRevisao(dto.getMotivoRevisao());
        } else {
            venda.setPrecisaRevisao(false);
            venda.setMotivoRevisao(null);
        }

        if (dto.getSugestaoIaAceita() != null) {
            venda.setSugestaoIaAceita(dto.getSugestaoIaAceita());
        }

        return vendaRepository.save(venda);
    }

    public TotaisVendaDTO calcularTotais(
            LocalDate dataInicio,
            LocalDate dataFim,
            String cliente,
            FormaPagamento formaPagamento,
            String produto,
            String texto
    ) {
        List<Venda> vendas = listarComFiltros(dataInicio, dataFim, cliente, formaPagamento, produto, texto);

        BigDecimal totalVendas = BigDecimal.ZERO;
        BigDecimal totalDevolucoes = BigDecimal.ZERO;

        for (Venda v : vendas) {
            if (v.getTipo() == TipoVenda.VENDA) {
                totalVendas = totalVendas.add(v.getValor());
            } else if (v.getTipo() == TipoVenda.DEVOLUCAO) {
                totalDevolucoes = totalDevolucoes.add(v.getValor().abs());
            }
        }

        BigDecimal totalGeral = totalVendas.subtract(totalDevolucoes);
        return new TotaisVendaDTO(totalVendas, totalDevolucoes, totalGeral, vendas.size());
    }

    private void validarRegrasNegocio(VendaRequestDTO dto) {
        if (dto.getValor() != null && dto.getValor().compareTo(BigDecimal.ZERO) < 0 && dto.getTipo() != TipoVenda.DEVOLUCAO) {
            throw new IllegalArgumentException("Valores negativos são permitidos apenas para devoluções.");
        }

        if ((dto.getFormaEntrega() == FormaEntrega.CORREIOS || dto.getFormaEntrega() == FormaEntrega.SEDEX)
                && (dto.getCodigoRastreio() == null || dto.getCodigoRastreio().trim().isEmpty())) {
            throw new IllegalArgumentException("Código de rastreio é obrigatório para entregas via Correios ou Sedex.");
        }
    }
}

