import React, { useState, useEffect } from 'react';
import ModalEditarVenda from './ModalEditarVenda';

export default function VendasRegistradas({ produtos, iaConfigurada, refreshTrigger, onVendaAtualizada }) {
  const [filtros, setFiltros] = useState({
    dataInicio: '',
    dataFim: '',
    cliente: '',
    formaPagamento: '',
    produto: '',
    texto: ''
  });

  const [vendas, setVendas] = useState([]);
  const [totais, setTotais] = useState({
    totalVendas: 0,
    totalDevolucoes: 0,
    totalGeral: 0,
    quantidadeRegistros: 0
  });
  const [carregando, setCarregando] = useState(false);
  const [vendaEmEdicao, setVendaEmEdicao] = useState(null);

  const carregarDados = async () => {
    setCarregando(true);
    const params = new URLSearchParams();
    if (filtros.dataInicio) params.append('dataInicio', filtros.dataInicio);
    if (filtros.dataFim) params.append('dataFim', filtros.dataFim);
    if (filtros.cliente) params.append('cliente', filtros.cliente);
    if (filtros.formaPagamento) params.append('formaPagamento', filtros.formaPagamento);
    if (filtros.produto) params.append('produto', filtros.produto);
    if (filtros.texto) params.append('texto', filtros.texto);

    try {
      const [resVendas, resTotais] = await Promise.all([
        fetch(`http://localhost:8080/api/vendas?${params.toString()}`),
        fetch(`http://localhost:8080/api/vendas/totais?${params.toString()}`)
      ]);

      if (resVendas.ok && resTotais.ok) {
        const dataVendas = await resVendas.json();
        const dataTotais = await resTotais.json();
        setVendas(dataVendas);
        setTotais(dataTotais);
      }
    } catch (err) {
      console.error('Erro ao buscar vendas:', err);
    } finally {
      setCarregando(false);
    }
  };

  useEffect(() => {
    carregarDados();
  }, [filtros, refreshTrigger]);

  const handleLimparFiltros = () => {
    setFiltros({
      dataInicio: '',
      dataFim: '',
      cliente: '',
      formaPagamento: '',
      produto: '',
      texto: ''
    });
  };

  const handleExportarExcel = () => {
    const params = new URLSearchParams();
    if (filtros.dataInicio) params.append('dataInicio', filtros.dataInicio);
    if (filtros.dataFim) params.append('dataFim', filtros.dataFim);
    if (filtros.cliente) params.append('cliente', filtros.cliente);
    if (filtros.formaPagamento) params.append('formaPagamento', filtros.formaPagamento);
    if (filtros.produto) params.append('produto', filtros.produto);
    if (filtros.texto) params.append('texto', filtros.texto);

    window.open(`http://localhost:8080/api/exportacao?${params.toString()}`, '_blank');
  };

  const formatarMoeda = (val) => {
    const num = Number(val) || 0;
    return num.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  };

  const formatarData = (dt) => {
    if (!dt) return '';
    const [ano, mes, dia] = dt.split('-');
    return `${dia}/${mes}/${ano}`;
  };

  const formatarFormaPagamento = (fp) => {
    const map = {
      PIX: 'Pix',
      DINHEIRO: 'Dinheiro',
      DEBITO: 'Débito',
      CREDITO: 'Crédito',
      MAQUININHA: 'Maquininha',
      FIADO: 'Fiado',
      PAGAR_DEPOIS: 'Pagar depois',
      NAO_INFORMADO: 'Não informado'
    };
    return map[fp] || fp;
  };

  const formatarEntrega = (fe) => {
    const map = {
      LOJA: 'Loja',
      PRESENCIAL: 'Presencial',
      CORREIOS: 'Correios',
      SEDEX: 'Sedex',
      MOTOBOY: 'Motoboy',
      RETIRADA: 'Ela veio buscar'
    };
    return map[fe] || fe;
  };

  return (
    <div>
      {/* Cards de Totais */}
      <div className="totais-grid">
        <div className="total-card">
          <div className="total-label">Total de Vendas</div>
          <div className="total-value vendas">
            {formatarMoeda(totais.totalVendas)}
          </div>
          <p className="field-hint" style={{ marginTop: 4 }}>
            {totais.quantidadeRegistros} registro(s) encontrado(s)
          </p>
        </div>

        <div className="total-card">
          <div className="total-label">Total de Devoluções</div>
          <div className="total-value devolucoes">
            {formatarMoeda(totais.totalDevolucoes)}
          </div>
          <p className="field-hint" style={{ marginTop: 4 }}>Itens devolvidos</p>
        </div>

        <div className="total-card">
          <div className="total-label">Total Geral Líquido</div>
          <div className="total-value liquido">
            {formatarMoeda(totais.totalGeral)}
          </div>
          <p className="field-hint" style={{ marginTop: 4 }}>Vendas menos devoluções</p>
        </div>
      </div>

      {/* Barra de Filtros e Exportação */}
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16, flexWrap: 'wrap', gap: 12 }}>
          <h2 className="card-title" style={{ margin: 0 }}>🔍 Filtros e Busca</h2>
          <button className="btn btn-success btn-sm" onClick={handleExportarExcel}>
            📊 Exportar para Excel Padronizado
          </button>
        </div>

        <div className="form-grid">
          <div className="form-group">
            <label className="form-label">De:</label>
            <input
              type="date"
              className="form-input"
              value={filtros.dataInicio}
              onChange={e => setFiltros({ ...filtros, dataInicio: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Até:</label>
            <input
              type="date"
              className="form-input"
              value={filtros.dataFim}
              onChange={e => setFiltros({ ...filtros, dataFim: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Cliente:</label>
            <input
              type="text"
              placeholder="Nome do cliente..."
              className="form-input"
              value={filtros.cliente}
              onChange={e => setFiltros({ ...filtros, cliente: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Forma de Pagamento:</label>
            <select
              className="form-select"
              value={filtros.formaPagamento}
              onChange={e => setFiltros({ ...filtros, formaPagamento: e.target.value })}
            >
              <option value="">Todas</option>
              <option value="PIX">Pix</option>
              <option value="DINHEIRO">Dinheiro</option>
              <option value="DEBITO">Débito</option>
              <option value="CREDITO">Crédito</option>
              <option value="MAQUININHA">Maquininha</option>
              <option value="FIADO">Fiado</option>
              <option value="PAGAR_DEPOIS">Pagar depois</option>
              <option value="NAO_INFORMADO">Não informado</option>
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Produto:</label>
            <select
              className="form-select"
              value={filtros.produto}
              onChange={e => setFiltros({ ...filtros, produto: e.target.value })}
            >
              <option value="">Todos os produtos</option>
              {produtos.map(p => (
                <option key={p.id} value={p.nome}>{p.nome}</option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Busca geral (texto):</label>
            <div style={{ display: 'flex', gap: 8 }}>
              <input
                type="text"
                placeholder="Rastreio, observação, cliente..."
                className="form-input"
                value={filtros.texto}
                onChange={e => setFiltros({ ...filtros, texto: e.target.value })}
              />
              <button type="button" className="btn btn-secondary btn-sm" onClick={handleLimparFiltros}>
                Limpar
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Tabela de Vendas */}
      <div className="card">
        <h3 className="card-title">📋 Lista de Vendas ({vendas.length})</h3>

        {carregando ? (
          <div style={{ padding: 24, textAlign: 'center' }}>
            <span className="spinner" style={{ borderColor: 'rgba(79, 70, 229, 0.3)', borderTopColor: 'var(--primary)' }}></span>
            <p style={{ marginTop: 8, color: 'var(--text-muted)' }}>Carregando vendas...</p>
          </div>
        ) : vendas.length === 0 ? (
          <div style={{ padding: 32, textAlign: 'center', color: 'var(--text-muted)' }}>
            Nenhuma venda encontrada com os filtros selecionados.
          </div>
        ) : (
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Data</th>
                  <th>Produto</th>
                  <th>Cliente</th>
                  <th>Valor</th>
                  <th>Pagamento</th>
                  <th>Entrega</th>
                  <th>Status</th>
                  <th>Ações</th>
                </tr>
              </thead>
              <tbody>
                {vendas.map(v => (
                  <tr key={v.id} className={v.precisaRevisao ? 'row-revisao' : ''}>
                    <td>{formatarData(v.data)}</td>
                    <td>
                      <strong>{v.produto}</strong>
                      {v.tipo === 'COMPRA' && <span className="badge badge-info" style={{ marginLeft: 6 }}>Compra</span>}
                      {v.tipo === 'DEVOLUCAO' && <span className="badge badge-danger" style={{ marginLeft: 6 }}>Devolução</span>}
                    </td>
                    <td>{v.cliente}</td>
                    <td>
                      <strong style={{ color: v.valor < 0 ? 'var(--danger)' : 'var(--text-main)' }}>
                        {formatarMoeda(v.valor)}
                      </strong>
                    </td>
                    <td>
                      {formatarFormaPagamento(v.formaPagamento)}
                      {v.bandeiraCartao && v.bandeiraCartao !== 'NAO_INFORMADO' && ` (${v.bandeiraCartao})`}
                      {v.parcelas > 1 && ` ${v.parcelas}x`}
                    </td>
                    <td>
                      {formatarEntrega(v.formaEntrega)}
                      {v.codigoRastreio && (
                        <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', fontFamily: 'monospace' }}>
                          {v.codigoRastreio}
                        </div>
                      )}
                    </td>
                    <td>
                      {v.precisaRevisao ? (
                        <span className="badge badge-warning" title={v.motivoRevisao}>
                          ⚠️ Precisa conferir
                        </span>
                      ) : (
                        <span className="badge badge-success">OK</span>
                      )}
                      {v.sugestaoIaAceita === true && (
                        <span className="badge badge-info" style={{ marginLeft: 4 }} title="Sugestão da IA foi aceita">
                          ✨ IA
                        </span>
                      )}
                    </td>
                    <td>
                      <button
                        className="btn btn-secondary btn-sm"
                        onClick={() => setVendaEmEdicao(v)}
                      >
                        {v.precisaRevisao ? 'Conferir' : 'Editar'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {vendaEmEdicao && (
        <ModalEditarVenda
          venda={vendaEmEdicao}
          produtos={produtos}
          iaConfigurada={iaConfigurada}
          onClose={() => setVendaEmEdicao(null)}
          onSalvo={(vAtualizada) => {
            carregarDados();
            if (onVendaAtualizada) onVendaAtualizada();
          }}
        />
      )}
    </div>
  );
}

