import React, { useState } from 'react';

export default function NovaVenda({ produtos, onProdutoCriado, onVendaSalva }) {
  const getHojeStr = () => new Date().toISOString().split('T')[0];

  const estadoInicial = {
    tipo: 'VENDA',
    data: getHojeStr(),
    produtoSelecionado: '',
    novoProdutoNome: '',
    valor: '',
    cliente: '',
    formaPagamento: 'PIX',
    bandeiraCartao: 'NAO_INFORMADO',
    parcelas: '1',
    formaEntrega: 'LOJA',
    codigoRastreio: '',
    observacao: ''
  };

  const [form, setForm] = useState(estadoInicial);
  const [isNovoProduto, setIsNovoProduto] = useState(false);
  const [salvando, setSalvando] = useState(false);
  const [mensagemSucesso, setMensagemSucesso] = useState(null);
  const [erros, setErros] = useState({});

  const ehCartao = ['DEBITO', 'CREDITO', 'MAQUININHA'].includes(form.formaPagamento);
  const ehEntregaComRastreio = ['CORREIOS', 'SEDEX'].includes(form.formaEntrega);

  const handleProdutoChange = (e) => {
    const val = e.target.value;
    if (val === '__NOVO__') {
      setIsNovoProduto(true);
      setForm({ ...form, produtoSelecionado: '' });
    } else {
      setIsNovoProduto(false);
      setForm({ ...form, produtoSelecionado: val, novoProdutoNome: '' });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSalvando(true);
    setMensagemSucesso(null);
    setErros({});

    const produtoFinal = isNovoProduto ? form.novoProdutoNome.trim() : form.produtoSelecionado;

    // Validações básicas no cliente
    const novosErros = {};
    if (!produtoFinal) {
      novosErros.produto = 'Por favor, selecione ou digite o nome do produto.';
    }
    if (!form.valor) {
      novosErros.valor = 'Por favor, informe o valor.';
    }
    if (ehEntregaComRastreio && !form.codigoRastreio.trim()) {
      novosErros.codigoRastreio = 'O código de rastreio é obrigatório para entregas via Correios ou Sedex.';
    }

    if (Object.keys(novosErros).length > 0) {
      setErros(novosErros);
      setSalvando(false);
      return;
    }

    const payload = {
      tipo: form.tipo,
      data: form.data,
      produto: produtoFinal,
      valor: parseFloat(form.valor),
      cliente: form.cliente.trim() || 'Não informado',
      formaPagamento: form.formaPagamento,
      bandeiraCartao: ehCartao ? form.bandeiraCartao : null,
      parcelas: form.formaPagamento === 'CREDITO' ? (parseInt(form.parcelas, 10) || 1) : null,
      formaEntrega: form.formaEntrega,
      codigoRastreio: ehEntregaComRastreio ? form.codigoRastreio.trim() : null,
      observacao: form.observacao.trim() || null,
      precisaRevisao: false
    };

    try {
      const res = await fetch('http://localhost:8080/api/vendas', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (!res.ok) {
        const errorData = await res.json();
        if (errorData.erros && errorData.erros.length > 0) {
          const map = {};
          errorData.erros.forEach(item => {
            map[item.campo] = item.motivo;
          });
          setErros(map);
        } else {
          setErros({ geral: errorData.mensagem || 'Erro ao registrar a venda.' });
        }
        setSalvando(false);
        return;
      }

      const vendaSalva = await res.json();
      setMensagemSucesso('🎉 Venda registrada com sucesso!');
      setForm(estadoInicial);
      setIsNovoProduto(false);
      onVendaSalva();
      if (isNovoProduto) {
        onProdutoCriado();
      }
    } catch (err) {
      setErros({ geral: 'Não foi possível conectar ao servidor. Verifique se o backend está ligado.' });
    } finally {
      setSalvando(false);
    }
  };

  return (
    <div className="card">
      <h2 className="card-title">🛍️ Registrar Nova Venda</h2>
      <p className="brand-subtitle" style={{ marginBottom: 20 }}>
        Preencha o formulário simples para adicionar uma nova venda à sua loja.
      </p>

      {mensagemSucesso && (
        <div className="alert alert-success">
          {mensagemSucesso}
        </div>
      )}

      {erros.geral && (
        <div className="alert alert-error">
          {erros.geral}
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="form-grid">
          {/* Tipo */}
          <div className="form-group">
            <label className="form-label">Tipo de Registro *</label>
            <select
              className="form-select"
              value={form.tipo}
              onChange={e => setForm({ ...form, tipo: e.target.value })}
            >
              <option value="VENDA">Venda</option>
              <option value="COMPRA">Compra (Estoque)</option>
              <option value="DEVOLUCAO">Devolução</option>
            </select>
          </div>

          {/* Data */}
          <div className="form-group">
            <label className="form-label">Data da Venda *</label>
            <input
              type="date"
              className={`form-input ${erros.data ? 'has-error' : ''}`}
              value={form.data}
              onChange={e => setForm({ ...form, data: e.target.value })}
              required
            />
            {erros.data && <span className="field-error">{erros.data}</span>}
          </div>

          {/* Produto */}
          <div className="form-group">
            <label className="form-label">Produto *</label>
            {!isNovoProduto ? (
              <select
                className={`form-select ${erros.produto ? 'has-error' : ''}`}
                value={form.produtoSelecionado}
                onChange={handleProdutoChange}
                required
              >
                <option value="">Selecione um produto...</option>
                {produtos.map(p => (
                  <option key={p.id} value={p.nome}>{p.nome}</option>
                ))}
                <option value="__NOVO__">➕ Cadastrar novo produto...</option>
              </select>
            ) : (
              <div style={{ display: 'flex', gap: 8 }}>
                <input
                  type="text"
                  className={`form-input ${erros.produto ? 'has-error' : ''}`}
                  placeholder="Nome do novo produto..."
                  value={form.novoProdutoNome}
                  onChange={e => setForm({ ...form, novoProdutoNome: e.target.value })}
                  autoFocus
                  required
                />
                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  onClick={() => {
                    setIsNovoProduto(false);
                    setForm({ ...form, novoProdutoNome: '' });
                  }}
                >
                  Voltar
                </button>
              </div>
            )}
            {erros.produto && <span className="field-error">{erros.produto}</span>}
          </div>

          {/* Valor */}
          <div className="form-group">
            <label className="form-label">Valor (R$) *</label>
            <input
              type="number"
              step="0.01"
              placeholder="Ex: 35.00"
              className={`form-input ${erros.valor ? 'has-error' : ''}`}
              value={form.valor}
              onChange={e => setForm({ ...form, valor: e.target.value })}
              required
            />
            {erros.valor && <span className="field-error">{erros.valor}</span>}
          </div>

          {/* Cliente */}
          <div className="form-group">
            <label className="form-label">Cliente (opcional)</label>
            <input
              type="text"
              className="form-input"
              placeholder="Deixe em branco se não lembrar"
              value={form.cliente}
              onChange={e => setForm({ ...form, cliente: e.target.value })}
            />
          </div>

          {/* Forma de Pagamento */}
          <div className="form-group">
            <label className="form-label">Forma de Pagamento *</label>
            <select
              className="form-select"
              value={form.formaPagamento}
              onChange={e => setForm({ ...form, formaPagamento: e.target.value })}
            >
              <option value="PIX">Pix</option>
              <option value="DINHEIRO">Dinheiro</option>
              <option value="DEBITO">Débito</option>
              <option value="CREDITO">Crédito</option>
              <option value="MAQUININHA">Maquininha</option>
              <option value="FIADO">Fiado</option>
              <option value="PAGAR_DEPOIS">Pagar depois</option>
              <option value="NAO_INFORMADO">Não lembro</option>
            </select>
          </div>

          {/* Condicionais de Cartão */}
          {ehCartao && (
            <>
              <div className="form-group">
                <label className="form-label">Bandeira do Cartão</label>
                <select
                  className="form-select"
                  value={form.bandeiraCartao}
                  onChange={e => setForm({ ...form, bandeiraCartao: e.target.value })}
                >
                  <option value="NAO_INFORMADO">Não informada</option>
                  <option value="VISA">Visa</option>
                  <option value="MASTER">Mastercard</option>
                  <option value="ELO">Elo</option>
                  <option value="OUTRA">Outra</option>
                </select>
              </div>

              {form.formaPagamento === 'CREDITO' && (
                <div className="form-group">
                  <label className="form-label">Número de Parcelas *</label>
                  <input
                    type="number"
                    min="1"
                    className="form-input"
                    value={form.parcelas}
                    onChange={e => setForm({ ...form, parcelas: e.target.value })}
                  />
                  <span className="field-hint">Padrão: 1 parcela (à vista)</span>
                </div>
              )}
            </>
          )}

          {/* Forma de Entrega */}
          <div className="form-group">
            <label className="form-label">Entrega *</label>
            <select
              className="form-select"
              value={form.formaEntrega}
              onChange={e => setForm({ ...form, formaEntrega: e.target.value })}
            >
              <option value="LOJA">Loja</option>
              <option value="PRESENCIAL">Presencial</option>
              <option value="CORREIOS">Correios</option>
              <option value="SEDEX">Sedex</option>
              <option value="MOTOBOY">Motoboy</option>
              <option value="RETIRADA">Ela veio buscar</option>
            </select>
          </div>

          {/* Código de Rastreio (Condicional) */}
          {ehEntregaComRastreio && (
            <div className="form-group">
              <label className="form-label">Código de Rastreio *</label>
              <input
                type="text"
                placeholder="Ex: AA123456789BR"
                className={`form-input ${erros.codigoRastreio ? 'has-error' : ''}`}
                value={form.codigoRastreio}
                onChange={e => setForm({ ...form, codigoRastreio: e.target.value })}
                required
              />
              {erros.codigoRastreio && <span className="field-error">{erros.codigoRastreio}</span>}
            </div>
          )}
        </div>

        {/* Observações */}
        <div className="form-group" style={{ marginTop: 18 }}>
          <label className="form-label">Observações (opcional)</label>
          <textarea
            rows="2"
            className="form-textarea"
            placeholder="Ex: pediu embrulho para presente, avisou que busca amanhã..."
            value={form.observacao}
            onChange={e => setForm({ ...form, observacao: e.target.value })}
          />
        </div>

        <div style={{ marginTop: 24 }}>
          <button type="submit" className="btn btn-primary" disabled={salvando}>
            {salvando ? 'Salvando venda...' : 'Salvar venda'}
          </button>
        </div>
      </form>
    </div>
  );
}

