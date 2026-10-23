import React, { useState, useEffect } from 'react';

export default function ModalEditarVenda({ venda, produtos, iaConfigurada, onClose, onSalvo }) {
  const [formData, setFormData] = useState({
    tipo: venda.tipo || 'VENDA',
    data: venda.data || '',
    produto: venda.produto || '',
    valor: venda.valor || '',
    cliente: venda.cliente === 'Não informado' ? '' : venda.cliente,
    formaPagamento: venda.formaPagamento || 'PIX',
    bandeiraCartao: venda.bandeiraCartao || 'NAO_INFORMADO',
    parcelas: venda.parcelas || 1,
    formaEntrega: venda.formaEntrega || 'LOJA',
    codigoRastreio: venda.codigoRastreio || '',
    observacao: venda.observacao || '',
    precisaRevisao: false, // Ao corrigir, resolve a pendência
    sugestaoIaAceita: venda.sugestaoIaAceita ?? null
  });

  const [loadingIa, setLoadingIa] = useState(false);
  const [sugestaoIa, setSugestaoIa] = useState(null);
  const [erroIa, setErroIa] = useState(null);
  const [errosValidacao, setErrosValidacao] = useState({});
  const [salvando, setSalvando] = useState(false);

  const precisaConferir = venda.precisaRevisao;

  const handlePedirSugestao = async () => {
    setLoadingIa(true);
    setSugestaoIa(null);
    setErroIa(null);

    try {
      const res = await fetch('http://localhost:8080/api/ia/sugestao', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          vendaId: venda.id,
          motivoRevisao: venda.motivoRevisao
        })
      });
      const data = await res.json();
      if (data.disponivel) {
        setSugestaoIa(data);
      } else {
        setErroIa(data.mensagem || 'A sugestão não está disponível agora. Você pode preencher a venda manualmente.');
      }
    } catch (err) {
      setErroIa('A sugestão não está disponível agora. Você pode preencher a venda manualmente.');
    } finally {
      setLoadingIa(false);
    }
  };

  const handleAplicarSugestao = () => {
    if (!sugestaoIa) return;

    const campo = sugestaoIa.campo;
    const valor = sugestaoIa.valorSugerido;

    setFormData(prev => {
      const updated = { ...prev, sugestaoIaAceita: true };
      if (campo === 'data') updated.data = valor;
      if (campo === 'valor') updated.valor = valor;
      if (campo === 'forma_pagamento') updated.formaPagamento = valor;
      if (campo === 'forma_entrega') updated.formaEntrega = valor;
      if (campo === 'cliente') updated.cliente = valor;
      if (campo === 'produto') updated.produto = valor;
      return updated;
    });

    setSugestaoIa(null);
  };

  const handleRecusarSugestao = () => {
    setFormData(prev => ({ ...prev, sugestaoIaAceita: false }));
    setSugestaoIa(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSalvando(true);
    setErrosValidacao({});

    try {
      const payload = {
        ...formData,
        cliente: formData.cliente?.trim() || 'Não informado',
        parcelas: (formData.formaPagamento === 'CREDITO') ? (Number(formData.parcelas) || 1) : formData.parcelas,
        precisaRevisao: false, // Marcada como resolvida
        motivoRevisao: null
      };

      const res = await fetch(`http://localhost:8080/api/vendas/${venda.id}`, {
        method: 'PUT',
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
          setErrosValidacao(map);
        } else {
          alert(errorData.mensagem || 'Erro ao salvar alterações da venda.');
        }
        setSalvando(false);
        return;
      }

      const vendaAtualizada = await res.json();
      onSalvo(vendaAtualizada);
      onClose();
    } catch (err) {
      alert('Não foi possível conectar ao servidor. Verifique se o backend está ligado.');
      setSalvando(false);
    }
  };

  const ehCartao = ['DEBITO', 'CREDITO', 'MAQUININHA'].includes(formData.formaPagamento);
  const ehEntregaComRastreio = ['CORREIOS', 'SEDEX'].includes(formData.formaEntrega);

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <div>
            <h2 className="modal-title">Editar Venda #{venda.id}</h2>
            {venda.linhaOriginal && (
              <p className="brand-subtitle">Linha {venda.linhaOriginal} da planilha original</p>
            )}
          </div>
          <button className="modal-close-btn" onClick={onClose}>&times;</button>
        </div>

        {precisaConferir && (
          <div className="alert alert-error" style={{ marginBottom: 16 }}>
            <div>
              <strong>Atenção:</strong> {venda.motivoRevisao || 'Esta linha precisa de conferência.'}
            </div>
          </div>
        )}

        {/* Integração com IA */}
        {precisaConferir && iaConfigurada && (
          <div style={{ marginBottom: 18 }}>
            {!sugestaoIa && !loadingIa && (
              <button
                type="button"
                className="btn btn-warning btn-sm"
                onClick={handlePedirSugestao}
              >
                ✨ Pedir sugestão da Inteligência Artificial
              </button>
            )}

            {loadingIa && (
              <div className="alert alert-info">
                <span className="spinner"></span>
                <span>Pensando na sugestão...</span>
              </div>
            )}

            {erroIa && (
              <div className="alert alert-info">
                {erroIa}
              </div>
            )}

            {sugestaoIa && (
              <div className="suggestion-box">
                <div className="suggestion-header">
                  💡 Sugestão Inteligente
                </div>
                <div className="suggestion-detail">
                  <p><strong>Na planilha estava:</strong> {sugestaoIa.valorOriginal || '(em branco)'}</p>
                  <p><strong>Sugestão:</strong> {sugestaoIa.valorSugerido}</p>
                  <p><strong>Por quê:</strong> {sugestaoIa.explicacao}</p>
                </div>
                <div className="suggestion-actions">
                  <button
                    type="button"
                    className="btn btn-success btn-sm"
                    onClick={handleAplicarSugestao}
                  >
                    Usar essa sugestão
                  </button>
                  <button
                    type="button"
                    className="btn btn-secondary btn-sm"
                    onClick={handleRecusarSugestao}
                  >
                    Deixar como está
                  </button>
                </div>
              </div>
            )}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-grid">
            <div className="form-group">
              <label className="form-label">Tipo de Registro *</label>
              <select
                className="form-select"
                value={formData.tipo}
                onChange={e => setFormData({ ...formData, tipo: e.target.value })}
              >
                <option value="VENDA">Venda</option>
                <option value="COMPRA">Compra (Estoque)</option>
                <option value="DEVOLUCAO">Devolução</option>
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Data *</label>
              <input
                type="date"
                className={`form-input ${errosValidacao.data ? 'has-error' : ''}`}
                value={formData.data}
                onChange={e => setFormData({ ...formData, data: e.target.value })}
                required
              />
              {errosValidacao.data && <span className="field-error">{errosValidacao.data}</span>}
            </div>

            <div className="form-group">
              <label className="form-label">Produto *</label>
              <input
                type="text"
                className={`form-input ${errosValidacao.produto ? 'has-error' : ''}`}
                value={formData.produto}
                onChange={e => setFormData({ ...formData, produto: e.target.value })}
                required
              />
              {errosValidacao.produto && <span className="field-error">{errosValidacao.produto}</span>}
            </div>

            <div className="form-group">
              <label className="form-label">Valor (R$) *</label>
              <input
                type="number"
                step="0.01"
                className={`form-input ${errosValidacao.valor ? 'has-error' : ''}`}
                value={formData.valor}
                onChange={e => setFormData({ ...formData, valor: e.target.value })}
                required
              />
              {errosValidacao.valor && <span className="field-error">{errosValidacao.valor}</span>}
            </div>

            <div className="form-group">
              <label className="form-label">Nome do Cliente</label>
              <input
                type="text"
                className="form-input"
                placeholder="Deixe em branco se não lembrar"
                value={formData.cliente}
                onChange={e => setFormData({ ...formData, cliente: e.target.value })}
              />
            </div>

            <div className="form-group">
              <label className="form-label">Como Pagou *</label>
              <select
                className="form-select"
                value={formData.formaPagamento}
                onChange={e => setFormData({ ...formData, formaPagamento: e.target.value })}
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

            {ehCartao && (
              <>
                <div className="form-group">
                  <label className="form-label">Bandeira do Cartão</label>
                  <select
                    className="form-select"
                    value={formData.bandeiraCartao || 'NAO_INFORMADO'}
                    onChange={e => setFormData({ ...formData, bandeiraCartao: e.target.value })}
                  >
                    <option value="NAO_INFORMADO">Não informada</option>
                    <option value="VISA">Visa</option>
                    <option value="MASTER">Mastercard</option>
                    <option value="ELO">Elo</option>
                    <option value="OUTRA">Outra</option>
                  </select>
                </div>

                <div className="form-group">
                  <label className="form-label">Número de Parcelas</label>
                  <input
                    type="number"
                    min="1"
                    className="form-input"
                    value={formData.parcelas || 1}
                    onChange={e => setFormData({ ...formData, parcelas: e.target.value })}
                  />
                </div>
              </>
            )}

            <div className="form-group">
              <label className="form-label">Entrega *</label>
              <select
                className="form-select"
                value={formData.formaEntrega}
                onChange={e => setFormData({ ...formData, formaEntrega: e.target.value })}
              >
                <option value="LOJA">Loja</option>
                <option value="PRESENCIAL">Presencial</option>
                <option value="CORREIOS">Correios</option>
                <option value="SEDEX">Sedex</option>
                <option value="MOTOBOY">Motoboy</option>
                <option value="RETIRADA">Ela veio buscar</option>
              </select>
            </div>

            {ehEntregaComRastreio && (
              <div className="form-group">
                <label className="form-label">Código de Rastreio *</label>
                <input
                  type="text"
                  className={`form-input ${errosValidacao.codigoRastreio ? 'has-error' : ''}`}
                  placeholder="Ex: AA123456789BR"
                  value={formData.codigoRastreio}
                  onChange={e => setFormData({ ...formData, codigoRastreio: e.target.value })}
                  required
                />
                {errosValidacao.codigoRastreio && <span className="field-error">{errosValidacao.codigoRastreio}</span>}
              </div>
            )}
          </div>

          <div className="form-group" style={{ marginTop: 16 }}>
            <label className="form-label">Observações</label>
            <textarea
              className="form-textarea"
              rows="2"
              value={formData.observacao}
              onChange={e => setFormData({ ...formData, observacao: e.target.value })}
              placeholder="Informações adicionais da venda..."
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12, marginTop: 24 }}>
            <button type="button" className="btn btn-secondary" onClick={onClose} disabled={salvando}>
              Cancelar
            </button>
            <button type="submit" className="btn btn-primary" disabled={salvando}>
              {salvando ? 'Salvando...' : 'Salvar e Confirmar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

